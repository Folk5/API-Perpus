package com.perpustakaan.perpusapi.resource;

import com.perpustakaan.perpusapi.config.DatabaseConfig;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.sql.Connection;

@Path("/database")
public class DatabaseResource {

    @GET
    @Path("/check")
    @Produces(MediaType.APPLICATION_JSON)
    public String checkDatabaseConnection() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            if (conn != null) {
                return "{ \"status\": \"success\", \"message\": \"Database connected successfully!\" }";
            } else {
                return "{ \"status\": \"error\", \"message\": \"Database connection failed!\" }";
            }
        } catch (Exception e) {
            return "{ \"status\": \"error\", \"message\": \"" + e.getMessage() + "\" }";
        }
    }
}