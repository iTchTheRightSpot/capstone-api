package com.example.sarabrandserver.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthResponse (@JsonProperty("principal") String principal) {}
