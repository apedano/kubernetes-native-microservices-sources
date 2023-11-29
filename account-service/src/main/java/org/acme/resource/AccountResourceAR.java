package org.acme.resource;

import io.smallrye.common.annotation.Blocking;
import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.acme.model.*;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Path("/accounts/active-record")
@Slf4j
public class AccountResourceAR {

    @Inject
    @Channel("account-overdrawn")
    Emitter<Overdrawn> emitter;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<AccountAr> allAccounts() {
        return AccountAr.listAll();
    }

    @GET
    @Path("/{accountNumber}")
    @Produces(MediaType.APPLICATION_JSON)
    public AccountAr getAccount(@PathParam("accountNumber") Long accountNumber) {
        try {
            return AccountAr.findByAccountNumber(accountNumber);
        } catch (NoResultException nre) {
            throw new WebApplicationException("Account with " + accountNumber
                    + " does not exist.", 404);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response createAccount(AccountAr account) {
        if (account.accountNumber == null) {
            throw new WebApplicationException("No Account number specified.", 404);
        }
        //FIXME: the generated id will be 1, so if a record with id 1
        //already exists it will generate ConstraintViolationException.
        account.persist();
        return Response.status(201).entity(account).build();
    }

    @PUT
    @Path("/{accountNumber}/withdraw")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public CompletionStage<AccountAr> withdrawal(@PathParam("accountNumber") Long accountNumber,
                                                 String amount) {
        log.info("Called withdrawal for account #{} with amount {}", accountNumber, amount);
        AccountAr entity = getAccount(accountNumber);
        try {
            entity.withdrawFunds(new BigDecimal(amount));
            entity.persist();
            if(entity.accountStatus.equals(AccountStatus.OVERDRAWN)) {
                return handleOverdrawn(entity);
            }
            return CompletableFuture.completedFuture(entity);
        } catch (AccountOverdrawnException e) {
            throw new WebApplicationException(
                    "Account is overdrawn, no further withdrawals permitted",
                    Response.Status.PRECONDITION_FAILED);
        }
    }


    @Incoming("overdraft-update")
    @Blocking //needed for the interaction with the DB
    @Transactional //it will persist the updated account
    public void processOverdraftUpdate(OverdraftLimitUpdate overdraftLimitUpdate) {
        AccountAr account =
                AccountAr.findByAccountNumber(overdraftLimitUpdate.getAccountNumber());
        account.overdraftLimit = overdraftLimitUpdate.getNewOverdraftLimit();
    }

    private CompletionStage<AccountAr> handleOverdrawn(AccountAr entity) {
        Overdrawn overdrawn = new Overdrawn(entity);
        log.info("Sending account overdrawn notification");
        CompletionStage<Void> sendFuture = emitter.send(overdrawn);
        return sendFuture.thenCompose(empty -> CompletableFuture.completedFuture(entity));
    }

    private CompletionStage<AccountAr> handleOverdrawnWithMessage(AccountAr entity) {
        Overdrawn payload = new Overdrawn(entity);
        CompletableFuture<AccountAr> future = new CompletableFuture<>();
        OutgoingKafkaRecordMetadata<String> myRecordMetadata = OutgoingKafkaRecordMetadata.
                <String>builder()
                .withKey("my-key") //if we want to make the key explicit
                .withTopic("myDynamicTopic") //
                .build();
        Message<Overdrawn> message =
                Message.of(payload)
                        .addMetadata(myRecordMetadata)
                        .withAck(() -> {
                            log.info("The account #{} overdrawn has been acked", entity.accountNumber);
                            future.complete(entity);
                            return CompletableFuture.completedFuture(null);
                        })
                        .withNack(throwable -> {
                            log.info("The account #{} overdrawn has been nacked", entity.accountNumber);
                            future.completeExceptionally(throwable);
                            return CompletableFuture.completedFuture(null);
                        });
        emitter.send(message); //this is a void method
        return future; //we return
    }

}
