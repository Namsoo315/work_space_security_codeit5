package com.codeit.security.dto;

import java.util.List;

public record UserList(
        List<UserRole> users
) {
}
