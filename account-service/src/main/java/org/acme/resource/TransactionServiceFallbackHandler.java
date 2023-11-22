package org.acme.resource;

import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.faulttolerance.ExecutionContext;
import org.eclipse.microprofile.faulttolerance.FallbackHandler;

@Slf4j
public class TransactionServiceFallbackHandler implements FallbackHandler<Response> {

    @Override
    public Response handle(ExecutionContext executionContext) {
        Response response;

        String name = executionContext.getFailure().getCause() == null ?
                executionContext.getFailure().getClass().getSimpleName() :
                executionContext.getFailure().getCause().getClass().getSimpleName();
        switch (name) {
            case "BulkheadException":
                response = Response
                        .status(Response.Status.TOO_MANY_REQUESTS)
                        .build();

                break;
            case "TimeoutException":
                response = Response
                        .status(Response.Status.GATEWAY_TIMEOUT)
                    .build();
                break;
            case "CircuitBreakerOpenException":
                response = Response
                        .status(Response.Status.SERVICE_UNAVAILABLE)
                    .build();
                break;
            case "WebApplicationException":
                response = Response
                        .status(Response.Status.INTERNAL_SERVER_ERROR)
                        .build();
                break;
            case "HttpHostConnectException":
                response = Response
                        .status(Response.Status.BAD_GATEWAY)
                    .build();
                break;
            default:
                response = Response
                        .status(Response.Status.NOT_IMPLEMENTED)
                        .build();
        }
        log.info("******** "
                + executionContext.getMethod().getName()
                + ": " + name
                + " ********");
        return response;
    }
}
