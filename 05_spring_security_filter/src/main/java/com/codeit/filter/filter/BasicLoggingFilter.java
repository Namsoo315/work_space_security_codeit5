package com.codeit.filter.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.GenericFilterBean;

// GenericFilterBean : 가장 간단한 서블릿 형태의 Bean 필터
// OncePerRequestFilter : 필터 체인에서 단 한번만 호출되는 필터
@Slf4j
public class BasicLoggingFilter extends GenericFilterBean {

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
      FilterChain filterChain) throws ServletException, IOException {

    HttpServletRequest request = (HttpServletRequest) servletRequest;
    HttpServletResponse response = (HttpServletResponse) servletResponse;

    String method = request.getMethod();
    String requestURI = request.getRequestURI();
    String remoteAddr = request.getRemoteAddr();    // IP

    log.info("사용자 요청 - method : {}, requestURI : {}, remoteAddr : {}", method, requestURI, remoteAddr);

    filterChain.doFilter(servletRequest, servletResponse);

    log.info("사용자 응답 - body : {}", response.getStatus());
  }
}
