package com.codeit.security.controller;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

  // session key
  private static final String SESSION_USER = "LOGIN_USER";

  // cookie에서 사용할 key
  private static final String COOKIE_ID = "cid";
  private static final String COOKIE_PW = "cpw";


  @GetMapping({"/", "/home"})
  public String home(Model model, HttpSession session) {
    User user = (User) session.getAttribute(SESSION_USER);
    model.addAttribute("user", user);
    return "home";
  }

  @GetMapping("/login")
  public String loginPage(Model model, HttpSession session, HttpServletRequest request) {
    if (session.getAttribute(SESSION_USER) != null) {
      return "redirect:/";
    }

    String cid = readCookie(request, COOKIE_ID);
    String cpw = readCookie(request, COOKIE_PW);

    model.addAttribute("cid", cid);
    model.addAttribute("cpw", cpw);

    return "login";
  }


  @PostMapping("/login")
  public String login(@RequestParam(required = false) String username,
      @RequestParam(required = false) String password,
      HttpServletRequest request,
      HttpServletResponse response,
      HttpSession session,
      Model model) {

    if ("test01".equals(username) && "1234".equals(password)) {
      User user = new User(username, password);
      session.setAttribute(SESSION_USER, user);
      model.addAttribute("user", user);

      // 로그인 성공시 쿠키 생성 (서버 제어)
      setCookie(response, COOKIE_ID, username, 60 * 60 * 24 * 30);  // 30일
      setCookie(response, COOKIE_PW, password, 60 * 60 * 24 * 30);

      return "login-success";
    }

    return "login-fail";
  }

  @PostMapping("/logout")
  public String logout(HttpSession session) {
    session.invalidate(); // 세션 모두 무효화
    return "redirect:/";
  }

  @GetMapping("/delete_cookie")
  public String deleteCookie(HttpServletResponse response) {
    deleteCookie(response, COOKIE_ID);
    deleteCookie(response, COOKIE_PW);
    return "redirect:/";
  }

  private String readCookie(HttpServletRequest request, String name) {
    if (request.getCookies() == null) {
      return null;
    }
    for (Cookie c : request.getCookies()) {
      if (name.equals(c.getName())) {
        return c.getValue();
      }
    }
    return null;
  }

  private void setCookie(HttpServletResponse response, String name, String value,
      int maxAgeSeconds) {
    Cookie cookie = new Cookie(name, value);
    cookie.setPath("/");
    cookie.setHttpOnly(false);   // JS 접근 차단
    cookie.setSecure(false);    // HTTPS 환경이라면 true
    cookie.setMaxAge(maxAgeSeconds);
    response.addCookie(cookie);
  }

  private void deleteCookie(HttpServletResponse response, String name) {
    Cookie cookie = new Cookie(name, "");
    cookie.setPath("/");
    cookie.setHttpOnly(true);
    cookie.setMaxAge(0);
    response.addCookie(cookie);
  }
}