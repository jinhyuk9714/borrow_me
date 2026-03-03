package com.ardkyer.borrowme.controller;

import com.ardkyer.borrowme.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserApiController {

    @Autowired
    private UserService userService;

    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("avatar") MultipartFile file, Authentication authentication) throws IOException {
        String username = authentication.getName();
        String avatarUrl = userService.updateUserAvatar(username, file);
        return ResponseEntity.ok(Map.of("success", true, "avatarUrl", avatarUrl));
    }
}