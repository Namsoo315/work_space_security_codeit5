package com.codeit.filter.filter;

import com.codeit.filter.dto.AuditRecord;
import com.codeit.filter.service.AuditService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class AuditFilter extends OncePerRequestFilter {

    private static final List<String> INCLUDE_PREFIXES = List.of("/api/", "/admin/", "/secure/");
    private static final List<String> EXCLUDE_PREFIXES = List.of("/public/", "/static/");
    private static final List<String> EXCLUDE_EXACT = List.of("/health");

    private final AuditService auditService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        final String path = request.getRequestURI();
        // 명시적 제외 우선
        if (startsWithAny(path, EXCLUDE_PREFIXES)) return true;
        if (EXCLUDE_EXACT.contains(path)) return true;
        // 포함 목록에 없으면 필터 미적용
        return !startsWithAny(path, INCLUDE_PREFIXES);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        final Instant startedAt = Instant.now();
        final String sessionId = safeSessionId(request);

        try {
            chain.doFilter(request, response);
        } finally {
            final AuditRecord record = buildAuditRecord(request, response, sessionId, startedAt);
            try {
                auditService.save(record);
            } catch (Exception e) {
                // 감사 기록 실패는 본 요청 흐름에 영향 주지 않음
                log.error("Audit save failed: {}", e.getMessage(), e);
            }
        }
    }

    private static boolean startsWithAny(String path, List<String> prefixes) {
        for (String p : prefixes) if (path.startsWith(p)) return true;
        return false;
    }

    private static String safeSessionId(HttpServletRequest request) {
        // 세션이 없으면 생성하지 않도록 false
        HttpSession session = request.getSession(false);
        return session != null ? session.getId() : "no-session";
    }

    private AuditRecord buildAuditRecord(HttpServletRequest req,
                                         HttpServletResponse res,
                                         String sessionId,
                                         Instant startedAt) {

        final String username = resolveUsername();
        final long durationMs = Duration.between(startedAt, Instant.now()).toMillis();

        return AuditRecord.builder()
                .timestamp(Instant.now())
                .username(username)
                .sessionId(sessionId)
                .ipAddress(clientIp(req))
                .userAgent(req.getHeader("User-Agent"))
                .method(req.getMethod())
                .uri(req.getRequestURI())
                .queryString(req.getQueryString())
                .statusCode(res.getStatus())
                .duration(durationMs)
                .success(res.getStatus() < 400)
                .build();
    }

    private static String resolveUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (log.isDebugEnabled()) {
            log.debug("Auth: {}", auth);
        }
        if (auth != null && auth.isAuthenticated()) {
            String name = auth.getName();
            if (name != null && !"anonymousUser".equals(name)) return name;
        }
        return "anonymous";
    }

    private static String clientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) return xff.split(",")[0].trim();
        return req.getRemoteAddr();
    }
}
