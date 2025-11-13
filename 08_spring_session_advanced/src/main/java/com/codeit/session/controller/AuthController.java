package com.codeit.session.controller;

import com.codeit.session.dto.user.UserDto;
import com.codeit.session.security.BlogUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(@AuthenticationPrincipal BlogUserDetails userDetails) {
        log.info("내 정보 조회");
        return ResponseEntity.status(HttpStatus.OK).body(userDetails.getUserDto());
    }

    @GetMapping("/me2")
    public ResponseEntity<UserDto> me2() {
        log.info("내 정보 조회2");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        BlogUserDetails userDetails = (BlogUserDetails) authentication.getPrincipal();

        return ResponseEntity.status(HttpStatus.OK).body(userDetails.getUserDto());
    }

    // front csrf 토큰 확인, 원래 만들지 않지만 과제용
    @GetMapping("csrf-token")
    public ResponseEntity<CsrfToken> getCsrfToken(CsrfToken csrfToken) {
        log.info("CSRF 토큰 발생");
        return ResponseEntity.status(HttpStatus.OK).body(csrfToken);
    }
}
