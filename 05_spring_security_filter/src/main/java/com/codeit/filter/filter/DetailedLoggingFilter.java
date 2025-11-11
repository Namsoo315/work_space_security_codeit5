package com.codeit.filter.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
public class DetailedLoggingFilter extends OncePerRequestFilter {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String traceId = shortUuid();
        MDC.put("traceId", traceId);

        ContentCachingRequestWrapper req = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper res = new ContentCachingResponseWrapper(response);

        long start = System.currentTimeMillis();

        try {
            logRequest(req, traceId);
            chain.doFilter(req, res);
        } finally {
            logResponse(req, res, start, traceId);
            res.copyBodyToResponse();
            MDC.clear();
        }
    }

    private void logRequest(ContentCachingRequestWrapper req, String traceId) {
        try {
            log.info("[REQ {}] {} {} user={} ip={}",
                    traceId,
                    req.getMethod(),
                    req.getRequestURI(),
                    currentUser(),
                    clientIp(req));

            if (isJsonWrite(req)) {
                String body = bodyString(req.getContentAsByteArray());
                if (!body.isEmpty()) {
                    log.info("[REQ_BODY {}] {}", traceId, maskSensitive(body));
                }
            }
        } catch (Exception e) {
            log.warn("request log fail: {}", e.getMessage());
        }
    }

    private void logResponse(ContentCachingRequestWrapper req,
                             ContentCachingResponseWrapper res,
                             long start,
                             String traceId) {
        try {
            long took = System.currentTimeMillis() - start;
            log.info("[RES {}] {} {} status={} took={}ms",
                    traceId,
                    req.getMethod(),
                    req.getRequestURI(),
                    res.getStatus(),
                    took);
        } catch (Exception e) {
            log.warn("response log fail: {}", e.getMessage());
        }
    }

    private String clientIp(HttpServletRequest req) {
        String xf = req.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) return xf.split(",")[0].trim();
        return req.getRemoteAddr();
    }

    private String currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        return "anonymous";
    }

    private boolean isJsonWrite(HttpServletRequest req) {
        String m = req.getMethod();
        String type = req.getContentType();
        return ("POST".equals(m) || "PUT".equals(m) || "PATCH".equals(m))
                && type != null && type.contains("application/json");
    }

    private String bodyString(byte[] content) {
        return (content == null || content.length == 0)
                ? ""
                : new String(content, StandardCharsets.UTF_8);
    }

    private String maskSensitive(String json) {
        return json
                .replaceAll("\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"***\"")
                .replaceAll("\"creditCard\"\\s*:\\s*\"[^\"]*\"", "\"creditCard\":\"***\"");
    }

    private String shortUuid() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        String path = req.getRequestURI();
        return path.startsWith("/static/")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.equals("/health");
    }
}
