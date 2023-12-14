package quarkus.service;


import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperties;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import quarkus.model.BankSupportConfig;
import quarkus.model.BankSupportConfigMapping;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Path("/bank")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BankResource {

    //CDI injection based configuration retrieval. The @Inject annotation is not required to semplify the code
    //Single property extraction
    @ConfigProperty(name = "bank.name", defaultValue = "Default bank name from @ConfigProperty")
    String name;

    //In case of missing configuration Optional.empty() will be injected
    @ConfigProperty(name = "app.mobilebanking")
    Optional<Boolean> mobileBanking;

    //Microprofile API
    @ConfigProperties(prefix="bank-support")
    BankSupportConfig supportConfig;

    @Inject //from Quarkus ConfigMapping class
    BankSupportConfigMapping bankSupportConfigMapping;

    //from secret
    @ConfigProperty(name="username")
    String username;
    //from secret
    @ConfigProperty(name="password")
    String password;

    @RolesAllowed("bankadmin")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/secure/secrets")
    public Map<String, String> secureGetSecrets() {
        return getSecrets();
    }

    @GET
    @Path("/name")
    @Produces(MediaType.TEXT_PLAIN)
    public String getName() {
        return name;
    }

    @GET
    @Path("/name-programmatically")
    @Produces(MediaType.TEXT_PLAIN)
    public String getNameProgrammatically() {
        //Programmatic access to config
        Config config = ConfigProvider.getConfig();
        return config.getValue("bank.name", String.class);
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/mobilebanking")
    public Boolean getMobileBanking() {
        return mobileBanking.orElse(false);
    }

    @GET
    @Path("/support")
    public HashMap<String, String> getSupport() {
        HashMap<String,String> map = new HashMap<>();
        map.put("email", supportConfig.getEmail());
        map.put("phone", supportConfig.getPhone());
        return map;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/supportmapping")
    public Map<String, String> getSupportMapping() {
        HashMap<String,String> map = getSupport();
        map.put("business.email", bankSupportConfigMapping.business().email());
        map.put("business.phone", bankSupportConfigMapping.business().phone());
        return map;
    }
    @GET
    @Path("/secrets")
    public Map<String, String> getSecrets() {
        HashMap<String,String> map = new HashMap<>();
        map.put("username", username);
        map.put("password", password);
        return map;
    }
}
