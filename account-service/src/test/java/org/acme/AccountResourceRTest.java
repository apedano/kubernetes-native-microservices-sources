package org.acme;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.model.Account;
import org.acme.repo.AccountRepository;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;

@QuarkusTest
@Transactional
@QuarkusTestResource(H2DatabaseTestResource.class)
//Tells Quarkus to start an H2 database prior to the tests being executed
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AccountResourceRTest {

    private final String BASE_PATH = "/accounts/repository";

    private List<Account> accountsList = Lists.
            list(
                    Account.builder().id(1L).customerNumber(11L).customerName("Customer1").accountNumber(111L).balance(BigDecimal.valueOf(1111L)).build(),
                    Account.builder().id(2L).customerNumber(22L).customerName("Customer2").accountNumber(222L).balance(BigDecimal.valueOf(2222L)).build(),
                    Account.builder().id(3L).customerNumber(33L).customerName("Customer3").accountNumber(333L).balance(BigDecimal.valueOf(3333L)).build()
            );

    @Inject
    AccountRepository accountRepositoryMock;


    @BeforeAll
    public static void setup() {
        AccountRepository accountRepositoryMock = Mockito.mock(AccountRepository.class);
        QuarkusMock.installMockForType(accountRepositoryMock, AccountRepository.class);
    }

    @Test
    @Order(1)
    void testRetrieveAllEm() {
        when(accountRepositoryMock.listAll()).thenReturn(accountsList);
        Response result =
                given()
                        .when().get(BASE_PATH)
                        .then()
                        .statusCode(200)
                        .body(
                                containsString("Customer1"),
                                containsString("Customer2"),
                                containsString("Customer3")
                        )
                        .extract()
                        .response();
        List<Account> accounts = result.jsonPath().getList("$");
        assertThat(accounts).isNotEmpty()
                .hasSameSizeAs(accountsList);
    }

    @Test
    @Order(2)
    void testGetSingle() {
        Account result = get(111L);
        assertThat(result)
                .isNotNull()
                .extracting(Account::getBalance)
                .isEqualTo(BigDecimal.valueOf(1111L));
    }

    @Test
    @Order(3)
    void testWithdraw() {
        Account account = get(222L);
        Account result =
                given()
                        .contentType("application/json")
                        .body("100")
                        .when().post(BASE_PATH + "/withdraw/" + 222L)
                        .then()
                        .statusCode(200)
                        .extract()
                        .as(Account.class);

        assertThat(result)
                .isNotNull()
                .extracting(Account::getBalance)
                .isEqualTo(BigDecimal.valueOf(2122L));
    }

//    @Test
//    @Order(4)
//    void testCreateAccountEm() {
//        accountRepositoryMock = Mockito.mock(AccountRepository.class);
//        long id = 123456L;
//        AtomicReference<Account> savedAccount = new AtomicReference<>();
//        doAnswer(invocationOnMock -> {
//            Account argument = invocationOnMock.getArgument(0);
//            argument.setId(id);
//            savedAccount.set(argument);
//            return null;
//        }).when(accountRepositoryMock).persistAndFlush(any(Account.class));
//
//        Account newAccount = new Account();
//        newAccount.setAccountNumber(666999L);
//        newAccount.setCustomerName("Ciccio Pasticcio");
//        newAccount.setCustomerNumber(7777333L);
//        newAccount.setBalance(BigDecimal.valueOf(12534.75));
//        Account result =
//                given()
//                        .contentType("application/json")
//                        .body(newAccount)
//                        .when().post(BASE_PATH)
//                        .then()
//                        .statusCode(201)
//                        .extract()
//                        .as(Account.class);
//
//        assertThat(result).isNotNull()
//                .extracting(Account::getId)
//                .isEqualTo(savedAccount.get().getId());
//    }

    private Account get(Long accountNumber) {
        when(accountRepositoryMock.findByAccountNumber(accountNumber))
                .thenReturn(accountsList.stream().filter(account -> Objects.equals(account.getAccountNumber(), accountNumber)).findFirst().orElse(null));
        return given()
                .when().get(BASE_PATH + "/" + accountNumber)
                .then()
                .statusCode(200)
                .extract()
                .as(Account.class);
    }
}