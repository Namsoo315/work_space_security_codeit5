package com.codeit.auth.controller;

import com.codeit.auth.dto.ApiMessage;
import com.codeit.auth.dto.UserResponse;
import com.codeit.auth.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // 컨트롤러 레벨에서 권한 검증
public class AdminUserController {
  private final UserService userService;

  @GetMapping
  public List<UserResponse> getUsers() {
    return userService.findAll();
  }

  @DeleteMapping("/{id}")
  public ApiMessage deleteUser(@PathVariable Long id) {
    userService.deleteById(id);
    return new ApiMessage("Delete User successfully, id : " + id);
  }

}
