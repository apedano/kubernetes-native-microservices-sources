package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.acme.service.CircuitBreakerBackendService.Outcome.*;


/**
 * This class resambles the behavior described in the 'How a circuit breaker works' paragraph's picture
 */
@ApplicationScoped
@Slf4j
public class CircuitBreakerBackendService {

    private static final List<Outcome> OUTCOME_SEQUENCE = Arrays.asList(SUCCESS, FAILURE, FAILURE, SUCCESS);

    private final AtomicInteger atomicInteger = new AtomicInteger();

    public Outcome callService() {

        Outcome outcome = OUTCOME_SEQUENCE.get(atomicInteger.getAndIncrement() % OUTCOME_SEQUENCE.size());
        log.info("Called CircuitBreakerBackendService at {} returning {}", new Date(), outcome);
        if(outcome.equals(FAILURE)) {
            throw new WebApplicationException("Exception due to failure outcome");
        }
        return outcome;
    }

    public enum Outcome {
        SUCCESS, FAILURE
    }

}
