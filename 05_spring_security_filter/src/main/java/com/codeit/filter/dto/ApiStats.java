package com.codeit.filter.dto;

import java.util.concurrent.atomic.AtomicLong;

public class ApiStats {
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong totalDuration = new AtomicLong(0);
    private final AtomicLong errorCount   = new AtomicLong(0);
    private final AtomicLong minDuration  = new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong maxDuration  = new AtomicLong(0);

    public void record(long durationMs, int httpStatus) {
        totalRequests.incrementAndGet();
        totalDuration.addAndGet(durationMs);
        if (httpStatus >= 400) {
            errorCount.incrementAndGet();
        }
        updateMinMax(durationMs);
    }

    private void updateMinMax(long duration) {
        minDuration.updateAndGet(curr -> Math.min(curr, duration));
        maxDuration.updateAndGet(curr -> Math.max(curr, duration));
    }

    // 조회용 접근자
    public long totalRequests() { return totalRequests.get(); }
    public long totalDuration() { return totalDuration.get(); }
    public long errorCount()    { return errorCount.get(); }
    public long minDuration()   {
        long v = minDuration.get();
        return (v == Long.MAX_VALUE) ? 0L : v;
    }
    public long maxDuration()   { return maxDuration.get(); }
}
