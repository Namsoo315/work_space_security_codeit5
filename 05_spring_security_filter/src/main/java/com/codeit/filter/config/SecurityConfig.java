package com.codeit.filter.config;

import com.codeit.filter.filter.AuditFilter;
import com.codeit.filter.filter.BasicLoggingFilter;
import com.codeit.filter.filter.PerformanceMonitoringFilter;
import com.codeit.filter.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final AuditService auditService;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/").permitAll()
            .requestMatchers("/public/**", "/health").permitAll()
            .requestMatchers("/api/**").authenticated()
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated()
        )
        .formLogin(form -> form
            .defaultSuccessUrl("/")
            .permitAll()
        )
        .httpBasic(Customizer.withDefaults())  //  HTTP Basic 인증 추가

        .logout(LogoutConfigurer::permitAll
        )
        .csrf(AbstractHttpConfigurer::disable)  // REST API이므로 CSRF 비활성화

        // 필터 순서: 성능 모니터링 → 로깅 → 감사 → 인증
//            .addFilterBefore(new PerformanceMonitoringFilter(), UsernamePasswordAuthenticationFilter.class)
//            .addFilterBefore(new BasicLoggingFilter(), UsernamePasswordAuthenticationFilter.class)
//            .addFilterBefore(new DetailedLoggingFilter(), UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(new AuditFilter(auditService), BasicAuthenticationFilter.class)
    ;
    return http.build();
  }

  @Bean
  public UserDetailsService userDetailsService(PasswordEncoder encoder) {
    UserDetails user = User.builder()
        .username("user")
        .password(encoder.encode("1234"))
        .roles("USER")
        .build();

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