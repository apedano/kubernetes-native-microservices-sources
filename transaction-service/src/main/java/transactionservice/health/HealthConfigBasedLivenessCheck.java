package transactionservice.health;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

@ApplicationScoped
@Liveness
@Slf4j
public class HealthConfigBasedLivenessCheck implements HealthCheck {

    @Inject
    private HealthConfig healthConfig;

    @Override
    public HealthCheckResponse call() {
        log.info("HealthConfigBasedLivenessCheck called with config: {}", healthConfig);
        return HealthCheckResponse.named("HealthConfigBased")
                .withData("Config value", healthConfig.isHealty())
                .status(healthConfig.isHealty())
                .build();
    }
}
