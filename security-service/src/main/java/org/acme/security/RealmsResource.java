package org.acme.security;


import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/realms")
public class RealmsResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getBankRealm() {
        return "";
    }
}
