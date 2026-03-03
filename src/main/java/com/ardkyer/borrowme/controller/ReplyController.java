package com.ardkyer.borrowme.controller;

import com.ardkyer.borrowme.entity.Reply;
import com.ardkyer.borrowme.entity.User;
import com.ardkyer.borrowme.service.ReplyService;
import com.ardkyer.borrowme.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/replies")
public class ReplyController {

    @Autowired
    private ReplyService replyService;

    @Autowired
    private UserService userService;

    @PostMapping("/comments/{commentId}")
    public ResponseEntity<Map<String, Object>> addReply(
            @PathVariable Long commentId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        String content = request.get("content");
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Reply content cannot be empty");
        }

        User user = userService.findByUsername(authentication.getName());
        Reply reply = replyService.addReply(commentId, content, user);

        Map<String, Object> replyData = new HashMap<>();
        replyData.put("id", reply.getId());
        replyData.put("content", reply.getContent());
        replyData.put("createdAt", reply.getCreatedAt());

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", reply.getUser().getId());
        userData.put("username", reply.getUser().getUsername());
        replyData.put("user", userData);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("reply", replyData);
        return ResponseEntity.ok(response);
    }
}