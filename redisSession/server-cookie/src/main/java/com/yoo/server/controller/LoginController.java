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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.session.data.redis.RedisSessionRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
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

        // 1 . 로그인 정보 확인
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);

        // >> Login 전 중복 로그인 체크
        // 2 . Redis에서 Login 정보 유/무 체크
        String redisSessionIdKey = "loginUserData:" + username;
        String previousSessionId = (String) redisTemplate.opsForHash().get(redisSessionIdKey, "sessionId");

        // 3 . 로그인 되어 있는 정보 삭제
        if (previousSessionId != null) {
            log.info("이전 로그인 Session Id: " + previousSessionId + " 삭제");
            redisSessionRepository.deleteById(previousSessionId); // 기존 세션 삭제
        } //if

        // >> 인증 정보를 SecurityContext에 저장
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 4 . 신규 생성된 Session에 Security Context 정보 저장
        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

        // 5 . Redis에 로그인 정보 저장 - 중복 로그인 방지용
        String sessionId                = session.getId();
        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("sessionId", sessionId);
        redisTemplate.opsForHash().putAll(redisSessionIdKey, sessionData);
        // 저장 시간 지정
        redisTemplate.expire(redisSessionIdKey, 180, TimeUnit.SECONDS);

        Map<String, String> result = new HashMap<>();
        result.put("userName", authentication.getName());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/member/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, @AuthenticationPrincipal User user) {
        // 현재 세션을 가져옵니다.
        HttpSession session = request.getSession(false);

        log.info("-- Session 삭제 진입--");
        if (session != null) {
            log.info("-- Session 삭제 성공--");
            session.invalidate(); // 세션 무효화
            // Redis Template 정보 삭제
            String redisSessionIdKey = "loginUserData:" + user.getUsername();
            String previousSessionId = (String) redisTemplate.opsForHash().get(redisSessionIdKey, "sessionId");
            redisSessionRepository.deleteById(previousSessionId);
        } // if

        // 로그아웃 성공 메시지 반환
        return ResponseEntity.ok("Logged out successfully");
    }

}
