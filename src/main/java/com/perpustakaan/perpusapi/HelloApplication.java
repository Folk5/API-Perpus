package com.perpustakaan.perpusapi;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import java.util.Set;
import java.util.HashSet;

import com.perpustakaan.perpusapi.resource.AuthResource;

@ApplicationPath("/api")
public class HelloApplication extends Application {

//    @Override
//    public Set<Class<?>> getClasses() {
//        Set<Class<?>> resources = new HashSet<>();
//        resources.add(AuthResource.class);
//        return resources;
//    }
}
