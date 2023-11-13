package org.acme.resource;

import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.model.Account;
import org.acme.repo.AccountRepository;

import java.math.BigDecimal;
import java.util.List;

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
        accountRepository.persist(updatedAccount);
        return entity;
    }

}
