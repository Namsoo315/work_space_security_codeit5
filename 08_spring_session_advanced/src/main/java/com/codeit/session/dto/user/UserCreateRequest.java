package com.codeit.session.dto.user;

import lombok.Builder;
import lombok.Data;

@Builder
public record UserCreateRequest(
        String username,
        String email,
        String password,
        String role   // null이면 USER
) {}
