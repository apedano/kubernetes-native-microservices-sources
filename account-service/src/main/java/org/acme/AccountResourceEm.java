package org.acme;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.model.Account;

import java.math.BigDecimal;
import java.util.List;

@Path("/accounts/em")
public class AccountResourceEm {

    @Inject
    EntityManager entityManager;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Account> allAccounts() {
        return entityManager
                .createNamedQuery("Accounts.findAll", Account.class)
                .getResultList();
    }

    @GET
    @Path("/{accountNumber}")
    @Produces(MediaType.APPLICATION_JSON)
    public Account getAccount(@PathParam("accountNumber") Long accountNumber) {
        try {
            return entityManager
                    .createNamedQuery("Accounts.findByAccountNumber", Account.class)
                    .setParameter("accountNumber", accountNumber)
                    .getSingleResult();
        } catch (NoResultException nre) {
            throw new WebApplicationException("Account with " + accountNumber
                    + " does not exist.", 404);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createAccount(Account account) {
        if (account.getAccountNumber() == null) {
            throw new WebApplicationException("No Account number specified.", 400);
        }
        entityManager.persist(account);
        return Response.status(201).entity(account).build();
    }

    @POST
    @Path("withdraw/{accountNumber}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Account withdrawal(@PathParam("accountNumber") Long accountNumber,
                              String amount) {
        Account entity = getAccount(accountNumber);
        Account updatedAccount = entity.withdrawFunds(new BigDecimal(amount));
        entityManager.merge(updatedAccount);
        return entity;
    }

}
