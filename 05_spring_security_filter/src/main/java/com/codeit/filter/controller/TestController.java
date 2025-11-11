package com.codeit.filter.controller;

import com.codeit.filter.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestController {

    private final AuditService auditService;

    @GetMapping("/fast")
    public Map<String, Object> fastEndpoint() {
        return Map.of(
                "message", "Fast response",
                "timestamp", System.currentTimeMillis(),
                "auditRecords", auditService.findAll()
        );
    }

    @GetMapping("/slow")
    public Map<String, String> slowEndpoint() throws InterruptedException {
        // 의도적으로 느린 응답 (2초 지연)
        Thread.sleep(2000);
        return Map.of("message", "Slow response", "timestamp", String.valueOf(System.currentTimeMillis()));
    }

    @GetMapping("/critical")
    public Map<String, String> criticalEndpoint() throws InterruptedException {
        // 의도적으로 매우 느린 응답 (6초 지연)
        Thread.sleep(6000);
        return Map.of("message", "Critical response", "timestamp", String.valueOf(System.currentTimeMillis()));
    }

    @PostMapping("/data")
    public Map<String, Object> postData(@RequestBody Map<String, Object> data) {
        return Map.of(
                "received", data,
                "timestamp", System.currentTimeMillis(),
                "status", "processed"
        );
    }

    @GetMapping("/error")
    public Map<String, String> errorEndpoint() {
        throw new RuntimeException("Intentional error for testing");
    }
}