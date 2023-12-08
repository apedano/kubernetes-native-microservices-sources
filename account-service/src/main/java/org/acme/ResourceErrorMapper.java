package org.acme;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Provider //indicates the class is an autodiscovered JAX-RS Provider
public class ResourceErrorMapper implements ExceptionMapper<Exception> { //Implements ExceptionMapper for all Exception types
    @Override
    public Response toResponse(Exception exception) {
        int code = 500;
        if (exception instanceof WebApplicationException) {
            code = ((WebApplicationException) exception)
                    .getResponse().getStatus();
        }
        ErrorResponse errorResponse = ErrorResponse.of(
                exception.getClass().getName(),
                code,
                Optional.ofNullable(exception.getMessage()).orElse("")
        );
        return Response.status(code)
                .entity(errorResponse)
                .build();
    }

    @RegisterForReflection
    @Data
    @NoArgsConstructor
    @AllArgsConstructor(staticName = "of")
    public static class ErrorResponse {
        String exceptionType;
        int code;
        String error;
    }
}