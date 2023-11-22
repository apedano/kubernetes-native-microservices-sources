package org.acme.resource;


import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.acme.service.CircuitBreakerBackendService;
import org.acme.service.ServiceForRetry;
import org.eclipse.microprofile.faulttolerance.*;
import org.eclipse.microprofile.faulttolerance.exceptions.BulkheadException;
import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException;

import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;

@Path("resilience")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
public class ResilienceStrategiesResource {
    @Inject
    ServiceForRetry serviceForRetry;

    @Inject
    CircuitBreakerBackendService circuitBreakerBackendService;

    private final AtomicInteger availableDrivers = new AtomicInteger(2);
    private final AtomicInteger deliveriesDone = new AtomicInteger(0);

    @GET
    @Path("/do-delivery-bulkhead")
    @Bulkhead(value = 2) //the value of allowed concurrent calls of this method
    @Fallback(fallbackMethod = "bulkheadFallback", //Method name of the fallback impl with the same signature as the resource method
            applyOn = {BulkheadException.class})
    public Response doDelivery() throws InterruptedException {
        log.info("Called doDelivery...");
        //if the bulkhead value is the same as the number of
        //available drivers this condition will never be true
        if (availableDrivers.get() == 0) {
            log.info("No drivers available. Calling fallback");
            throw new BulkheadException("No drivers available");
        }
        log.info("Using driver #{}", availableDrivers.getAndDecrement());
        sleep(3000);

        log.info("Delivery #{} executed. #{} drivers available",
                deliveriesDone.incrementAndGet(),
                availableDrivers.incrementAndGet());
        return Response.ok().entity(deliveriesDone.get()).build();
    }

    public Response bulkheadFallback() {
        log.info("Falling back to ResilienceStrategiesResource#bulkheadFallback()");
        return Response.status(Response.Status.TOO_MANY_REQUESTS).entity("No drivers available").build();
    }

    @GET
    @Path("/call-with-timeout")
    @Timeout(100) //need to complete in less than 100 ms or a TimeoutException will be thrown
    @Fallback(fallbackMethod = "timeoutFallback") //if any exception is thrown
    public Response getTimeout() throws InterruptedException {
        sleep(200);
        return Response.ok().build();
    }

    public Response timeoutFallback() {
        return Response.status(Response.Status.GATEWAY_TIMEOUT).build();
    }

    @GET
    @Path("/call-retry")
    @Timeout(100)
    @Retry(delay = 100, //delay between reties
            jitter = 25, //variance of time between reties (85 - 125)
            maxRetries = 3,
            retryOn = TimeoutException.class) //other exceptions will be handled normally
    @Fallback(fallbackMethod = "timeoutFallback")
    public Response getRetry() {
        //the service method will delay the return (to trigger the timeout) for 2 times only
        return Response.ok().entity(serviceForRetry.doesNotDelayAfterRepeatedCalls()).build();
    }

    @GET
    @Path("/call-circuit-breaker")
    @Bulkhead(1)
    @CircuitBreaker(
            requestVolumeThreshold = 3,
            failureRatio = .66,
            delay = 3000,
            successThreshold = 2
    )
    @Fallback(value = TransactionServiceFallbackHandler.class) //handler class to support multiple type of exceptions
    public Response callCircuitBreakerService() {
        //the service will return the sequence in the picture
        // SUCCESS, FAILURE, FAILURE, FAILURE, FAILURE, SUCCESS, SUCCESS, SUCCESS
        return Response.ok().entity(circuitBreakerBackendService.callService()).build();
    }


}

