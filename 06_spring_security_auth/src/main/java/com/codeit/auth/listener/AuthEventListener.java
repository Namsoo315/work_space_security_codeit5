package com.codeit.auth.listener;

import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authorization.event.AuthorizationEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuthEventListener {

  @EventListener
  public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
    Authentication authentication = event.getAuthentication();
    log.info("Authentication success, name : {}", authentication.getName());
  }

  @EventListener
  public void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
    Authentication authentication = event.getAuthentication();
    log.info("Authentication fail, name : {}, pass : {}", authentication.getName(), authentication.getCredentials());
  }

  @EventListener
  public void onAuthorizationEvent(AuthorizationEvent event) {
    Supplier<Authentication> authentication = event.getAuthentication();
    log.info("Authorization event, name : {}", authentication.get().getName());
  }
}
