package com.emmanuel.sarabrandserver.jwt;

public record JwtUserStatus(String principal, boolean isTokenValid) { }
