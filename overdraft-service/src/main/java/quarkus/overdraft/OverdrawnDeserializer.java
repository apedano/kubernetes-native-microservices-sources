package quarkus.overdraft;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import quarkus.overdraft.events.Overdrawn;

public class OverdrawnDeserializer extends ObjectMapperDeserializer<Overdrawn> {
    public OverdrawnDeserializer() {
        super(Overdrawn.class);
    }
}

