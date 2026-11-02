package com.acme.salary.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BearerTokenTests {

    @Test
    void extractsTheTokenAfterTheBearerPrefix() {
        assertEquals("abc-123", BearerToken.from("Bearer abc-123"));
    }

    @Test
    void trimsSurroundingSpaces() {
        assertEquals("abc-123", BearerToken.from("Bearer  abc-123 "));
    }

    @Test
    void returnsNullForMissingOrEmptyHeader() {
        assertNull(BearerToken.from(null));
        assertNull(BearerToken.from(""));
        assertNull(BearerToken.from("Bearer "));
    }

    @Test
    void returnsNullForAnotherScheme() {
        assertNull(BearerToken.from("Basic abc-123"));
    }
}