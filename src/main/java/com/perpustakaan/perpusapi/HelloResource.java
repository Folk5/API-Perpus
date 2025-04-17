package com.perpustakaan.perpusapi;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

@Path("/")
public class HelloResource {
    @GET
    @Produces("text/plain")
    public String hello() {
        return "Server Tomcat Berhasil Berjalan";
    }
}