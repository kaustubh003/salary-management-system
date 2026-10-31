package com.acme.salary.service.impl;

import com.acme.salary.exception.InvalidCredentialsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceImplTests {

    private static final String USERNAME = "hr.manager";
    private static final String PASSWORD = "s3cret";

    private AuthServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AuthServiceImpl(USERNAME, PASSWORD);
    }

    @Test
    void loginReturnsTokenForCorrectCredentials() {
        String token = service.login(USERNAME, PASSWORD);

        assertFalse(token.isBlank());
    }

    @Test
    void loginRejectsWrongPassword() {
        assertThrows(InvalidCredentialsException.class, () -> service.login(USERNAME, "wrong"));
    }

    @Test
    void loginRejectsWrongUsername() {
        assertThrows(InvalidCredentialsException.class, () -> service.login("someone.else", PASSWORD));
    }

    @Test
    void loginRejectsMissingCredentials() {
        assertThrows(InvalidCredentialsException.class, () -> service.login(null, null));
    }

    @Test
    void eachLoginIssuesANewToken() {
        String first = service.login(USERNAME, PASSWORD);
        String second = service.login(USERNAME, PASSWORD);

        assertNotEquals(first, second);
    }

    @Test
    void tokenIsValidAfterLogin() {
        String token = service.login(USERNAME, PASSWORD);

        assertTrue(service.isValidToken(token));
    }

    @Test
    void unknownOrMissingTokenIsNotValid() {
        assertFalse(service.isValidToken("not-a-real-token"));
        assertFalse(service.isValidToken(null));
    }

    @Test
    void logoutInvalidatesTheToken() {
        String token = service.login(USERNAME, PASSWORD);

        service.logout(token);

        assertFalse(service.isValidToken(token));
    }

    @Test
    void logoutWithUnknownTokenDoesNothing() {
        assertDoesNotThrow(() -> service.logout("not-a-real-token"));
        assertDoesNotThrow(() -> service.logout(null));
    }
}