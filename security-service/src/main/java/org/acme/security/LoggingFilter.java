package org.acme.security;

import io.quarkus.resteasy.reactive.server.runtime.QuarkusResteasyReactiveRequestContext;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.server.jaxrs.ContainerRequestContextImpl;

@Slf4j
@Provider
class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    /* Useful stuff for later development purposes.
    @Context
    UriInfo info;

    @Context
    HttpServerRequest request;
    */

    @Override
    public void filter(ContainerRequestContext requestContext) {
        log.info("Request received:" + requestContext.getMethod()  + " " + requestContext.getRequest().toString());
        log.info("Request received headers:" + requestContext.getHeaders());
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) {
        QuarkusResteasyReactiveRequestContext quarkusRequestContext = ((ContainerRequestContextImpl) requestContext).getServerRequestContext().getResteasyReactiveResourceInfo();
        log.info("Request received:" + requestContext.getMethod()  + " " + quarkusRequestContext.getPath());
        log.info("Request received headers:" + requestContext.getHeaders());
        log.info("Response returned headers:" + responseContext.getHeaders());
    }
}
