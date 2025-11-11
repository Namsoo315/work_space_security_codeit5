package com.codeit.security.basic.dto;

import org.springframework.security.core.GrantedAuthority;

import java.util.List;

public record UserBoardInfo(
        String username,
        List<GrantedAuthority> authorities,
        List<String> boards
) {
}
