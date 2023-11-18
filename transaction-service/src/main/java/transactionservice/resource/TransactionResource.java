package transactionservice.resource;


import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import transactionservice.service.AccountService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Path("/transactions")
@Slf4j
public class TransactionResource {
    @Inject
    @RestClient //CDI qualifier telling Quarkus to inject a type-safe REST client bean matching the interface
    AccountService accountService;

    @GET
    @Path("/{acctNumber}/balance")
    public Response balance(@PathParam("acctNumber") Long accountNumber) {
        BigDecimal balance = accountService.getBalance(accountNumber);
        return Response.ok(balance).build();
    }

    @POST
    @Path("/{acctNumber}")
    public Response newTransaction(@PathParam("acctNumber") Long accountNumber,
                                   BigDecimal amount) {
        accountService.transact(accountNumber, amount);
        return Response.ok().build();
    }

    @POST
    @Path("/{acctNumber}/headers")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response newTransactionHeaders(@PathParam("acctNumber") Long accountNumber,
                                   BigDecimal amount, @Context HttpHeaders httpHeaders) {
        log.info("In the newTransactionHeaders method");
        log.info("Httpheaders:" + httpHeaders.getRequestHeaders());
        Map<String, List<String>> headersFromBody = accountService.transactionHeaders(accountNumber, amount, httpHeaders);
        return Response
                .status(Response.Status.OK)
                .entity(headersFromBody)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
