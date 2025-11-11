package com.codeit.security.basic.controller;

import com.codeit.security.basic.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class BoardController {

    // 누구나 접근 가능
    @GetMapping("/public/info")
    public ResponseEntity<PublicInfo> publicInfo() {
        PublicInfo publicInfo = new PublicInfo("This is public information", "success");
        return ResponseEntity.ok(publicInfo);
    }

    // 유저만 접근 가능
    @GetMapping("/board")
    public ResponseEntity<UserBoardInfo> boardList(Authentication auth) {
        return ResponseEntity.ok(
            new UserBoardInfo(
                    auth.getName(),
                    new ArrayList<>(auth.getAuthorities()),
                    List.of("게시글1", "게시글2", "게시글3")
            )
        );
    }

    // 직원만 접근 가능
    @PostMapping("/board")
    public ResponseEntity<CreateResult> createBoard(@RequestBody CreateRequest request) {
        return ResponseEntity.ok(
            new CreateResult(
                "게시글이 성공적으로 생성되었습니다.",
                request.title()
            )
        );
    }

    // 관리자만 접근 가능
    @GetMapping("/admin/users")
    public ResponseEntity<UserList> userList() {
        return ResponseEntity.ok(
            new UserList(
                List.of(
                    new UserRole("user", "USER"),
                    new UserRole("admin", "ADMIN")
                )
            )
        );
    }
}
