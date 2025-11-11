package com.codeit.security.basic.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // 메서드 레벨 보안을 사용하려면 추가
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // REST API이므로 CSRF 비활성화
                .csrf(csrf -> csrf.disable())

                // 세션 사용 안 함 (REST API는 무상태)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(authorize -> authorize
                        // 공개 API는 모두 접근 가능
                        .requestMatchers("/api/public/**").permitAll()

                        // 게시판 API는 USER 역할 필요 (직원)
                        .requestMatchers("/api/board/**").hasAnyRole("USER", "ADMIN")

                        // 관리자 API는 ADMIN 역할 필요
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // index는 접근 가능
                        .requestMatchers("/", "/index.html").permitAll()

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )

                // HTTP Basic 인증 사용 (개발용)
                .httpBasic(withDefaults())

                // 예외 처리 추가
                .exceptionHandling(exception -> exception
                        // 인증되지 않은 사용자가 접근 시
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write("{\"error\":\"인증이 필요합니다\"}");
                        })

                        // 권한이 없는 사용자가 접근 시
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.getWriter().write("{\"error\":\"권한이 없습니다\"}");
                        })
                );

        return http.build();
    }

    // 테스트용 사용자 생성
    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        // 일반 직원
        UserDetails user = User.builder()
                .username("user")
                .password(encoder.encode("1234"))
                .roles("USER")
                .build();

        // 관리자 (직원 권한 + 관리자 권한)
        UserDetails admin = User.builder()
                .username("admin")
                .password(encoder.encode("admin"))
                .roles("USER", "ADMIN")
                .build();

        return new InMemoryUserDetailsManager(user, admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
