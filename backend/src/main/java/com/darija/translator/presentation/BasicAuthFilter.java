package com.darija.translator.presentation;

import jakarta.annotation.Priority;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import jakarta.security.auth.message.AuthException;
import jakarta.security.auth.message.AuthStatus;
import jakarta.security.auth.message.MessageInfo;
import jakarta.security.auth.message.MessagePolicy;
import jakarta.security.auth.message.callback.CallerPrincipalCallback;
import jakarta.security.auth.message.module.ServerAuthModule;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Base64;
import java.util.Map;

/**
 * Security component implementing HTTP Basic Authentication in compliance with
 * the Jakarta Authentication Specification (JASPIC ServerAuthModule) and
 * Jakarta RESTful Web Services ContainerRequestFilter.
 *
 * Default Credentials:
 *   Username: admin
 *   Password: password123 (or darija123)
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class BasicAuthFilter implements ContainerRequestFilter, ServerAuthModule {

    private static final String REALM = "DarijaTranslatorAPI";
    private static final String BASIC_PREFIX = "Basic ";

    private final String expectedUsername;
    private final String expectedPassword;
    private CallbackHandler handler;

    public BasicAuthFilter() {
        this.expectedUsername = System.getenv().getOrDefault("API_USERNAME", "admin");
        this.expectedPassword = System.getenv().getOrDefault("API_PASSWORD", "password123");
    }

    // =========================================================================
    // Jakarta RESTful Web Services Filter (Active in Embedded Grizzly Runtime)
    // =========================================================================
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // Allow CORS preflight OPTIONS requests without credentials
        if ("OPTIONS".equalsIgnoreCase(requestContext.getMethod())) {
            return;
        }

        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        String authenticatedUser = validateBasicAuthHeader(authHeader);

        if (authenticatedUser == null) {
            abortWithUnauthorized(requestContext);
            return;
        }

        final SecurityContext currentSecurityContext = requestContext.getSecurityContext();
        requestContext.setSecurityContext(new SecurityContext() {
            @Override
            public Principal getUserPrincipal() {
                return () -> authenticatedUser;
            }

            @Override
            public boolean isUserInRole(String role) {
                return "USER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);
            }

            @Override
            public boolean isSecure() {
                return currentSecurityContext != null && currentSecurityContext.isSecure();
            }

            @Override
            public String getAuthenticationScheme() {
                return SecurityContext.BASIC_AUTH;
            }
        });
    }

    // =========================================================================
    // Jakarta Authentication Specification (ServerAuthModule SPI Implementation)
    // =========================================================================
    @Override
    public void initialize(MessagePolicy requestPolicy, MessagePolicy responsePolicy,
                           CallbackHandler handler, Map<String, Object> options) throws AuthException {
        this.handler = handler;
    }

    @Override
    public Class<?>[] getSupportedMessageTypes() {
        return new Class<?>[]{Object.class};
    }

    @Override
    public AuthStatus validateRequest(MessageInfo messageInfo, Subject clientSubject,
                                      Subject serviceSubject) throws AuthException {
        Object authObj = messageInfo.getMap().get(HttpHeaders.AUTHORIZATION);
        String authHeader = authObj != null ? authObj.toString() : null;
        String username = validateBasicAuthHeader(authHeader);

        if (username != null) {
            if (handler != null) {
                try {
                    handler.handle(new Callback[]{new CallerPrincipalCallback(clientSubject, username)});
                } catch (Exception e) {
                    throw new AuthException("Failed to register caller principal: " + e.getMessage());
                }
            }
            return AuthStatus.SUCCESS;
        }
        return AuthStatus.SEND_FAILURE;
    }

    @Override
    public AuthStatus secureResponse(MessageInfo messageInfo, Subject serviceSubject) {
        return AuthStatus.SEND_SUCCESS;
    }

    @Override
    public void cleanSubject(MessageInfo messageInfo, Subject subject) {
        if (subject != null) {
            subject.getPrincipals().clear();
        }
    }

    // =========================================================================
    // Credential Verification Helper
    // =========================================================================
    private String validateBasicAuthHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith(BASIC_PREFIX)) {
            return null;
        }

        try {
            String base64Token = authHeader.substring(BASIC_PREFIX.length()).trim();
            byte[] decodedBytes = Base64.getDecoder().decode(base64Token);
            String credentials = new String(decodedBytes, StandardCharsets.UTF_8);

            int colonIndex = credentials.indexOf(':');
            if (colonIndex <= 0) {
                return null;
            }

            String username = credentials.substring(0, colonIndex);
            String password = credentials.substring(colonIndex + 1);

            boolean validUser = expectedUsername.equals(username);
            boolean validPass = expectedPassword.equals(password) || "darija123".equals(password);

            return (validUser && validPass) ? username : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private void abortWithUnauthorized(ContainerRequestContext requestContext) {
        String errorJson = "{\"error\":\"Unauthorized\",\"message\":\"Valid HTTP Basic Authentication credentials are required (e.g. admin:password123).\"}";
        requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"" + REALM + "\"")
                        .type(MediaType.APPLICATION_JSON)
                        .entity(errorJson)
                        .build()
        );
    }
}

