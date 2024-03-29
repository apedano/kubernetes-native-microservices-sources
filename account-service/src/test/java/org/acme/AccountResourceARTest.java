//package org.acme;
//
//import io.quarkus.test.common.QuarkusTestResource;
//import io.quarkus.test.h2.H2DatabaseTestResource;
//import io.quarkus.test.junit.QuarkusTest;
//import io.restassured.response.Response;
//import jakarta.transaction.Transactional;
//import org.acme.model.Account;
//import org.acme.model.AccountAr;
//import org.junit.jupiter.api.MethodOrderer;
//import org.junit.jupiter.api.Order;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.TestMethodOrder;
//
//import java.math.BigDecimal;
//import java.util.List;
//
//import static io.restassured.RestAssured.given;
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.hamcrest.Matchers.containsString;
//
//@QuarkusTest
//@Transactional
//@QuarkusTestResource(H2DatabaseTestResource.class)
////Tells Quarkus to start an H2 database prior to the tests being executed
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//public class AccountResourceARTest {
//
//    private final String BASE_PATH = "/accounts/active-record";
//    @Test
//    @Order(1)
//    void testRetrieveAllEm() {
//        Response result =
//                given()
//                        .when().get(BASE_PATH)
//                        .then()
//                        .statusCode(200)
//                        .body(
//                                containsString("Debbie Hall"),
//                                containsString("David Tennant"),
//                                containsString("Alex Kingston")
//                        )
//                        .extract()
//                        .response();
//        List<Account> accounts = result.jsonPath().getList("$");
//        assertThat(accounts).isNotEmpty()
//                .hasSize(8);
//    }
//
//    @Test
//    @Order(2)
//    void testGetSingle() {
//        AccountAr result = get();
//
//        assertThat(result)
//                .isNotNull()
//                .extracting("accountNumber")
//                .isEqualTo(78790L);
//    }
//
//    @Test
//    @Order(3)
//    void testWithdraw() {
//        Account result =
//                given()
//                        .contentType("application/json")
//                        .body("100")
//                        .when().put(BASE_PATH + "/78790/withdraw")
//                        .then()
//                        .statusCode(200)
//                        .extract()
//                        .as(Account.class);
//
//        assertThat(result)
//                .isNotNull()
//                .extracting(Account::getBalance)
//                .isEqualTo(BigDecimal.valueOf(339.01));
//    }
//
//    @Test
//    @Order(4)
//    void testWithdrawWithOverdrawnException() {
//        AccountAr account = get();
//
//        BigDecimal withdrawForOverdrawn = account.overdraftLimit.abs().add(account.balance).add(BigDecimal.ONE);
//
//
//        Account response = given()
//                .contentType("application/json")
//                .body(withdrawForOverdrawn.toString())
//                .when().put(BASE_PATH + "/" + account.accountNumber + "/withdraw")
//                .then()
//                .statusCode(200)
//                .extract()
//                .as(Account.class);
//
//        //now we have overdrawn exception
//
//        given()
//                .contentType("application/json")
//                .body("1")
//                .when().put(BASE_PATH + "/" + account.accountNumber + "/withdraw")
//                .then()
//                .statusCode(jakarta.ws.rs.core.Response.Status.PRECONDITION_FAILED.getStatusCode())
//                .extract();
//
//
//    }
//
//    @Test
//    @Order(5)
//    void testCreateAccountEm() {
//        AccountAr newAccount = new AccountAr();
//        newAccount.accountNumber = 666999L;
//        newAccount.customerName = "Ciccio Pasticcio";
//        newAccount.customerNumber = 7777333L;
//        newAccount.balance = BigDecimal.valueOf(12534.75);
//        AccountAr result =
//                given()
//                        .contentType("application/json")
//                        .body(newAccount)
//                        .when().post(BASE_PATH)
//                        .then()
//                        .statusCode(201)
//                        .extract()
//                        .as(AccountAr.class);
//
//        assertThat(result).isNotNull();
//    }
//
//    private AccountAr get() {
//        return given()
//                .when().get(BASE_PATH + "/78790")
//                .then()
//                .statusCode(200)
//                .body(
//                        containsString("Vanna White")
//                )
//                .extract()
//                .as(AccountAr.class);
//    }
//}