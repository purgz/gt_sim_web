package com.purgz.egt_api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/hello")
public class HelloController {

    @GetMapping("/")
    public ResponseEntity<String> helloWorld(){

        return ResponseEntity.ok("Hello user");
    }

    @GetMapping("/hello_admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> helloAdmin(){

        return ResponseEntity.ok("Hello admin");
    }
}
