package transactionservice.health;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

import java.util.Date;

@ApplicationScoped
@Liveness //This is a liveness health check
public class AlwaysLiveLivenessCheck implements HealthCheck {

    @Override
    public HealthCheckResponse call() {
        return HealthCheckResponse
                .named("Always live")
                .withData("time", String.valueOf(new Date()))
                .up()
                .build();
    }
}
