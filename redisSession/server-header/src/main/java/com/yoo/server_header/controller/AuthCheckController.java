package com.yoo.server_header.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Log4j2
@RestController
public class AuthCheckController {

    @Value("${server.port}")
    private String serverPort;

    @PostMapping("/all")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Map<String, String>> all(){
        Map<String, String> msg = new HashMap<>();

        msg.put("port", serverPort);

        msg.put("msg", "All Access");
        return ResponseEntity.ok(msg);
    }

    // 인증되지 않은 사용자
    @PostMapping("/no-login")
    @PreAuthorize("isAnonymous()")
    public ResponseEntity<Map<String, String>> noLogin(){
        Map<String, String> msg = new HashMap<>();
        msg.put("msg", "Doesn't have Auth");
        msg.put("port", serverPort);

        return ResponseEntity.ok(msg);
    }

    // 인증된 사용자
    @PostMapping("/has-certified")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> isAuthenticated(HttpServletRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        String authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .reduce((a, b) -> a + ", " + b)
                .orElse("권한 없음");

        // 사용자 정보와 권한을 Map에 담기
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("username", userDetails.getUsername());
        userInfo.put("password", userDetails.getPassword());
        userInfo.put("authorities", authorities);

        userInfo.put("port", serverPort);

        // Map을 JSON 응답으로 반환
        return ResponseEntity.ok(userInfo);
    }

    @PostMapping("/admin")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Map<String, String>> admin(@AuthenticationPrincipal UserDetails userDetails) {
        String authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .reduce((a, b) -> a + ", " + b)
                .orElse("권한 없음");
        // 사용자 정보와 권한을 Map에 담기
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("username", userDetails.getUsername());
        userInfo.put("password", userDetails.getPassword());
        userInfo.put("authorities", authorities);
        userInfo.put("port", serverPort);

        // Map을 JSON 응답으로 반환
        return ResponseEntity.ok(userInfo);
    }

    @PostMapping("/user")
    @PreAuthorize("hasRole('User')")
    public ResponseEntity<Map<String, String>> user(@AuthenticationPrincipal UserDetails userDetails) {
        String authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .reduce((a, b) -> a + ", " + b)
                .orElse("권한 없음");
        // 사용자 정보와 권한을 Map에 담기
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("username", userDetails.getUsername());
        userInfo.put("password", userDetails.getPassword());
        userInfo.put("authorities", authorities);
        userInfo.put("port", serverPort);

        // Map을 JSON 응답으로 반환
        return ResponseEntity.ok(userInfo);
    }

}
