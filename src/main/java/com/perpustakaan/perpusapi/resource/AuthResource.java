package com.perpustakaan.perpusapi.resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.perpustakaan.perpusapi.service.AccountService;

import java.util.HashMap;
import java.util.Map;

@Path("/auth")
public class AuthResource {

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(Map<String, String> request) {
        System.out.println(">>> Login endpoint HIT"); // debug
        String email = request.get("email");
        String password = request.get("password");

        if (email == null || password == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Email dan password wajib diisi"))
                    .build();
        }

        AccountService.Role role = AccountService.login(email, password);

        if (role != AccountService.Role.NONE) {
            int userId = AccountService.getAccountId(email);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login berhasil");
            response.put("user_id", userId);
            response.put("role", role.toString().toLowerCase());
            return Response.ok(response).build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("message", "Email atau password salah"))
                    .build();
        }
    }
}
