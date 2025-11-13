package com.codeit.session.dto.user;


import com.codeit.session.entity.Role;

public record UserDto(
        Long id,
        String username,
        String email,
        Role role
) {}
