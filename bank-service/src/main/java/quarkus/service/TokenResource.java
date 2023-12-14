package quarkus.service;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A Quarkus-specific annotation that allows access only to an authenticated user. This annotation, and
 * any protective security annotation, will trigger the Authorization Code Flow and token creation for a
 * successful authentication.
 */
@Authenticated
@Path("/token")
public class TokenResource {
    /**
     * Injection point for the Access Token issued
     * by the OpenID Connect Provider
     */
    @Inject
    JsonWebToken accessToken;

    @GET
    @Path("/tokeninfo")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<String> token() {
        return accessToken.getClaimNames().stream()
                .map(claimName -> claimName + " = " + accessToken.getClaim(claimName))
                .collect(Collectors.toSet());
    }
}
