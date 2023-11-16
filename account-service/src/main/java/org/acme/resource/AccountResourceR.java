package org.acme.resource;

import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.model.Account;
import org.acme.model.AccountStatus;
import org.acme.repo.AccountRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Path("/accounts/repository")
public class AccountResourceR {

    @Inject
    AccountRepository accountRepository;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Account> allAccounts() {
        return accountRepository.listAll();
    }

    @GET
    @Path("/{accountNumber}")
    @Produces(MediaType.APPLICATION_JSON)
    public Account getAccount(@PathParam("accountNumber") Long accountNumber) {
        try {
            return accountRepository.findByAccountNumber(accountNumber);
        } catch (NoResultException nre) {
            throw new WebApplicationException("Account with " + accountNumber
                    + " does not exist.", 404);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response createAccount(Account account) {
        if (account.getAccountNumber() == null) {
            throw new WebApplicationException("No Account number specified.", 404);
        }
        accountRepository.persistAndFlush(account);
        return Response.status(201).entity(account).build();
    }

    @POST
    @Path("withdraw/{accountNumber}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Account withdrawal(@PathParam("accountNumber") Long accountNumber,
                              String amount) {
        Account entity = getAccount(accountNumber);
        Account updatedAccount = (Account) entity.withdrawFunds(new BigDecimal(amount));
        if(updatedAccount.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            updatedAccount.setAccountStatus(AccountStatus.OVERDRAWN);
        }
        accountRepository.persist(updatedAccount);

        return entity;
    }

    @GET
    @Path("/{acctNumber}/balance")
    public BigDecimal getBalance(@PathParam("acctNumber") Long accountNumber) {
        return getAccount(accountNumber).getBalance();
    }

    @POST
    @Path("{accountNumber}/transaction")
    @Transactional
    public void transact(@PathParam("accountNumber") Long accountNumber,
                         BigDecimal amount) {
        Account account = getAccount(accountNumber);
        if (account.getAccountStatus().equals(AccountStatus.OVERDRAWN)) {
            throw new WebApplicationException("Account is overdrawn, no further withdrawals permitted", 409);
        }
        withdrawal(accountNumber, amount.toString());

    }

    @POST
    @Path("{accountNumber}/transaction-async")
    public CompletionStage<Void> transactAsync(@PathParam("accountNumber") Long accountNumber, BigDecimal amount) {
        return CompletableFuture.runAsync(() -> this.transact(accountNumber, amount));
    }


    @POST
    @Path("{accountNumber}/transaction-headers")
    public Map<String, List<String>> transactionHeaders(@PathParam("accountNumber") Long
                                                                accountNumber, BigDecimal amount,
                                                        @Context HttpHeaders httpHeaders) { //@Context is equivalent to @Inject for CDI in the JAX-RS
        //returns the headers from the incoming call including the class and method level ones defined in the AccountService interface
        return httpHeaders.getRequestHeaders();
    }

}
