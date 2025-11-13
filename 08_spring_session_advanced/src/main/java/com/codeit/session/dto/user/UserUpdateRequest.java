package com.codeit.session.dto.user;


public record UserUpdateRequest(
        String email,
        String password
) {}
