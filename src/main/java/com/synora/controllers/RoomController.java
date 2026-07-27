package com.synora.controllers;

import com.synora.dto.MyRoomResponse;
import com.synora.dto.RoomRequest;
import com.synora.entities.Message;
import com.synora.entities.Room;
import com.synora.repositories.MessageRepository;
import com.synora.services.MembershipService;
import com.synora.services.RoomService;
import com.synora.services.impl.CurrentUserService;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/rooms")
public class RoomController {

    private final RoomService roomService;
    private final MessageRepository messageRepository;
    private final MembershipService membershipService;
    private final CurrentUserService currentUserService;

    public RoomController(RoomService roomService, MessageRepository messageRepository,
                          MembershipService membershipService, CurrentUserService currentUserService) {
        this.roomService = roomService;
        this.messageRepository = messageRepository;
        this.membershipService = membershipService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public ResponseEntity<?> createRoom(@RequestBody RoomRequest request, Authentication authentication) {
        String roomId = request.getRoomId();
        String name = (request.getName() == null || request.getName().isBlank()) ? roomId : request.getName();
        if (roomService.findByRoomId(roomId) != null) {
            return ResponseEntity.badRequest().body("Room already exists !!");
        }
        Room savedRoom = roomService.createRoom(roomId,name);
        Long userId = currentUserService.getCurrentUser(authentication).getId();
        membershipService.joinRoom(userId, roomId);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRoom);
    }

    @GetMapping("/my-rooms")
    public ResponseEntity<List<MyRoomResponse>> getMyRooms(Authentication authentication) {
        Long userId = currentUserService.getCurrentUser(authentication).getId();
        return ResponseEntity.ok(membershipService.getMyRooms(userId));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<?> joinRoom(@PathVariable String roomId, Authentication authentication) {
        Room room = roomService.findByRoomId(roomId);
        if (room == null) {
            return ResponseEntity.badRequest().body("Room not found !!");
        }
        Long userId = currentUserService.getCurrentUser(authentication).getId();
        membershipService.joinRoom(userId, roomId);
        return ResponseEntity.ok(room);
    }

    @GetMapping("/{roomId}/messages")
    public ResponseEntity<List<Message>> getMessages(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Room room = roomService.findByRoomId(roomId);
        if (room == null) {
            return ResponseEntity.badRequest().build();
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        Page<Message> messagePage = messageRepository.findByRoomIdOrderByTimestampDesc(roomId, pageable);
        return ResponseEntity.ok(messagePage.getContent());
    }

    @GetMapping("/{roomId}/messages/since")
    public ResponseEntity<List<Message>> getMessagesSince(
            @PathVariable String roomId,
            @RequestParam String after
    ) {
        Room room = roomService.findByRoomId(roomId);
        if (room == null) return ResponseEntity.badRequest().build();

        Instant since = Instant.parse(after);
        return ResponseEntity.ok(messageRepository.findByRoomIdAndTimestampAfterOrderByTimestampAsc(roomId, since));
    }
}
