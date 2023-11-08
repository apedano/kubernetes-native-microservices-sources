package quarkus.model;

import lombok.Data;
import org.eclipse.microprofile.config.inject.ConfigProperties;

@Data
@ConfigProperties(prefix="bank-support")
public class BankSupportConfig {
    private String email;
    private String phone;
}
