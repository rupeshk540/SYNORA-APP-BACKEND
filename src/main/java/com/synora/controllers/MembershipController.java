package com.synora.controllers;

import com.synora.services.MembershipService;
import com.synora.services.impl.CurrentUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/rooms")
@CrossOrigin("http://localhost:5173")
public class MembershipController {

    private final MembershipService membershipService;
    private final CurrentUserService currentUserService;

    public MembershipController(MembershipService membershipService, CurrentUserService currentUserService) {
        this.membershipService = membershipService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/{roomId}/read")
    public ResponseEntity<?> markAsRead(@PathVariable String roomId, Authentication authentication) {
        Long userId = currentUserService.getCurrentUser(authentication).getId();
        membershipService.markAsRead(userId, roomId);
        return ResponseEntity.ok().build();
    }
}
