package org.acme;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import io.smallrye.reactive.messaging.memory.InMemorySink;
import io.smallrye.reactive.messaging.memory.InMemorySource;
import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;
import org.acme.model.*;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@QuarkusTestResource(InMemoryLifecycleManager.class)
public class AccountArResourceEventsTest {

    //Injects an InMemoryConnector into the test for interacting with a channel.
    @Inject
    //@Any on the injection point is needed because the instance to be injected has a qualifier present,
    //indicating any qualifiers can be ignored.
    @Any
    InMemoryConnector connector;


    @Test
    void testOverdraftEvent() {
        //Retrieves the sink for the account-overdrawn channel from the InMemoryConnector.
        // The sink receives any events sent to the channel.
        InMemorySink<Overdrawn> overdrawnSink = connector.sink("account-overdrawn");

        AccountAr account =
                given()
                        .when().get("/accounts/active-record/{accountNumber}", 78790)
                        .then().statusCode(200)
                        .extract().as(AccountAr.class);
        BigDecimal withdrawal = new BigDecimal("23.82");
        //First withdrawal without overdrawn
        BigDecimal expectedBalance = account.balance.subtract(withdrawal);
                account =
                given()
                        .contentType(ContentType.JSON)
                        .body(withdrawal.toString())
                        .when().put("/accounts/active-record/{accountNumber}/withdraw", 78790)
                        .then().statusCode(200)
                        .extract().as(AccountAr.class);
        // Asserts verifying account and balance have been removed.
        assertThat(overdrawnSink.received().size()).isEqualTo(0);
                withdrawal = new BigDecimal("6000.00");
        assertThat(account.balance).isEqualTo(expectedBalance);
        expectedBalance = account.balance.subtract(withdrawal);
                account =
                given()
                        .contentType(ContentType.JSON)
                        .body(withdrawal.toString())
                        .when().put("/accounts/active-record/{accountNumber}/withdraw", 78790)
                        .then().statusCode(200)
                        .extract().as(AccountAr.class);
        // Asserts verifying account and customer details have been removed.
        assertThat(account.accountStatus).isEqualTo(AccountStatus.OVERDRAWN);
        assertThat(overdrawnSink.received().size()).isEqualTo(1);
        Message<Overdrawn> overdrawnMsg = overdrawnSink.received().get(0);
        assertThat(overdrawnMsg).isNotNull();
        Overdrawn overdrawn = overdrawnMsg.getPayload();
        assertThat(overdrawn.accountNumber).isEqualTo(78790L);
        assertThat(overdrawn.customerNumber).isEqualTo(444222L);
        assertThat(overdrawn.balance).isEqualTo(expectedBalance);
        assertThat(overdrawn.overdraftLimit).isEqualTo(new BigDecimal("-200.00"));
    }

    @Test
    void testOverdraftUpdate() {
        InMemorySource<OverdraftLimitUpdate> source =
                connector.source("overdraft-update");
        AccountAr account =
                given()
                        .when().get("/accounts/active-record/{accountNumber}", 123456789)
                        .then().statusCode(200)
                        .extract().as(AccountAr.class);
        // Asserts verifying account and balance have been removed.
        assertThat(account.overdraftLimit).isEqualTo(new BigDecimal("-200.00"));
        OverdraftLimitUpdate updateEvent = new OverdraftLimitUpdate();
        updateEvent.setAccountNumber(123456789L);
        updateEvent.setNewOverdraftLimit(new BigDecimal("-600.00"));
        source.send(updateEvent); //this will be received by the code
                account =
                given()
                        .when().get("/accounts/active-record/{accountNumber}", 123456789)
                        .then().statusCode(200)
                        .extract().as(AccountAr.class);
        // Asserts verifying account and balance have been removed.
        assertThat(account.overdraftLimit).isEqualTo(new BigDecimal("-600.00"));
    }
}
