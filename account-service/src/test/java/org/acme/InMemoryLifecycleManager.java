package org.acme;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;

import java.util.HashMap;
import java.util.Map;

public class InMemoryLifecycleManager implements QuarkusTestResourceLifecycleManager {
    @Override
    public Map<String, String> start() {
        Map<String, String> env = new HashMap<>();
        //Alters the incoming channel named overdraft-update to use the in-memory connector
        env.putAll(InMemoryConnector.switchIncomingChannelsToInMemory(
                "overdraft-update"));
        //The outgoing channel account-overdrawn switches to use the in-memory connector.
        env.putAll(InMemoryConnector.switchOutgoingChannelsToInMemory(
                "account-overdrawn"));
        return env;
    }

    @Override
    public void stop() {
        //Resets the configuration for any channels that were switched to the in-memory connector.
        InMemoryConnector.clear();
    }
}
