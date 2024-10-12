package com.yoo.server.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequiredArgsConstructor
public class LoginController {
    // Spring Security Manager
    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    @PreAuthorize("isAnonymous()")
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(String username, String password, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 인증 정보를 SecurityContext에 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 새로운 세션 생성
        HttpSession session = request.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

        Map<String, String> result = new HashMap<>();
        result.put("userName", authentication.getName());

        return ResponseEntity.ok(result);
    }


    @GetMapping("/dummy-login")
    private ResponseEntity<String> login( HttpServletRequest request) {

        HttpSession session = request.getSession(true);
        // Redis cli 를 통해 확인 가능
        // > keys *
        // > 1) "yoo:session:sessions:ae4a86cb-f2e7-43ef-a730-4a738255d226"
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, "Hi - ContextObject");
        // Session 지속 시간
        session.setMaxInactiveInterval(3600);

        return ResponseEntity.ok("!?!?!");
    }

    @GetMapping("/session-data")
    private ResponseEntity<String> getSessionData( HttpServletRequest httpRequest) {

        final HttpSession session = httpRequest.getSession();
        String memberId = session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY).toString();

        return ResponseEntity.ok(memberId);
    }


}
