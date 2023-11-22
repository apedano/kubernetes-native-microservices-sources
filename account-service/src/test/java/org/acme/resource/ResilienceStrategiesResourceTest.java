package org.acme.resource;

import io.quarkus.test.junit.QuarkusTest;

import io.restassured.response.Response;
import jakarta.ws.rs.core.Response.Status;
import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.IntStream;


import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.Response.Status.*;
import static java.lang.Thread.sleep;
import static org.assertj.core.api.Assertions.assertThat;


@QuarkusTest
@Slf4j
public class ResilienceStrategiesResourceTest {
    @Test
    void bulkheadTest() {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        List<CompletableFuture<Response>> completableFutures = List.of(
                CompletableFuture.supplyAsync(responseSupplier(1), executor),
                CompletableFuture.supplyAsync(responseSupplier(2), executor),
                CompletableFuture.supplyAsync(responseSupplier(3), executor)
        );
        List<Response> list = completableFutures.stream().map(CompletableFuture::join).toList();
        log.info("All futures joined");
        assertThat(list)
                .isNotEmpty()
                .hasSize(3)
                .map(response -> Status.fromStatusCode(response.getStatusCode()))
                .containsExactlyInAnyOrder(Status.OK, Status.OK, Status.TOO_MANY_REQUESTS);
    }

    private Supplier<Response> responseSupplier(int counter) {
        return () -> {
            log.info("Executing call #{} on thread [{}]", counter, Thread.currentThread().getName());
            return given()
                    .contentType("application/json")
                    .when()
                    .get("resilience/do-delivery-bulkhead")
                    .then()
                    .extract()
                    .response();
        };
    }

    @Test
    void timeoutTest() {
        given()
                .contentType("application/json")
                .when()
                .get("resilience/call-with-timeout")
                .then()
                .statusCode(GATEWAY_TIMEOUT.getStatusCode());
    }

    @Test
    void retryTest() {
        //the first two timeouts triggered by the service are transparent, the response
        //will be delayed until the number of retries is completed (in that case the Timeout exception will be thrown)
        //or service call is successful
        given()
                .contentType("application/json")
                .when()
                .get("resilience/call-retry")
                .then()
                .statusCode(OK.getStatusCode());
    }

    @Test
    void circuitBreakerTest() throws InterruptedException {
        //success first
        given()
                .contentType("application/json")
                .when()
                .get("resilience/call-circuit-breaker")
                .then()
                .statusCode(OK.getStatusCode());
        //first failure for three times
        Response response;
        int counter = 1;
        do {
            sleep(1000);
            log.info("Checking circuit breaker non ok #{}", counter++);
            response = given()
                    .contentType("application/json")
                    .when()
                    .get("resilience/call-circuit-breaker")
                    .then()
                    .extract().response();
            log.info("Checking open circuit breaker response {}", response.getStatusCode());
        } while (response.getStatusCode() != OK.getStatusCode() && counter <20);
        //service operational again
        assertThat(response.getStatusCode()).isEqualTo(OK.getStatusCode());


        }

    private Supplier<Response> cBResponseSupplier(int counter) {
        return () -> {
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            log.info("Executing call #{} on thread [{}]", counter, Thread.currentThread().getName());
            return given()
                    .contentType("application/json")
                    .when()
                    .get("resilience/call-circuit-breaker")
                    .then()
                    .extract()
                    .response();
        };
    }


}
