package dev.webserver.user;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserResponse(String firstname, String lastname, String email, @JsonProperty("image_key") String imageKey) { }