package quarkus.overdraft;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import io.smallrye.reactive.messaging.memory.InMemorySink;
import io.smallrye.reactive.messaging.memory.InMemorySource;
import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.Test;
import quarkus.overdraft.events.AccountFee;
import quarkus.overdraft.events.OverdraftLimitUpdate;
import quarkus.overdraft.events.Overdrawn;


import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;


@QuarkusTest
@QuarkusTestResource(InMemoryLifecycleManager.class)
public class OverdraftResourceEventsTest {
  @Inject
  @Any
  InMemoryConnector connector;

  @Test
  void testOverdraftEvent() {
    InMemorySource<Overdrawn> overdrawnSource = connector.source("account-overdrawn");
    InMemorySink<AccountFee> overdraftSink = connector.sink("overdraft-fee");

    Overdrawn overdrawn = new Overdrawn(121212L, 212121L, new BigDecimal("-185.00"), new BigDecimal("-200.00"));
    overdrawnSource.send(overdrawn);

    await().atMost(3, TimeUnit.SECONDS).until(() -> overdraftSink.received().size() == 1);

    Message<AccountFee> overdraftFeeMessage = overdraftSink.received().get(0);
    assertThat(overdraftFeeMessage).isNotNull();

    AccountFee feePayload = overdraftFeeMessage.getPayload();
    assertThat(feePayload).isNotNull();
    assertThat(feePayload.accountNumber).isEqualTo(121212L);
    assertThat(feePayload.overdraftFee).isEqualTo(new BigDecimal("15.00"));

    overdrawn = new Overdrawn(33443344L, 656565L, new BigDecimal("-98.00"), new BigDecimal("-200.00"));
    overdrawnSource.send(overdrawn);

    await().atMost(3, TimeUnit.SECONDS).until(() -> overdraftSink.received().size() == 2);

    overdraftFeeMessage = overdraftSink.received().get(1);
    assertThat(overdraftFeeMessage).isNotNull();

    feePayload = overdraftFeeMessage.getPayload();
    assertThat(feePayload).isNotNull();
    assertThat(feePayload.accountNumber).isEqualTo(33443344L);
    assertThat(feePayload.overdraftFee).isEqualTo(new BigDecimal("15.00"));

    overdrawn = new Overdrawn(121212L, 212121L, new BigDecimal("-285.00"), new BigDecimal("-300.00"));
    overdrawnSource.send(overdrawn);

    await().atMost(3, TimeUnit.SECONDS).until(() -> overdraftSink.received().size() == 3);

    overdraftFeeMessage = overdraftSink.received().get(2);
    assertThat(overdraftFeeMessage).isNotNull();

    feePayload = overdraftFeeMessage.getPayload();
    assertThat(feePayload).isNotNull();
    assertThat(feePayload.accountNumber).isEqualTo(121212L);
    assertThat(feePayload.overdraftFee).isEqualTo(new BigDecimal("30.00"));

    overdrawn = new Overdrawn(878897L, 212121L, new BigDecimal("-76.00"), new BigDecimal("-200.00"));
    overdrawnSource.send(overdrawn);

    await().atMost(3, TimeUnit.SECONDS).until(() -> overdraftSink.received().size() == 4);

    overdraftFeeMessage = overdraftSink.received().get(3);
    assertThat(overdraftFeeMessage).isNotNull();
    feePayload = overdraftFeeMessage.getPayload();
    assertThat(feePayload).isNotNull();
    assertThat(feePayload.accountNumber).isEqualTo(878897L);
    assertThat(feePayload.overdraftFee).isEqualTo(new BigDecimal("35.00"));
  }

  @Test
  void testUpdateOverdraftEvent() {
    InMemorySink<OverdraftLimitUpdate> limitUpdateSink = connector.sink("overdraft-update");

    given()
        .contentType(ContentType.JSON)
        .body("-550.00")
        .when().put("/overdraft/{accountNumber}", 4324321)
        .then()
        .statusCode(204);

    assertThat(limitUpdateSink.received().size()).isEqualTo(1);

    Message<OverdraftLimitUpdate> updateEventMsg = limitUpdateSink.received().get(0);
    assertThat(updateEventMsg).isNotNull();
    OverdraftLimitUpdate updatePayload = updateEventMsg.getPayload();
    assertThat(updatePayload).isNotNull();
    assertThat(updatePayload.accountNumber).isEqualTo(4324321L);
    assertThat(updatePayload.newOverdraftLimit).isEqualTo(new BigDecimal("-550.00"));
  }
}
