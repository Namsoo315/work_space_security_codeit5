package com.codeit.session.config;

import com.codeit.session.entity.Role;
import com.codeit.session.security.Http403ForbiddenAccessDeniedHandler;
import com.codeit.session.security.LoginFailureHandler;
import com.codeit.session.security.LoginSuccessHandler;
import com.codeit.session.security.SpaCsrfTokenRequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyAuthoritiesMapper;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            ObjectMapper objectMapper,
            LoginSuccessHandler loginSuccessHandler,
            LoginFailureHandler loginFailureHandler,
            DaoAuthenticationProvider daoAuthenticationProvider,
            Http403ForbiddenAccessDeniedHandler forbiddenAccessDeniedHandler,
            SessionRegistry sessionRegistry,
            UserDetailsService userDetailsService
    ) throws Exception {
        http
                .authenticationProvider(daoAuthenticationProvider)
                .authorizeHttpRequests(auth -> auth
                        // permitAll 경로 설정
                        .requestMatchers("/login", "/error", "/", "/index.html").permitAll()
                        .requestMatchers("/api/auth/csrf-token").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll() // 회원 가입
                        .requestMatchers("/api/posts").permitAll() // 공개 post 포기
                        // 나머지 권한 USER 필요
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf
                        // 로그 아웃은 CSRF 예외처리 필요
                        .ignoringRequestMatchers("/api/auth/logout")
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        // spring에서 제공하는 강력한 CSRF 토큰 만드는 코드, 최신버전은 유사코드가 삽입됨!
                        .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
                )
                .formLogin(login -> login
                        .loginProcessingUrl("/api/auth/login")
                        .successHandler(loginSuccessHandler)
                        .failureHandler(loginFailureHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        // logout 응답값 자동생성용 핸들러
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler())
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new Http403ForbiddenEntryPoint())
                        .accessDeniedHandler(forbiddenAccessDeniedHandler)
                )
                .sessionManagement(session -> session
                        .sessionFixation().migrateSession()
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                        .sessionRegistry(sessionRegistry)
                )
                // Remember Me 간단한설정 버전
                //.rememberMe(Customizer.withDefaults())
                .rememberMe(remember -> remember
                        .key("my-remember-me-key") // 쿠키 이름
                        .tokenValiditySeconds(30)
                        .userDetailsService(userDetailsService)
                )
        ;
        return http.build();
    }

    // 사용자 권한의 높고 낮음을 알리는 RoleHierarchy를 만드는 Bean
    // -> 해당 객체가 있어야 특정 권한 보다 높은 사용자 허용이 가능하다!
    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role(Role.ADMIN.name()) //  높은 권한
                .implies(Role.USER.name()) // 낮은 권한

                // role + implies를 통해 추가 설정 가능!
//                .role(Role.MANAGER.name())
//                .implies(Role.USER.name())

                .build();
    }

    // BCrypt 알고리즘을 통해 패스워드 생성 및 복호화를 수행할 객체 생성
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // DaoAuthenticationProvider : db기반으로 인증을 수행하는 provider
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(
            // UserDetailsService : 우리가 만들어야할 서비스!!
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder,
            RoleHierarchy roleHierarchy
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        provider.setAuthoritiesMapper(new RoleHierarchyAuthoritiesMapper(roleHierarchy));
        return provider;
    }

    // 세션 저장소인데, 만일 DB 혹은 레디스를 사용할 경우 옵션에서 추가 가능!
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }


    // SessionEvent 이벤트 발행 수행
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    // MethodSecurityExpression(어노테이션 기반 권한 제어)를 수행할때 roleHierarchy를 알리기 위해 추가
    @Bean
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler(
            RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setRoleHierarchy(roleHierarchy);
        return handler;
    }

    // 디버깅용 필터 추가!
    @Bean
    public CommandLineRunner debugFilterChain(SecurityFilterChain filterChain) {
        return args -> {
            int filterSize = filterChain.getFilters().size();
            List<String> filterNames = IntStream.range(0, filterSize)
                    .mapToObj(idx -> String.format("\t[%s/%s] %s", idx + 1, filterSize,
                            filterChain.getFilters().get(idx).getClass()))
                    .toList();
            log.debug("Debug Filter Chain...\n{}", String.join(System.lineSeparator(), filterNames));
        };
    }
}
