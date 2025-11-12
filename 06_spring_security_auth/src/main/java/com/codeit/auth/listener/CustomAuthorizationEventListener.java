package com.codeit.auth.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authorization.event.AuthorizationEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomAuthorizationEventListener
        implements ApplicationListener<AuthorizationEvent> {

    @Override
    public void onApplicationEvent(AuthorizationEvent event) {
        String user = event.getAuthentication().get().getName();
        boolean result = event.getAuthorizationResult().isGranted();
        log.info("[인가 이벤트] user={} result={}", user, result);
    }
}