package com.perpustakaan.perpusapi.middleware;

import com.perpustakaan.perpusapi.utils.TokenUtil;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
@AuthRequired
@Priority(Priorities.AUTHENTICATION)
public class AuthFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Ambil token dari header Authorization
        String authHeader = requestContext.getHeaderString("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Token tidak ditemukan atau salah format!")
                    .build());
            return;
        }

        // Ambil token setelah "Bearer "
        String token = authHeader.substring("Bearer ".length());

        if (!TokenUtil.verifyToken(token)) {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Token tidak valid atau sudah expired!")
                    .build());
        }
    }
}


