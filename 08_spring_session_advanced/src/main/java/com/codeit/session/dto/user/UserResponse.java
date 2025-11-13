package com.codeit.session.dto.user;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Builder
public record UserResponse(
        Long id,
        String username,
        String email,
        String role,
        Instant createdAt,
        Instant updatedAt
) {}