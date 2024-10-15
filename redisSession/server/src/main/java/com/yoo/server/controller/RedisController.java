package com.yoo.server.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisController {

    @PreAuthorize("isAnonymous()")
    @PostMapping("/session")
    public String sessionTest(HttpSession httpSession) {
        String id = "test321";
        httpSession.setAttribute("sessionID", id);
        return "session TEST";
    }

    @GetMapping("/session")
    public String sessionTest2(HttpSession httpSession) {
        String data = httpSession.getAttribute("sessionID").toString();
        return data;
    }


}
