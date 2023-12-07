package transactionservice;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class AccountFeeDeserializer extends ObjectMapperDeserializer<AccountFee> {
    public AccountFeeDeserializer() {
        super(AccountFee.class);
    }
}
