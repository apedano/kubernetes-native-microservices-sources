package org.acme.resource;

import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

@Provider //indicates the class is an autodiscovered JAX-RS Provider
@Slf4j
public class ResourceErrorMapper implements ExceptionMapper<Exception> { //Implements ExceptionMapper for all Exception types
    @Override
    public Response toResponse(Exception exception) {
        int code = 500;
        if (exception instanceof WebApplicationException) {
            log.info("Managing WebApplicationException:{} - code {}", exception.getMessage(), ((WebApplicationException) exception).getResponse().getStatus());
            code = ((WebApplicationException) exception)
                    .getResponse().getStatus();
        }
        JsonObjectBuilder entityBuilder = Json.createObjectBuilder()
                .add("exceptionType", exception.getClass().getName())
                .add("code", code);
        if (exception.getMessage() != null) {
            entityBuilder.add("error", exception.getMessage());
        }
        return Response.status(code)
                .entity(entityBuilder.build())
                .build();
    }
}