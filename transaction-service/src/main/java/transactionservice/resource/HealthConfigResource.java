package transactionservice.resource;


import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import transactionservice.health.HealthConfig;

@Path("/health-config")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class HealthConfigResource {

    @Inject
    HealthConfig healthConfig;

    @GET
    public HealthConfig getHealthConfig() {
        return this.healthConfig;
    }

    @POST
    public HealthConfig setHealthConfig(HealthConfig newHealthConfig) {
        this.healthConfig.setHealty(newHealthConfig.isHealty());
        this.healthConfig.setReady(newHealthConfig.isReady());
        return this.healthConfig;
    }


}
