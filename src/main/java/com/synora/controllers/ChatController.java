package com.synora.controllers;


import com.synora.dto.DeliveryAckRequest;
import com.synora.dto.MessageRequest;
import com.synora.dto.MessageStatusUpdate;
import com.synora.dto.TypingEventDto;
import com.synora.entities.Message;
import com.synora.entities.Room;
import com.synora.repositories.MessageRepository;
import com.synora.services.RoomService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.security.Principal;

@Controller
public class ChatController {
    @Autowired
    private RoomService roomService;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

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

    @MessageMapping("/ack/{roomId}")
    @Transactional
    public void acknowledgeDelivery(
            @DestinationVariable String roomId,
            @Payload DeliveryAckRequest request
    ) {
        int updated = messageRepository.markDelivered(request.getMessageId());
        if (updated > 0) {
            messagingTemplate.convertAndSend(
                    "/topic/room/" + roomId + "/status",
                    new MessageStatusUpdate(request.getMessageId(), "DELIVERED")
            );
        }
    }

    @MessageMapping("/typing/{roomId}")
    @SendTo("/topic/room/{roomId}/typing")
    public TypingEventDto handleTyping(
            @DestinationVariable String roomId,
            @Payload TypingEventDto event,
            Principal principal
    ) {
        event.setSender(principal.getName()); // never trust the client for identity
        return event;
    }
}

