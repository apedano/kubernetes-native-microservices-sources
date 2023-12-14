package transactionservice.resource;


import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.contrib.kafka.TracingKafkaUtils;
import io.smallrye.reactive.messaging.kafka.api.IncomingKafkaRecordMetadata;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.eclipse.microprofile.opentracing.Traced;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import transactionservice.AccountFee;
import transactionservice.service.AccountService;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Path("/transactions")
@Slf4j
public class TransactionResource {
    @Inject
    Tracer tracer; //OpenTracing tracer
    @Inject
    @RestClient //CDI qualifier telling Quarkus to inject a type-safe REST client bean matching the interface
    AccountService accountService;

    @GET
    @Path("/config-secure/{acctnumber}/balance")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response secureConfigGetBalance(@PathParam("acctnumber") Long accountNumber) {
        return getBalance(accountNumber);
    }

    @GET
    @Path("/{acctNumber}/balance")
    @Traced(operationName = "get-balance-from-account-service")
    public Response getBalance(@PathParam("acctNumber") Long accountNumber) {
        BigDecimal balance = accountService.getBalance(accountNumber);
        return Response.ok(balance).build();
    }

    @POST
    @Path("/{acctNumber}")
    @Traced(operationName = "post-new-transaction")
    public Response newTransaction(@PathParam("acctNumber") Long accountNumber,
                                   BigDecimal amount) {
        accountService.withdraw(accountNumber, amount.multiply(BigDecimal.valueOf(-1)));
        return Response.ok().build();
    }

    @Incoming("overdraft-fee")
    public CompletionStage<Void> processOverdraftFee(Message<AccountFee> message) {
        log.info("Received overdraft fee");
        if (message.getMetadata(IncomingKafkaRecordMetadata.class).isPresent()) {
            //we build the span for this operation
            Span span = tracer.buildSpan("insert-overdraft-fee")
                    //Makes the new span a child of the span extracted from the Kafka message on the next line
                    .asChildOf(
                            //context from the incoming message headers
                            TracingKafkaUtils.extractSpanContext(
                                    message.getMetadata(IncomingKafkaRecordMetadata
                                            .class).get().getHeaders(),
                                    tracer))
                    .start();
            //the scope of the span is autoclosable
            try (Scope scope = tracer.activateSpan(span)) {
                //Retrieves the current span context, and injects it into RecordHeaders
                TracingKafkaUtils.inject(span.context(), new RecordHeaders(), tracer);
            } finally {
                span.finish();
            }
        }
        AccountFee payload = message.getPayload();
        log.info("Inserting accountFee: #{} - {} euro", payload.accountNumber, payload.overdraftFee.setScale(2));
        return CompletableFuture.completedFuture(null);
    }

}
