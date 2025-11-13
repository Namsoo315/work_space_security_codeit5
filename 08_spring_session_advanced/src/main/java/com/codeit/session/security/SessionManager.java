package com.codeit.session.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
// Customizing Session Manager
public class SessionManager {

    private final SessionRegistry sessionRegistry;

    // userId로 저장된 세션들을 찾아오는 메서드
    public List<SessionInformation> getActiveSessionsByUserId(Long userId) {
        return sessionRegistry.getAllPrincipals().stream()
                .filter(principal -> principal instanceof BlogUserDetails)
                .map(BlogUserDetails.class::cast)
                .filter(details -> details.getUserDto() != null && userId.equals(details.getUserDto().id()))
                .flatMap(details -> sessionRegistry.getAllSessions(details, false).stream())
                .toList();
    }

    // 세션 무효화 기능
    public void invalidateSessionsByUserId(Long userId) {
        List<SessionInformation> activeSessions = getActiveSessionsByUserId(userId);
        if (!activeSessions.isEmpty()) {
            activeSessions.forEach(SessionInformation::expireNow);
            log.info("Session expired: {}", activeSessions.size());
        }
    }
}
