package com.codeit.session.dto.post;

import jakarta.validation.constraints.Size;

public record PostUpdateRequest(
        @Size(max = 200, message = "제목은 200자 이하여야 합니다")
        String newTitle,

        String newContent
) {}
