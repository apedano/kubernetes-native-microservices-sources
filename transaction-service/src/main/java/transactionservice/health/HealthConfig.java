package transactionservice.health;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApplicationScoped
@Data
@NoArgsConstructor
public class HealthConfig {
    private boolean isHealty = true;
    private boolean isReady = true;
}
