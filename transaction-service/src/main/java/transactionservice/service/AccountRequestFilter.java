package transactionservice.service;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Method;

/*
We implement the ClientRequestFilter interface that we will use to define a JAX-RS provider
 */

public class AccountRequestFilter implements ClientRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountRequestFilter.class);

    /*
    This implementation adds the invoked method to the request as a param
     */
    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        LOGGER.info("Inside the AccountRequestFilter provider method");
        Method invokedMethod =
                (Method) requestContext.getProperty("org.eclipse.microprofile.rest.client.invokedMethod");
        requestContext.getHeaders().add("Invoked-Client-Method", invokedMethod.getName());
    }
}
