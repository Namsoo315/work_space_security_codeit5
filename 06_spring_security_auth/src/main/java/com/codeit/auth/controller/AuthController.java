package com.codeit.auth.controller;

import com.codeit.auth.dto.ApiMessage;
import com.codeit.auth.dto.SignupRequest;
import com.codeit.auth.dto.UserResponse;
import com.codeit.auth.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final UserService userService;

  @PostMapping("/signup")
  public UserResponse signup(@Valid @RequestBody SignupRequest request) {
    return userService.signup(request.username(), request.password());
  }

  // 로그인한 사용자 정보 가져오기
  @GetMapping("/me")
  public UserResponse me(Authentication authentication) {   // Security Authentication의 사용자 정보를 알아서 가져옴
    return userService.findByUsername(authentication.getName());
  }

  @GetMapping("/me2")
  public UserResponse me2() {
    // 가끔식 주입을 못받을 때가 잇어서 Context에 있는 인가 정보 데이터를 가져옴.
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return userService.findByUsername(authentication.getName());
  }

  @GetMapping("/me3")
  public UserResponse me3(@AuthenticationPrincipal UserDetails userDetails) {
    return userService.findByUsername(userDetails.getUsername());
  }

  @DeleteMapping("/me")
  public ApiMessage deleteMe(Authentication authentication) {
    String username = authentication.getName();
    userService.deleteByUsername(username);
    return new ApiMessage("회원 탈퇴 완료 : " + username);
  }
}
