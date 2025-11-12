package com.codeit.auth.service;

import com.codeit.auth.dto.UserResponse;
import com.codeit.auth.entity.User;
import com.codeit.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse signup(String username, String rawPassword) {
        userRepository.findByUsername(username).ifPresent(u -> {
            throw new IllegalArgumentException("이미 존재하는 사용자명");
        });
        User saved = userRepository.save(
                new User(null, username, passwordEncoder.encode(rawPassword), "USER", true)
        );

        System.out.println(saved);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public UserResponse findByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void deleteByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자 없음"));
        userRepository.delete(user);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!userRepository.existsById(id)) throw new IllegalArgumentException("사용자 없음");
        userRepository.deleteById(id);
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getUsername(), u.getRole(), u.isEnabled());
    }
}
