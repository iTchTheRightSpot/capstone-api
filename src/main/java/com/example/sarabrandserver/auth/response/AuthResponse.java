package com.example.sarabrandserver.auth.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthResponse (@JsonProperty("principal") String principal) {}
