package org.acme.model;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class OverdraftLimitUpdateDeserializer extends ObjectMapperDeserializer<OverdraftLimitUpdate> {
    public OverdraftLimitUpdateDeserializer() {
        super(OverdraftLimitUpdate.class);
    }
}
