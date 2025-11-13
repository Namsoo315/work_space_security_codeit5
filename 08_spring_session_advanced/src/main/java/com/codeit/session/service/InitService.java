package com.codeit.session.service;

import com.codeit.session.entity.Post;
import com.codeit.session.entity.Role;
import com.codeit.session.entity.User;
import com.codeit.session.exception.UserNotFoundException;
import com.codeit.session.mapper.UserMapper;
import com.codeit.session.repository.PostRepository;
import com.codeit.session.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@RequiredArgsConstructor
@Service
public class InitService {


    @Value("${blog.admin.username}")
    private String adminUsername;
    @Value("${blog.admin.password}")
    private String adminPassword;
    @Value("${blog.admin.email}")
    private String adminEmail;

    @Value("${blog.user.username}")
    private String userUsername;
    @Value("${blog.user.password}")
    private String userPassword;
    @Value("${blog.user.email}")
    private String userEmail;

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void initAdmin() {
        if (userRepository.existsByEmail(adminEmail) || userRepository.existsByUsername(adminUsername)) {
            log.warn("이미 관리자가 존재합니다.");
            return;
        }

        String encodedPassword = passwordEncoder.encode(adminPassword);
        User admin = User.builder()
                .username(adminUsername)
                .email(adminEmail)
                .password(encodedPassword)
                .role(Role.ADMIN)
                .build();

        userRepository.save(admin);
        log.info("관리자가 초기화되었습니다.");
    }

    @Transactional
    public void initDefaultUser() {
        if (userRepository.existsByEmail(userEmail) || userRepository.existsByUsername(userUsername)) {
            log.warn("이미 기본 사용자가 존재합니다.");
            return;
        }

        String encodedPassword = passwordEncoder.encode(userPassword);
        User user = User.builder()
                .username(userUsername)
                .email(userEmail)
                .password(encodedPassword)
                .role(Role.USER)
                .build();

        userRepository.save(user);
        log.info("기본 사용자가 초기화되었습니다.");
    }


    @Transactional
    public void initSamplePosts() {
        if (postRepository.count() > 0) {
            log.warn("이미 게시글이 존재하므로 초기화를 건너뛴다");
            return;
        }

        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new UserNotFoundException("Admin user not found: " + adminUsername));
        User user = userRepository.findByUsername(userUsername)
                .orElseThrow(() -> new UserNotFoundException("Default user not found: " + userUsername));

        List<Post> posts = List.of(
                Post.builder().title("Spring Boot 3.x 시작하기")
                        .content("스프링 부트 3.x의 변화와 기본 프로젝트 구조 정리")
                        .author(admin).deleted(true).build(),
                Post.builder().title("Spring Security 6 인증 흐름")
                        .content("AuthenticationManager, ProviderManager, 필터 체인 핵심 개념 요약")
                        .author(user).deleted(false).build(),
                Post.builder().title("OAuth2/OIDC 로그인 이해")
                        .content("Authorization Code + PKCE, ID 토큰과 액세스 토큰 역할")
                        .author(admin).deleted(true).build(),
                Post.builder().title("CSRF와 CORS 정리")
                        .content("SPA 백엔드 연동 시 CookieCsrfTokenRepository, CORS 정책 구성")
                        .author(user).deleted(false).build(),
                Post.builder().title("Remember-Me 동작과 보안 고려사항")
                        .content("PersistentToken 기반 구조와 토큰 탈취 대응")
                        .author(admin).deleted(true).build(),
                Post.builder().title("세션 관리 전략")
                        .content("Session Fixation 방어, 동시 세션 제한, 만료 전략 설계")
                        .author(user).deleted(false).build(),
                Post.builder().title("JPA와 Query 성능 기초")
                        .content("지연 로딩, 페치 조인, N+1 대응과 인덱스 기본")
                        .author(admin).deleted(true).build(),
                Post.builder().title("PostgreSQL로 이전 시 체크리스트")
                        .content("데이터 타입, 시퀀스/ID, 트랜잭션 격리수준, 확장 모듈 점검")
                        .author(user).deleted(false).build(),
                Post.builder().title("Docker와 Compose로 로컬 개발환경 구성")
                        .content("애플리케이션, DB, Redis를 묶은 개발 스택 템플릿")
                        .author(admin).deleted(true).build(),
                Post.builder().title("CI/CD 파이프라인 기초")
                        .content("빌드, 테스트, 보안 스캔, 배포 자동화 단계 정리")
                        .author(user).deleted(false).build()
        );
        postRepository.saveAll(posts);
        log.info("샘플 게시글 초기화 완료: {}건", posts.size());
    }

}
