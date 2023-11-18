package org.acme.resource;

import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
        ResourceError resourceError = new ResourceError();
        resourceError.setExceptionType(exception.getClass().getName());
        resourceError.setCode(code);
        if (exception.getMessage() != null) {
            resourceError.setError(exception.getMessage());
        }

        return Response.status(code)
                .entity(resourceError)
                .build();
    }

    @Data
    /*
    After native compiling the class not being used by any path in the code gets deleted, this causes at runtime the following:
    org.jboss.resteasy.spi.UnhandledException: com.fasterxml.jackson.databind.exc.InvalidDefinitionException:
     *  No serializer found for class accountservice.resources.AccountResource$ResourceError and no properties discovered to create BeanSerializer (to avoid exception, disable SerializationFeature.FAIL_ON_EMPTY_BEANS). This appears to be a native image, in which case you may need to configure reflection for the class that is to be serialized
    This because, at the moment, when JSON-B or Jackson tries to get the list of fields of a class, if the class is not registered for reflection, no exception will be thrown. GraalVM will simply return an empty list of fields.
     The solution is to apply the annotation to instructs Quarkus to keep the class and its members during the native compilation
     */
    @RegisterForReflection
    @EqualsAndHashCode
    public static class ResourceError {
        private String exceptionType;
        private int code;
        private String error;

    }
}

