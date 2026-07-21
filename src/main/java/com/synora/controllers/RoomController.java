package com.synora.controllers;

import com.synora.entities.Message;
import com.synora.entities.Room;
import com.synora.repositories.MessageRepository;
import com.synora.services.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rooms")
@CrossOrigin("http://localhost:5173")
public class RoomController {

    @Autowired
    private MessageRepository messageRepository;
    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    private RoomService roomService;

    //create room
    @PostMapping
    public ResponseEntity<?> createRoom(@RequestBody String roomId){

        //if room is already exists
        if(roomService.findByRoomId(roomId) != null){
            return ResponseEntity.badRequest().body("Room is already exists !!");
        }

        //if not exists create then create
        Room savedRoom=roomService.createRoom(roomId);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRoom);

    }

    //get room:Join
    @GetMapping("/{roomId}")
    public ResponseEntity<?> joinRoom(@PathVariable String roomId){
        Room room = roomService.findByRoomId(roomId);

        if(room==null){
            return ResponseEntity.badRequest().body("Room not found !!");
        }

        return ResponseEntity.ok(room);
    }

    //get messages of room
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<List<Message>> getMessages(
            @PathVariable String roomId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        Room room = roomService.findByRoomId(roomId);
        if (room == null) {
            return ResponseEntity.badRequest().build();
        }

        Pageable pageable = (Pageable) PageRequest.of(page, size,Sort.by("timestamp").descending());
        Page<Message> messagePage = messageRepository.findByRoomIdOrderByTimestampDesc(roomId, pageable);
        return ResponseEntity.ok(messagePage.getContent());
    }
}
