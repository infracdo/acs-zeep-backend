package com.acs_tr069.test_tr069.Filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import java.io.IOException;

@Component
public class ApiAccessLogFilter extends OncePerRequestFilter {

    @Autowired
    private Environment env;

    private static final Logger logger = LoggerFactory.getLogger(ApiAccessLogFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();

        try {
            // Put context info into MDC
            MDC.put("method", request.getMethod());
            String uri = request.getRequestURI();
            String query = request.getQueryString();
            if (query != null && !query.isEmpty()) {
                uri += "?" + query;
            }
            MDC.put("uri", uri);

            String forwarded = request.getHeader("X-Forwarded-For");
            String clientIp = (forwarded != null) ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
            MDC.put("clientIp", clientIp);
            MDC.put("requestIp", request.getRemoteAddr());

            String userAgent = request.getHeader("User-Agent");
            MDC.put("userAgent", (userAgent != null) ? userAgent : "unknown");

            MDC.put("clientId", env.getProperty("client.id", "unknown-client"));
            MDC.put("userId", getUserId());
            MDC.put("username", getUsername());
            MDC.put("sessionId", getSessionId());

            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            MDC.put("durationMs", String.valueOf(duration));
            MDC.put("status", String.valueOf(response.getStatus()));

            // Log after the request is processed
            logger.info("API Access Log");

            // Clear MDC to avoid leaking info to other requests
            MDC.clear();
        }
    }

    private String getUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName();
            }
        } catch (Exception ignored) {
        }
        return "no-user-id";
    }

    private String getUsername() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth instanceof JwtAuthenticationToken) {
                JwtAuthenticationToken token = (JwtAuthenticationToken) auth;
                return token.getToken().getClaimAsString("preferred_username");
            }
        } catch (Exception ignored) {
        }
        return "anonymous";
    }

    private String getSessionId() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth instanceof JwtAuthenticationToken) {
                JwtAuthenticationToken token = (JwtAuthenticationToken) auth;
                return token.getToken().getClaimAsString("session_state");
            }
        } catch (Exception ignored) {
        }
        return "no-session-id";
    }
}
