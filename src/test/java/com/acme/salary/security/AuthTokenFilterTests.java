package com.acme.salary.security;

import com.acme.salary.service.impl.AuthServiceImpl;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class AuthTokenFilterTests {

    private AuthServiceImpl authService;
    private AuthTokenFilter filter;
    private MockHttpServletResponse response;
    private MockFilterChain chain;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl("hr.manager", "s3cret");
        filter = new AuthTokenFilter(authService);
        response = new MockHttpServletResponse();
        chain = new MockFilterChain();
    }

    private MockHttpServletRequest request(String method, String path) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setServletPath(path);
        return request;
    }

    private void run(MockHttpServletRequest request) throws ServletException, IOException {
        filter.doFilter(request, response, chain);
    }

    @Test
    void requestWithValidTokenPassesThrough() throws Exception {
        String token = authService.login("hr.manager", "s3cret");
        MockHttpServletRequest request = request("GET", "/api/employees");
        request.addHeader("Authorization", "Bearer " + token);

        run(request);

        assertNotNull(chain.getRequest());
        assertEquals(200, response.getStatus());
    }

    @Test
    void requestWithoutTokenIsRejected() throws Exception {
        run(request("GET", "/api/employees"));

        assertNull(chain.getRequest());
        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("UNAUTHORIZED"));
    }

    @Test
    void requestWithUnknownTokenIsRejected() throws Exception {
        MockHttpServletRequest request = request("GET", "/api/employees");
        request.addHeader("Authorization", "Bearer not-a-real-token");

        run(request);

        assertNull(chain.getRequest());
        assertEquals(401, response.getStatus());
    }

    @Test
    void requestWithAnotherAuthSchemeIsRejected() throws Exception {
        MockHttpServletRequest request = request("GET", "/api/employees");
        request.addHeader("Authorization", "Basic aHI6czNjcmV0");

        run(request);

        assertNull(chain.getRequest());
        assertEquals(401, response.getStatus());
    }

    @Test
    void tokenIsRejectedAfterLogout() throws Exception {
        String token = authService.login("hr.manager", "s3cret");
        authService.logout(token);
        MockHttpServletRequest request = request("GET", "/api/employees");
        request.addHeader("Authorization", "Bearer " + token);

        run(request);

        assertNull(chain.getRequest());
        assertEquals(401, response.getStatus());
    }

    @Test
    void loginAndLogoutPathsArePublic() throws Exception {
        run(request("POST", "/api/auth/login"));

        assertNotNull(chain.getRequest());
        assertEquals(200, response.getStatus());
    }

    @Test
    void pathsOutsideApiArePublic() throws Exception {
        run(request("GET", "/swagger-ui/index.html"));

        assertNotNull(chain.getRequest());
    }

    @Test
    void corsPreflightIsNotBlocked() throws Exception {
        run(request("OPTIONS", "/api/employees"));

        assertNotNull(chain.getRequest());
    }
}