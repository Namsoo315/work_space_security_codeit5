package com.codeit.filter.controller;

import com.codeit.filter.dto.AuditRecord;
import com.codeit.filter.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AuditService auditService;

    @GetMapping("/audit")
    public Map<String, Object> getAuditRecords() {
        List<AuditRecord> records = auditService.findAll();
        return Map.of(
                "totalRecords", records.size(),
                "records", records
        );
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        return Map.of(
                "message", "Admin dashboard",
                "auditRecords", auditService.count(),
                "timestamp", System.currentTimeMillis()
        );
    }
}