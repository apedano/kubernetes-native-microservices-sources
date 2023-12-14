//package quarkus;
//
//
//import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
//import com.gargoylesoftware.htmlunit.UnexpectedPage;
//import com.gargoylesoftware.htmlunit.WebClient;
//import com.gargoylesoftware.htmlunit.html.HtmlForm;
//import com.gargoylesoftware.htmlunit.html.HtmlPage;
//import io.quarkus.test.common.QuarkusTestResource;
//import io.quarkus.test.junit.QuarkusTest;
//import io.quarkus.test.oidc.server.OidcWiremockTestResource;
//import jakarta.json.bind.Jsonb;
//import jakarta.json.bind.JsonbBuilder;
//import org.junit.jupiter.api.Test;
//
//import java.util.Map;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//
///**
// * Utilizes the OidcWiremockTestResource class to simulate an OIDC authorization server like
// * Keycloak. The life cycle of the WireMock is bound to the life cycle of the BankTest.java class
// */
//@QuarkusTest
//@QuarkusTestResource(OidcWiremockTestResource.class)
//public class BankTest {
//    @Test
//    public void testGetSecretsSecure() throws Exception {
//        try (final WebClient webClient = createWebClient()) {
//            //redirect support is necessary
//            webClient.getOptions().setRedirectEnabled(true);
//            //authorization server login emulation
//            HtmlPage page =
//                    webClient.getPage("http://localhost:8081/bank/secure/secrets");
//            HtmlForm loginForm = page.getForms().get(0);
//            loginForm.getInputByName("username").setValueAttribute("admin");
//            loginForm.getInputByName("password").setValueAttribute("admin");
//            UnexpectedPage json = loginForm.getInputByValue("login").click();
//            Map<String, String> credentials;
//            try (Jsonb jsonb = JsonbBuilder.create()) {
//                credentials = jsonb.fromJson(json.getWebResponse().getContentAsString(),
//                        Map.class);
//            }
//            assertThat(credentials.get("username")).isEqualTo("admin");
//            assertThat(credentials.get("password")).isEqualTo("admin");
//        }
//    }
//
//    /*
//        Creates the HTMLUnit web client
//     */
//    private WebClient createWebClient() {
//            WebClient webClient = new WebClient();
//        webClient.setCssErrorHandler(new SilentCssErrorHandler());
//        return webClient;
//    }
//}
