package quarkus.model;

import io.smallrye.config.ConfigMapping;
import jakarta.validation.constraints.Size;

// Quarkus specific configuration annotation, more evolved than MicroProfile ConfigProperties
// config mapping applies to interfaces only
@ConfigMapping(prefix = "bank-support-mapping")
public interface BankSupportConfigMapping {
    //Unlike MicroProfile Config @ConfigProperties,
    // @ConfigMapping properties can be validated using
    // Bean Validation constraints.
    @Size(min=12, max=12)
    String phone();

    //Properties are defined as method names
    String email();

    Business business();
    interface Business {
        @Size(min=12, max=12)
        String phone();
        String email();
    }
}
