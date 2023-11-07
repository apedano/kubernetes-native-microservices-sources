package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.acme.model.Account;
import org.acme.model.AccountStatus;
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
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AccountResourceTest {
    @Test
    @Order(1)
    void testRetrieveAll() {
        Response result =
                given()
                        .when().get("/accounts")
                .then()
                .statusCode(200)
                .body(
                        containsString("George Baird"),
                        containsString("Mary Taylor"),
                        containsString("Diana Rigg")
                )
                .extract()
                .response();
        List<Account> accounts = result.jsonPath().getList("$");
        assertThat(accounts).hasSize(3);
    }

    @Test
    @Order(2)
    void testGetAccount() {
        Account account =
                given()
                        .when().get("/accounts/{accountNumber}", 545454545)
                .then()
                .statusCode(200)
                .extract()
                .as(Account.class);

        assertThat(account)
                .extracting(Account::getAccountNumber, Account::getAccountStatus, Account::getCustomerNumber, Account::getCustomerName)
                .contains(545454545L, AccountStatus.OPEN, 222444999L, "Diana Rigg");
    }

    @Test
    @Order(3)
    void testCreateAccount() {
        Account newAccount = new Account(324324L, 112244L, "Sandy Holmes", new
                BigDecimal("154.55"));
        Account returnedAccount =
                given()
                        .contentType(ContentType.JSON)
                        .body(newAccount)
                        .when().post("/accounts")
                        .then()
                        .statusCode(201)
                        .extract()
                        .as(Account.class);
        assertThat(returnedAccount)
                .isNotNull()
                .isEqualTo(newAccount);
        Response result =
                given()
                        .when().get("/accounts")
                        .then()
                        .statusCode(200)
                        .body(
                                containsString("George Baird"),
                                containsString("Mary Taylor"),
                                containsString("Diana Rigg"),
                                containsString("Sandy Holmes")
                        )
                        .extract()
                        .response();
        List<Account> accounts = result.jsonPath().getList("$");
        assertThat(accounts).hasSize(4);

    }
}