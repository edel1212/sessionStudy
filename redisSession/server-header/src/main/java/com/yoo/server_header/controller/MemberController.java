package com.yoo.server_header.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.session.Session;
import org.springframework.session.data.redis.RedisSessionRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Log4j2
@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {
    // Spring Security Manager
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    private final RedisSessionRepository redisSessionRepository;

    @Value("${spring.session.redis.namespace}")
    private String redisSessionPrefix;

    @PreAuthorize("isAnonymous()")
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(String username, String password, HttpServletRequest request) {
        // 인증 로직
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 인증 정보를 SecurityContext에 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 새로운 세션 생성
        HttpSession newSession = request.getSession(true);
        newSession.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

        Map<String, String> result = new HashMap<>();
        result.put("userName", authentication.getName());
        result.put("xAuthToken", newSession.getId());

        return ResponseEntity.ok(result);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        String sessionId = request.getHeader("x-auth-token");
        Session session = redisSessionRepository.findById(sessionId);

        // Session 유무 확인
        if (session == null) ResponseEntity.ok("Log-out Fail");

        redisSessionRepository.deleteById(sessionId);

        // 로그아웃 성공 메시지 반환
        return ResponseEntity.ok("Logged out successfully");
    }


}
