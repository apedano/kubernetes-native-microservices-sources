package transactionservice.service;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

@Path("/accounts/active-record") //the same as in the AccountResource
@RegisterRestClient(configKey = "account-service") //the properties name component to configure the client
//Indicates that the interface should have a CDI bean created that can be injected into classes
@ClientHeaderParam(name = "class-level-param", value = "AccountServiceinterface") //Adds class-level-param to the outgoing HTTP request header.
// At class level it will be added to all methods
@RegisterClientHeaders //Indicates the default ClientHeadersFactory should be used.
    // The default factory will propagate any
    //  headers from an inbound JAX-RS request onto the outbound client request, where the headers are
    // added as a comma-separated list into the configuration key named
    // org.eclipse.microprofile.rest.client.propagateHeaders. A custom ClientHeadersFactory can also be added
@RegisterProvider(AccountRequestFilter.class) //Adds the ClientRequestFilter Rest service interface impl as provider
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface AccountService {
    @GET
    @Path("/{acctNumber}/balance")
    BigDecimal getBalance(@PathParam("acctNumber") Long accountNumber);

    @GET
    @Path("/jwt-secure/{acctNumber}/balance")
    BigDecimal getBalanceJwtSecured(@PathParam("acctNumber") Long accountNumber);

    @PUT
    @Path("{accountNumber}/withdraw")
    void withdraw(@PathParam("accountNumber") Long accountNumber,
                  BigDecimal amount);

//    @POST
//    @Path("{accountNumber}/transaction-async")
//    CompletionStage<Void> transactAsync(@PathParam("accountNumber") Long
//                                                accountNumber, BigDecimal amount);
//
//    @POST
//    @Path("{accountNumber}/transaction-headers")
//    @ClientHeaderParam(name = "method-level-param", value = "{generateParameterValue}")
//        //Similar to the usage of @ClientHeaderParam on the type, it adds the method-level-param header to
//        //the outbound HTTP request to the external service.
//    Map<String, List<String>> transactionHeaders(@PathParam("accountNumber") Long
//                                                         accountNumber, BigDecimal amount, @Context HttpHeaders httpHeaders);
//
//    /**
//     * Default method on the interface used to create a value for the header on transactionHeaders
//     * @return
//     */
//    default String generateParameterValue() {
//        return "Value generated in method for async call";
//    }
}