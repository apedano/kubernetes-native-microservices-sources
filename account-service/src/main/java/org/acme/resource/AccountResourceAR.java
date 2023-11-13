package org.acme.resource;

import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.model.AccountAr;

import java.math.BigDecimal;
import java.util.List;

@Path("/accounts/active-record")
public class AccountResourceAR {

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

    @POST
    @Path("withdraw/{accountNumber}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public AccountAr withdrawal(@PathParam("accountNumber") Long accountNumber,
                                String amount) {
        AccountAr entity = getAccount(accountNumber);
        entity.withdrawFunds(new BigDecimal(amount));
        entity.persist();
        return entity;
    }

}
