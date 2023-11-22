package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
@Slf4j
public class ServiceForRetry {

    private static final Integer MAX_DELAY_CALLS = 2;

    private final Map<String, Integer> retriesPerThreadMap = new HashMap<>();

    public String doesNotDelayAfterRepeatedCalls() {
        String threadName = Thread.currentThread().getName();
        if (!retriesPerThreadMap.containsKey(threadName)) {
            log.info("Initialising map value");
            retriesPerThreadMap.put(threadName, 0);
        }
        int currentNumberOfCalls = retriesPerThreadMap.get(threadName);
        log.info("Call #{} for thread {}", currentNumberOfCalls, threadName);
        if (currentNumberOfCalls < MAX_DELAY_CALLS) {
            retriesPerThreadMap.put(threadName, currentNumberOfCalls + 1);
            log.info("Applying delay");
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        } else {
            log.info("Resetting the count, not applying delay");
            //restart the count for the thread
            retriesPerThreadMap.put(threadName, 0);
        }
        return "done doesNotDelayAfterRepeatedCalls";
    }


}
