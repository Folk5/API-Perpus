package com.perpustakaan.perpusapi.resource;

import com.perpustakaan.perpusapi.model.Account;
import com.perpustakaan.perpusapi.repo.AccountRepo;
import com.perpustakaan.perpusapi.service.AuthService;
import com.perpustakaan.perpusapi.utils.TokenUtil;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Path("/auth")
public class AuthResource {

    private final AccountRepo accountRepo = new AccountRepo();

    @POST
    @Path("login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(Map<String, String> credentials){
        String email = credentials.get("email");
        String password = credentials.get("password");
        AccountRepo accountRepo = new AccountRepo();
        try {
            AuthService auth = new AuthService(accountRepo);
            if (auth.login(email, password)) {
                String token = TokenUtil.generateToken(email);

                Map<String, Object> response = new HashMap<>();
                response.put("message", "Login berhasil!");
                response.put("token", token);

                return Response.ok(response).build();
            } else {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Collections.singletonMap("message", "Email atau password salah"))
                        .build();

            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Collections.singletonMap("message", "Terjadi kesalahan pada server"))
                    .build();
        }
    }

    @POST
    @Path("register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(Map<String, String> credentials){
        String email = credentials.get("email");
        String password = credentials.get("password");
        try {
            AuthService auth = new AuthService(accountRepo);
            Account account = auth.register(email, password).orElse(null);
            if (account != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Registrasi berhasil!");
                response.put("account", account);

                return Response.ok(response).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Collections.singletonMap("message", "Registrasi gagal"))
                        .build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Collections.singletonMap("message", e.getMessage()))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Collections.singletonMap("message", "Terjadi kesalahan pada server"))
                    .build();
        }
    }
}