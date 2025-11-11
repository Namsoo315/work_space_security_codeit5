package com.codeit.security.basic.dto;

import java.util.List;

public record UserList(
        List<UserRole> users
) {
}
