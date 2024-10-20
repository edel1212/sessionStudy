package com.yoo.server_header.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Log4j2
@RestController
@RequiredArgsConstructor
public class LoginController {
    // Spring Security Manager
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final RedisTemplate redisTemplate;

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

        // Redis에서 기존 세션 찾기
        String existingSessionId = (String) redisTemplate.opsForValue().get("loginUserId:" + username);
        log.info("--------------");
        log.info(existingSessionId);
        log.info("--------------");
        // 이미 로그인한 경우
        if (existingSessionId != null) {
            // 기존 세션 무효화
//            HttpSession oldSession = request.getSession(false);
//            oldSession.invalidate();
            // Redis에 저장된 Session 제거
            log.info("redisSessionPrefix ::" + redisSessionPrefix + ":sessions:" +existingSessionId);
            // TOdo 체크 필요
            redisTemplate.delete(redisSessionPrefix + ":sessions:" +existingSessionId);
        }// if

        // 새로운 세션 생성
        HttpSession newSession = request.getSession(true);
        newSession.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

        // Redis에 새 세션 ID 저장
        redisTemplate.opsForValue().set("loginUserId:" + username, newSession.getId());

        Map<String, String> result = new HashMap<>();
        result.put("userName", authentication.getName());
        result.put("sessionToken", newSession.getId());

        return ResponseEntity.ok(result);
    }

    @PostMapping("/member/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
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
