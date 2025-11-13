package com.codeit.session.dto.post;

import com.codeit.session.dto.user.UserDto;

import java.time.Instant;

public record PostDto(
        Long id,
        String title,
        String content,
        boolean deleted,
        Instant createdAt,
        Instant updatedAt,
        UserDto author
) {}
