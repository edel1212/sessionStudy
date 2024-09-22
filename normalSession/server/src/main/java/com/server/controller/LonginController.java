package com.server.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/login")
@RestController
public class LonginController {

    @GetMapping
    public ResponseEntity login(){
        return ResponseEntity.ok("Hi");
    }
}
