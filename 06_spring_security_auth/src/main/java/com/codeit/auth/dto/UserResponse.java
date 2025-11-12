package com.codeit.auth.dto;

public record UserResponse(Long id, String username, String role, boolean enabled) {}
