package com.codeit.filter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditRecord {
    private Instant timestamp;
    private String username;
    private String sessionId;
    private String ipAddress;
    private String userAgent;
    private String method;
    private String uri;
    private String queryString;
    private int statusCode;
    private long duration;
    private boolean success;
}
