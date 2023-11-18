package transactionservice.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import transactionservice.service.AccountService;

import java.math.BigDecimal;

@Readiness
@Slf4j
@ApplicationScoped
public class AccountHealthReadinessCheck implements HealthCheck {

    @Inject
    @RestClient
    AccountService accountService;

    @Override
    public HealthCheckResponse call() {
        log.info("AccountHealthReadinessCheck called...");
        BigDecimal balance = BigDecimal.valueOf(Integer.MIN_VALUE);
        try {
            balance = accountService.getBalance(5465L);
        } catch (WebApplicationException ex) {

            if (ex.getResponse().getStatus() >= 500) {
                log.info("AccountHealthReadinessCheck failed");
                return HealthCheckResponse
                        .named("AccountServiceCheck")
                        .withData("exception", ex.toString())
                        .down()
                        .build();
            }
        }
        log.info("AccountHealthReadinessCheck passed");
        return HealthCheckResponse
                .named("AccountServiceCheck")
                .withData("balance", balance.toString())
                .up()
                .build();
    }
}
