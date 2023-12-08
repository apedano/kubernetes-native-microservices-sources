package org.acme;

import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.model.Account;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Path("/accounts")
public class AccountResource {

    Set<Account> accounts = new HashSet<>();
    @PostConstruct
    public void setup() {
        accounts.add(new Account(123456789L, 987654321L, "George Baird", new
                BigDecimal("354.23")));
        accounts.add(new Account(121212121L, 888777666L, "Mary Taylor", new
                BigDecimal("560.03")));
        accounts.add(new Account(545454545L, 222444999L, "Diana Rigg", new
                BigDecimal("422.00")));
    }
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Set<Account> allAccounts() {
        return accounts;
    }

    @Tag(name = "get-by-accountnumber", description = "Separate grouping because we can")
    @APIResponse(responseCode = "200", description = "Successfully retrieved an account.",
            content = @Content(
                    schema = @Schema(implementation = Account.class))
    )
    @APIResponse(responseCode = "400", description = "Account with id of {accountNumber} does not exist.",
            content = @Content(
                    schema = @Schema(
                            implementation = ResourceErrorMapper.ErrorResponse.class,
                            example = "{\n" +
                                    "\"exceptionType\": \"javax.ws.rs.WebApplicationException\",\n" +
                                    "\"code\": 400,\n" +
                                    "\"error\": \"Account with id of 12345678 does not exist.\"\n" +
                            "}\n")
            )
    )
    @GET
    @Path("/{accountNumber}")
    @Produces(MediaType.APPLICATION_JSON)
    public Account getAccount(
            @Parameter(name = "accountNumber", description = "Number of the Account instance to be retrieved.",
                    required = true, in = ParameterIn.PATH, example = "123456789")
            @PathParam("accountNumber") Long accountNumber) {
        Optional<Account> response = accounts.stream()
                .filter(acct -> acct.getAccountNumber().equals(accountNumber))
                .findFirst();
        return response.orElseThrow(()
                -> new WebApplicationException("Account with id of " + accountNumber + " does not exist.", 404));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createAccount(Account account) {
        if (account.getAccountNumber() == null) {
            throw new WebApplicationException("No Account number specified.", 400);
        }
        accounts.add(account);
        return Response.status(201).entity(account).build();
    }
}
