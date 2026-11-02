package com.acme.salary.security;

import com.acme.salary.service.IAuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

/**
 * Rejects requests to /api/** that do not carry a valid session token.
 * Login and logout (/api/auth/**) and everything outside /api/ stay public.
 */
@Component
@AllArgsConstructor
public class AuthTokenFilter extends OncePerRequestFilter {

    private static final String API_PREFIX = "/api/";
    private static final String PUBLIC_PREFIX = "/api/auth/";
    private static final String MESSAGE = "Missing or invalid session token. Log in at /api/auth/login";

    private final IAuthService authService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true; // CORS preflight requests carry no credentials
        }
        // The servlet path is normalised by the container, so tricks like /api/auth/../employees cannot slip through
        String path = request.getServletPath();
        return !path.startsWith(API_PREFIX) || path.startsWith(PUBLIC_PREFIX);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = BearerToken.from(request.getHeader(HttpHeaders.AUTHORIZATION));
        if (authService.isValidToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }
        writeUnauthorized(request, response);
    }

    /**
     * Filters run before Spring MVC, so GlobalExceptionHandler cannot handle this.
     * The body is written by hand to match the shape of ErrorResponseDto.
     */
    private void writeUnauthorized(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");

        String body = String.format(
                "{\"apiPath\":\"uri=%s\",\"errorCode\":\"UNAUTHORIZED\",\"errorMessage\":\"%s\",\"errorTime\":\"%s\"}",
                escape(request.getRequestURI()), MESSAGE, LocalDateTime.now());
        response.getWriter().write(body);
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}