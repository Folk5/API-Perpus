package com.perpustakaan.perpusapi.middleware;

import com.perpustakaan.perpusapi.utils.TokenUtil;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.core.Response;
import java.io.IOException;

@Provider
@Auth
@Priority(Priorities.AUTHENTICATION)
public class AuthFilter implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();

        // Bypass login & register
        if (path.startsWith("auth/login") || path.startsWith("auth/register")) {
            return;
        }

        String authHeader = requestContext.getHeaderString("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("Token Required").build());
            return;
        }

        String token = authHeader.substring("Bearer ".length());
        if (!TokenUtil.verifyToken(token)) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("Invalid Token").build());
        }
    }
}

