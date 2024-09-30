package com.server.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Log4j2
@RestController
public class AuthCheckController {

    @GetMapping("/all")
    @PreAuthorize("permitAll()")
    public ResponseEntity<String> all(){
        return ResponseEntity.ok("모두가 접근");
    }

    // 인증되지 않은 사용자
    @GetMapping("/no-login")
    @PreAuthorize("isAnonymous()")
    public ResponseEntity<String> noLogin(){
        return ResponseEntity.ok("인증되지 않은 사람만 접근!!");
    }

    // 인증된 사용자
    @GetMapping("/has-certified")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> isAuthenticated(@AuthenticationPrincipal UserDetails userDetails) {
        String authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .reduce((a, b) -> a + ", " + b)
                .orElse("권한 없음");

        // 사용자 정보와 권한을 Map에 담기
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("username", userDetails.getUsername());
        userInfo.put("password", userDetails.getPassword());
        userInfo.put("authorities", authorities);

        // Map을 JSON 응답으로 반환
        return ResponseEntity.ok(userInfo);
    }

    @GetMapping("/admin")
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
        // Map을 JSON 응답으로 반환
        return ResponseEntity.ok(userInfo);
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('User')")
    public ResponseEntity<Map<String, String>> use(@AuthenticationPrincipal UserDetails userDetails) {
        String authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .reduce((a, b) -> a + ", " + b)
                .orElse("권한 없음");
        // 사용자 정보와 권한을 Map에 담기
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("username", userDetails.getUsername());
        userInfo.put("password", userDetails.getPassword());
        userInfo.put("authorities", authorities);
        // Map을 JSON 응답으로 반환
        return ResponseEntity.ok(userInfo);
    }

}
