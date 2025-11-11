package com.codeit.filter.dto;

public record ApiStatsInfo(
        long totalRequests,
        long avgDuration,
        long minDuration,
        long maxDuration,
        long errorCount,
        double errorRate
) {}
