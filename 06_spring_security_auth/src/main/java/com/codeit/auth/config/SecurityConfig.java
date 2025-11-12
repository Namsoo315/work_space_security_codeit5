package com.codeit.auth.config;


import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationEventPublisher;
import org.springframework.security.authorization.SpringAuthorizationEventPublisher;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // 메서드 레벨로 접근제어
public class SecurityConfig {

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        // CORS 설정: 모든 도메인, 메서드, 헤더 허용
        // - 개발·테스트 단계에서 편하게 모든 요청 허용
        // - 운영 시에는 허용 origin만 지정 권장
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))

        // XSS 방어 설정
        // - Content-Security-Policy(CSP): 외부 스크립트 삽입 차단
        // - jsdelivr, cdnjs 허용 (axios, 기타 CDN 리소스 사용 시 필요)
        .headers(headers -> headers
            .contentSecurityPolicy(csp ->
                csp.policyDirectives(
                    "script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net  https://cdnjs.cloudflare.com\""
                )
            )
        )

        // url 기반 필터링 수행
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/").permitAll() // index page
            .requestMatchers("/api/auth/signup").permitAll() // 회원 가입
            .requestMatchers("/api/**").authenticated() // 인증된 사람들만
            .anyRequest().authenticated()
        )
        // 폼 로그인
        .formLogin(form -> form
            .defaultSuccessUrl("/", true)
            .permitAll()
        )
        // 로그아웃 허용
        .logout(LogoutConfigurer::permitAll)

        // CSRF 방어 설정
        // - REST API는 세션 기반 폼 요청이 아닌 JSON 요청임으로 disable 처리한다.
        .csrf(AbstractHttpConfigurer::disable);

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  // 인가 설정 시 이벤트 퍼블리셔 활성화
  @Bean
  public AuthorizationEventPublisher authorizationEventPublisher(ApplicationEventPublisher publisher) {
    return new SpringAuthorizationEventPublisher(publisher);
  }


  // CORS 설정 빈
  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOriginPatterns(List.of("*")); // 모든 Origin 허용
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true); // 인증정보(쿠키 등) 포함 허용

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

}