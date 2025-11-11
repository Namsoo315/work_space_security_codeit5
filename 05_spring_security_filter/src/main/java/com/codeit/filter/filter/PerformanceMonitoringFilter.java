package com.codeit.filter.filter;

import com.codeit.filter.dto.ApiStats;
import com.codeit.filter.dto.ApiStatsInfo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class PerformanceMonitoringFilter extends OncePerRequestFilter {

    private final ConcurrentHashMap<String, ApiStats> apiStatsMap = new ConcurrentHashMap<>();

    private static final long slowRequestThreshold     = 1000;
    private static final long criticalRequestThreshold = 5000;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        String apiPath = getApiPath(request);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            updateApiStats(apiPath, duration, response.getStatus());
            monitorPerformance(request, duration);
        }
    }

    private void updateApiStats(String apiPath, long duration, int statusCode) {
        ApiStats stats = apiStatsMap.computeIfAbsent(apiPath, k -> new ApiStats());
        stats.record(duration, statusCode);
    }

    private void monitorPerformance(HttpServletRequest request, long duration) {
        if (duration >= criticalRequestThreshold) {
            log.error("CRITICAL_SLOW_REQUEST: {} {} took {}ms (threshold: {}ms)",
                    request.getMethod(), request.getRequestURI(), duration, criticalRequestThreshold);
        } else if (duration >= slowRequestThreshold) {
            log.warn("SLOW_REQUEST: {} {} took {}ms (threshold: {}ms)",
                    request.getMethod(), request.getRequestURI(), duration, slowRequestThreshold);
        }

        if (log.isDebugEnabled()) {
            log.debug("REQUEST_TIMING: {} {} - {}ms",
                    request.getMethod(), request.getRequestURI(), duration);
        }
    }

    private String getApiPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String method = request.getMethod();

        // 동적 파라미터 패턴화: 숫자 id, UUID(대소문자 무시)
        uri = uri.replaceAll("/\\d+", "/{id}");
        uri = uri.replaceAll("/(?i)[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}", "/{uuid}");

        return method + " " + uri;
    }

    public Map<String, ApiStatsInfo> getApiStats() {
        Map<String, ApiStatsInfo> result = new HashMap<>();

        apiStatsMap.forEach((path, stats) -> {
            long total = stats.totalRequests();
            if (total > 0) {
                long avg = stats.totalDuration() / total;
                long min = stats.minDuration();
                long max = stats.maxDuration();
                long err = stats.errorCount();
                double rate = (double) err / total * 100.0;

                result.put(path, new ApiStatsInfo(total, avg, min, max, err, rate));
            }
        });

        return result;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/static/")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.equals("/health")
                || path.equals("/favicon.ico");
    }
}
