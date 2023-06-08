package com.example.sarabrandserver.util;

public class Routes {

    public String[] publicRoutes() {
        return new String[]{
                "/api/v1/auth/client/register",
                "/api/v1/auth/client/login",
                "/api/v1/auth/worker/login"
        };
    }

}
