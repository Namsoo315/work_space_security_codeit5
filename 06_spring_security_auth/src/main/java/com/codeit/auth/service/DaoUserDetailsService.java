package com.codeit.auth.service;

// Security에 사용하는 UserDetails 정보를 조회할 수 있는 인터페이스
// 두가지의 만드는 방법이 있음 -> 기존 Service에서 구현해도 좋지만 일반적으로 분리하여 설계한다.

import com.codeit.auth.entity.User;
import com.codeit.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DaoUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("NotFound" + username));

    return org.springframework.security.core.userdetails.User.builder()
        .username(user.getUsername())
        .password(user.getPassword())
        .roles(user.getRole())
        .disabled(!user.isEnabled())
        .build();
  }
}
