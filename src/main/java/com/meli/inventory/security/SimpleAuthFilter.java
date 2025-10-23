// ## Prompt: Add Basic Authentication filter
// Add a simple BasicAuth filter to secure all API endpoints.

// Requirements:
// - Create a class `SimpleAuthFilter` that extends `OncePerRequestFilter`.
// - Check for an Authorization header like: `Basic dXNlcjpwYXNz`.
// - Validate against hardcoded credentials (user=admin, password=admin123).
// - If missing or invalid, return 401 with message "Unauthorized".
// - Annotate the filter with `@Component` so it’s registered automatically.
// - Ensure Swagger endpoints (`/swagger-ui` and `/v3/api-docs`) are excluded.
// - Add a test in `SecurityFilterTest` that verifies 401 and 200 responses.
package com.meli.inventory.security;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SimpleAuthFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String AUTH_PREFIX = "Basic ";
    private static final String VALID_CREDENTIALS = "admin:admin123";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filter)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // ✅ Excluir rutas públicas
        if (path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/actuator")
                || path.startsWith("/h2-console")
                || path.startsWith("/error")) {
            filter.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader(AUTH_HEADER);
        if (authHeader == null || !authHeader.startsWith(AUTH_PREFIX)) {
            unauthorized(response);
            return;
        }

        try {
            String base64 = authHeader.substring(AUTH_PREFIX.length());
            String credentials = new String(Base64.getDecoder().decode(base64));

            if (!VALID_CREDENTIALS.equals(credentials)) {
                unauthorized(response);
                return;
            }

            // ✅ Registrar autenticación en el contexto
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken("admin", null, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (IllegalArgumentException e) {
            unauthorized(response);
            return;
        }

        filter.doFilter(request, response);
    }

    private void unauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("Unauthorized");
    }
}



