package com.codeit.session.service;

import com.codeit.session.dto.user.UserCreateRequest;
import com.codeit.session.dto.user.UserResponse;
import com.codeit.session.dto.user.UserUpdateRequest;
import com.codeit.session.entity.User;
import com.codeit.session.mapper.UserMapper;
import com.codeit.session.repository.UserRepository;
import com.codeit.session.security.SessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    // 패스워드 암호화 사용
    private final PasswordEncoder passwordEncoder;

    // 회원 탈퇴나 update시 세션을 제거하는 작업
    private final SessionManager sessionManager;


    @Transactional
    public  UserResponse create (@RequestBody UserCreateRequest userCreateRequest) {
        User user = userMapper.toUser(userCreateRequest);

        if (user.getId() == null)
            throw new RuntimeException("잘못된 사용자 파라미터 입니다.");

        if(userRepository.existsByUsername(user.getUsername()))
            throw new RuntimeException("이미 사용중인 유저 아이디 입니다. : " + user.getUsername());

        if (userRepository.existsByEmail(user.getEmail()))
            throw new RuntimeException("이미 사용중인 이메일 입니다. : " + user.getEmail());

        try {
            // 회원가입시 패스워드 암호화
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user = userRepository.save(user);
        }catch (Exception e){
            throw new RuntimeException("사용자 저장중 오류가 발생하였습니다.");
        }

        return userMapper.toResponse(user);
    }

    @PreAuthorize("principal.userDto.id == #id or hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. id=" + id));
        return userMapper.toResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userMapper.toUserResponseList(userRepository.findAll());
    }

    @PreAuthorize("principal.userDto.id = #userId")
    @Transactional
    public UserResponse update(Long userId, UserUpdateRequest newUser) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new RuntimeException("사용자를 찾을수 없습니다. " + +userId));

        if (newUser.password() != null) {
            user.setPassword(passwordEncoder.encode(newUser.password()));
        }

        if (newUser.email() != null) {
            user.setEmail(newUser.email());
        }

        User save = userRepository.save(user);
        // 업데이트 시 기존 세션을 무효화 하고 싶을 때
        sessionManager.invalidateSessionsByUserId(user.getId());
        return userMapper.toResponse(save);
    }

    @PreAuthorize("principal.userDto.id == #id")
    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. id=" + id));
        userRepository.delete(user);

        // 세션 무효화
        sessionManager.invalidateSessionsByUserId(id);
    }

}
