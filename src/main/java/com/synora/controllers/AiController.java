package com.synora.controllers;

import com.synora.dto.AiFixRequest;
import com.synora.dto.AiFixResponse;
import com.synora.services.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin("http://localhost:5173")
public class AiController {

    @Autowired
    private AiService aiService;

    @PostMapping("/fix")
    public ResponseEntity<?> fixText(@RequestBody AiFixRequest request) {
        if (request.getText() == null || request.getText().isBlank()) {
            return ResponseEntity.badRequest().body("Text cannot be empty");
        }
        try {
            String corrected = aiService.fixText(request.getText());
            return ResponseEntity.ok(new AiFixResponse(corrected));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("AI service unavailable, try again");
        }
    }
}
