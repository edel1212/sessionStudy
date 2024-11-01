package com.yoo.server.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.session.data.redis.RedisSessionRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Log4j2
@RestController
@RequiredArgsConstructor
public class LoginController {
    // Spring Security Manager
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    private final RedisTemplate<String, Object> redisTemplate;

    private final RedisSessionRepository redisSessionRepository;

    @PreAuthorize("isAnonymous()")
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(String username, String password, HttpServletRequest request) {

        // Login 전 중복 로그인 체크
        // Redis에서 기존 세션 ID와 로그인 상태 조회
        String redisSessionIdKey = "loginUserData:" + username;
        String previousSessionId = (String) redisTemplate.opsForHash().get(redisSessionIdKey, "sessionId");
        log.info("Previous Session ID from Redis: " + previousSessionId);

        // 새로운 세션 생성
        HttpSession session = request.getSession(true);

        // Redis에 저장된 세션 ID가 현재 세션과 다르고, 이미 로그인된 상태라면 기존 세션을 삭제
        if (!Objects.equals(session.getId(), previousSessionId) && previousSessionId != null) {
            log.info("Deleting previous session ID: {}", previousSessionId);
            redisSessionRepository.deleteById(previousSessionId); // 기존 세션 삭제
        } //if

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 인증 정보를 SecurityContext에 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);

        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

        Map<String, String> result = new HashMap<>();
        result.put("userName", authentication.getName());

        // 로그인 정보 Redis에 저장
        String sessionId = session.getId();
        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("sessionId", sessionId);    // 세션 ID
        // 하나의 Redis Hash에 저장
        redisTemplate.opsForHash().putAll("loginUserData:" + username, sessionData);
        // 저장 시간 지정
        redisTemplate.expire("loginUserData:" + username, 180, TimeUnit.SECONDS);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/member/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, Authentication authentication) {
        // 현재 세션을 가져옵니다.
        HttpSession session = request.getSession(false); // false로 설정하면 세션이 없을 때 새로운 세션을 만들지 않습니다.

        log.info("-- Session 삭제 진입--");
        if (session != null) {
            log.info("-- Session 삭제 성공--");
            session.invalidate(); // 세션 무효화
        }

        // 로그아웃 성공 메시지 반환
        return ResponseEntity.ok("Logged out successfully");
    }


}
