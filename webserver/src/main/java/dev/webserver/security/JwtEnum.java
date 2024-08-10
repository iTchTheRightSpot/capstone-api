package dev.webserver.security;

public enum JwtEnum {
    FIRSTNAME("FIRSTNAME"), USER_ID("USER_ID"), CLAIMS("roles");

    private final String jwt;

    JwtEnum(final String jwt) {
        this.jwt = jwt;
    }

    public String property() {
        return jwt;
    }
}
