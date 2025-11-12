package com.codeit.auth.config;

import com.codeit.auth.entity.User;
import com.codeit.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(String... args) throws Exception {
    // 테스트 사용자 생성
    if (userRepository.findByUsername("admin").isEmpty()) {
      User user = new User(null, "admin", passwordEncoder.encode("admin"), "ADMIN", true);
      log.info("생성된 사용자 : {}", user);
      userRepository.save(user);
    }

    if(userRepository.findByUsername("user").isEmpty()) {
      User user = new User(null, "user", passwordEncoder.encode("1234"), "USER", true);
      log.info("생성된 사용자 : {}", user);
      userRepository.save(user);
    }
  }
}
