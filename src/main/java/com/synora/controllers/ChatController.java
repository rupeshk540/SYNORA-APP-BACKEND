package com.synora.controllers;


import com.synora.dto.MessageRequest;
import com.synora.entities.Message;
import com.synora.entities.Room;
import com.synora.repositories.MessageRepository;
import com.synora.services.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.security.Principal;

@Controller
@CrossOrigin("http://localhost:5173")
public class ChatController {
    @Autowired
    private RoomService roomService;

    @Autowired
    private MessageRepository messageRepository;

    @MessageMapping("/sendMessage/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public Message sendMessages(
            @DestinationVariable String roomId,
            @Payload MessageRequest request,
            Principal principal
    ) {
        Room room = roomService.findByRoomId(roomId);
        if (room == null) {
            throw new RuntimeException("Room not found !!");
        }

        Message message = new Message();
        message.setRoomId(roomId);
        message.setContent(request.getContent());
        message.setSender(principal.getName());   // from the JWT, not the request body

        return messageRepository.save(message);
    }
}

