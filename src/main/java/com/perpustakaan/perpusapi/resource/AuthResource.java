package com.perpustakaan.perpusapi.resource;

import com.perpustakaan.perpusapi.service.AuthService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Optional;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {
    @Inject
    private AuthService authService;

    // ✅ Login
    @POST
    @Path("/login")
    public Response login(LoginRequest request) {
        Optional<String> token = authService.login(request.email, request.password);
        if (token.isPresent()) {
            return Response.ok(new TokenResponse(token.get())).build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).entity("Login gagal").build();
    }

    // ✅ Register
    @POST
    @Path("/register")
    public Response register(LoginRequest request) {
        Optional<String> token = authService.register(request.email, request.password);
        if (token.isPresent()) {
            return Response.ok(new TokenResponse(token.get())).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).entity("Registrasi gagal").build();
    }

    public static class LoginRequest {
        public String email;
        public String password;
    }

    public static class TokenResponse {
        public String token;

        public TokenResponse(String token) {
            this.token = token;
        }
    }
}
