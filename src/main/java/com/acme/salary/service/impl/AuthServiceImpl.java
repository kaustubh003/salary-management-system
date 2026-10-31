package com.acme.salary.service.impl;

import com.acme.salary.exception.InvalidCredentialsException;
import com.acme.salary.service.IAuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Single-user authentication for the HR manager. Credentials come from configuration
 * and session tokens are kept in memory, so they are lost when the application restarts.
 */
@Service
public class AuthServiceImpl implements IAuthService {

    private final String username;
    private final String password;
    private final Set<String> activeTokens = ConcurrentHashMap.newKeySet();

    public AuthServiceImpl(@Value("${app.auth.username}") String username,
                           @Value("${app.auth.password}") String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public String login(String username, String password) {
        // Check both before deciding, so the response does not reveal which one was wrong
        boolean usernameMatches = matches(this.username, username);
        boolean passwordMatches = matches(this.password, password);
        if (!usernameMatches || !passwordMatches) {
            throw new InvalidCredentialsException("Invalid username or password");
        }
        String token = UUID.randomUUID().toString();
        activeTokens.add(token);
        return token;
    }

    @Override
    public boolean isValidToken(String token) {
        return token != null && activeTokens.contains(token);
    }

    @Override
    public void logout(String token) {
        if (token != null) {
            activeTokens.remove(token);
        }
    }

    private static boolean matches(String expected, String actual) {
        if (actual == null) {
            return false;
        }
        // Constant-time comparison, so response time does not leak how much of the value matched
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }
}