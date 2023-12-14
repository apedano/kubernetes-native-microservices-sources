package transactionservice.resource;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;
import transactionservice.config.WiremockAccountService;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
@QuarkusTestResource(WiremockAccountService.class)
//@TestSecurity defines a user, duke, in the customer role.
// Because it is applied to the TestSecurity class, it will be applied to all test
// methods in the class. This duke user applies only when running
// tests, whereas the embedded duke user defined in application.properties applies only during
// development.
@TestSecurity(user = "duke", roles = {"customer"})
public class SecurityTest {
    @Test
    public void built_in_security() {
        given()
                .when()
                .get("/transactions/config-secure/{acctNumber}/balance", 121212)
                .then()
                .statusCode(200)
                .body(containsString("435.76"));
    }
}

