package org.acme;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.acme.model.Account;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.math.BigDecimal;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
//Tells Quarkus to start an H2 database prior to the tests being executed
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AccountResourceEmTest {
    @Test
    @Order(1)
    void testRetrieveAllEm() {
        Response result =
                given()
                        .when().get("/accounts/em")
                        .then()
                        .statusCode(200)
                        .body(
                                containsString("Debbie Hall"),
                                containsString("David Tennant"),
                                containsString("Alex Kingston")
                        )
                        .extract()
                        .response();
        List<Account> accounts = result.jsonPath().getList("$");
        assertThat(accounts).isNotEmpty()
                .hasSize(8);
    }

    @Test
    @Order(2)
    void testGetSingle() {
        Account result = get();

        assertThat(result)
                .isNotNull()
                .extracting(Account::getBalance)
                .isEqualTo(BigDecimal.valueOf(439.01));
    }



    @Test
    @Order(3)
    void testWithdraw() {
        Account result =
                given()
                        .contentType("application/json")
                        .body("100")
                        .when().post("/accounts/em/withdraw/78790")
                        .then()
                        .statusCode(200)
                        .extract()
                        .as(Account.class);

        assertThat(result)
                .isNotNull()
                .extracting(Account::getBalance)
                .isEqualTo(BigDecimal.valueOf(339.01));
    }

    @Test
    @Order(4)
    void testCreateAccountEm() {
        Account newAccount = new Account();
        newAccount.setAccountNumber(666999L);
        newAccount.setCustomerName("Ciccio Pasticcio");
        newAccount.setCustomerNumber(7777333L);
        newAccount.setBalance(BigDecimal.valueOf(12534.75));
        Account result =
                given()
                        .contentType("application/json")
                        .body(newAccount)
                        .when().post("/accounts/em")
                        .then()
                        .statusCode(201)
                        .extract()
                        .as(Account.class);

        assertThat(result).isNotNull()
                .extracting(Account::getId)
                .isNotNull();
    }

    private Account get() {
        return given()
                .when().get("/accounts/em/78790")
                .then()
                .statusCode(200)
                .body(
                        containsString("Vanna White")
                )
                .extract()
                .as(Account.class);
    }
}