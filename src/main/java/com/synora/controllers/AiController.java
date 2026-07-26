package com.synora.controllers;

import com.synora.dto.AiFixRequest;
import com.synora.dto.AiFixResponse;
import com.synora.services.AiService;
import com.synora.services.impl.CurrentUserService;
import com.synora.services.impl.RateLimitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin("http://localhost:5173")
public class AiController {

    @Autowired
    private AiService aiService;
    @Autowired
    private CurrentUserService currentUserService;
    @Autowired
    private RateLimitService rateLimitService;


    @PostMapping("/fix")
    public ResponseEntity<?> fixText(@RequestBody AiFixRequest request,Authentication authentication) {
        if (request.getText() == null || request.getText().isBlank()) {
            return ResponseEntity.badRequest().body("Text cannot be empty");
        }
        Long userId = currentUserService.getCurrentUser(authentication).getId();
        rateLimitService.checkLimit(userId);
        try {
            String corrected = aiService.fixText(request.getText());
            return ResponseEntity.ok(new AiFixResponse(corrected));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("AI service unavailable, try again");
        }
    }

    @GetMapping("/summarize/{roomId}")
    public ResponseEntity<?> summarizeRoom(
            @PathVariable String roomId,
            @RequestParam(required = false) String since,
            Authentication authentication
    ) {
        Long userId = currentUserService.getCurrentUser(authentication).getId();
        rateLimitService.checkLimit(userId);
        try {
            Instant sinceInstant = since != null ? Instant.parse(since) : null;
            return ResponseEntity.ok(aiService.summarizeRoom(userId, roomId, sinceInstant));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Could not generate summary, try again");
        }
    }
}

