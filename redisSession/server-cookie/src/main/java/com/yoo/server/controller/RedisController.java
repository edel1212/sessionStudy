package com.yoo.server.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisController {

    @PostMapping("/session")
    private ResponseEntity<Void> login(final HttpServletRequest httpRequest) {

        final HttpSession session = httpRequest.getSession();
        session.setAttribute("memberId", "kokoa");
        session.setMaxInactiveInterval(3600);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/session")
    public String sessionTest2(HttpSession httpSession) {
        String data = httpSession.getAttribute("memberId").toString();
        return data;
    }


}
