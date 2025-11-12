package com.codeit.auth.config;

import com.codeit.auth.service.DaoUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DaoProviderConfig {

  private final DaoUserDetailsService daoUserDetailsService;
  private final PasswordEncoder passwordEncoder;

  // 실제 인증을 수행하는 provider 선언
  // DaoAuthenticationProvider : database를 통해 사용자 인증을 수행하는 provider
  @Bean
  public DaoAuthenticationProvider daoAuthenticationProvider() {
    DaoAuthenticationProvider daoAuthenticationProvider
        = new DaoAuthenticationProvider(daoUserDetailsService);
    daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);

    return daoAuthenticationProvider;
  }
}
