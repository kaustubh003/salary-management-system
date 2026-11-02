package com.acme.salary.security;

public final class BearerToken {

    private static final String PREFIX = "Bearer ";

    private BearerToken() {
    }

    /**
     * @param authorizationHeader the raw Authorization header, may be null
     * @return the token after "Bearer ", or null if the header is missing or uses another scheme.
     */
    public static String from(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith(PREFIX)) {
            String token = authorizationHeader.substring(PREFIX.length()).trim();
            return token.isEmpty() ? null : token;
        }
        return null;
    }
}