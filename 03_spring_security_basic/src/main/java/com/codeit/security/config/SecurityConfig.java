package com.codeit.security.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration      // Config임을 알리는 어노테이션
@EnableWebSecurity  // Security 설정 어노테이션
// @EnableMethodSecurity // Method 레벨 Security설정 어노테이션
public class SecurityConfig {

  @Bean
  public SecurityFilterChain SecurityFilterChain(HttpSecurity http) throws Exception {
    http
        // API CSRF 제외, 폼 로그인은 유지
        .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))

        // 세션 정책 -> IF_REQUIRED: 피룡에 따라서 세션 생성
        .sessionManagement(sm ->
            sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))  // 필요할 때 마다 (다른 옵션 있음)

        // 접근 경로 제어
        .authorizeHttpRequests(auth -> auth
            // 정적 리소스 / 홈
            .requestMatchers("/", "/index.html", "/css/**").permitAll()
            // 공개 API URL
            .requestMatchers("/api/public/**").permitAll()
            // 권한 필요 API
            // URL + Role 기반 접근
            .requestMatchers("/api/board/**").hasAnyRole("USER", "ADMIN")
            .requestMatchers("/api/admin/**").hasAnyRole("ADMIN")
            .anyRequest().authenticated()
        )
        // 기본 로그인 폼들 설정 (안하면 default가 있는거)
        .formLogin(Customizer.withDefaults())
        .logout(Customizer.withDefaults())

        // 인증 실패시 기본 동작
        .exceptionHandling(ex -> ex
            .accessDeniedHandler((req, res, e) -> {
              res.setContentType("application/json");
              res.setStatus(HttpServletResponse.SC_FORBIDDEN);
              res.setCharacterEncoding("UTF-8");
              res.getWriter().write("{error: 권한이 없습니다.}");
            }));

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  // 테스트 사용자 생성 영역
  @Bean
  public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
    // 일반 직원
    UserDetails user = User.builder()
        .username("user")
        .password(passwordEncoder.encode("1234"))
        .roles("USER")
        .build();

    // 관리자
    UserDetails admin = User.builder()
        .username("admin")
        .password(passwordEncoder.encode("admin"))
        .roles("USER", "ADMIN")
        .build();

    return new InMemoryUserDetailsManager(user, admin);
  }
}
