package com.synora.config;

import com.synora.services.impl.PresenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Component
public class PresenceEventListener {

    @Autowired private PresenceService presenceService;
    @Autowired private SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleSubscribe(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();

        // only the bare message topic counts as "entered the room" -
        // ignore subscriptions to /status, /typing, /presence on the same room
        if (destination == null || !destination.matches("^/topic/room/[^/]+$")) return;

        String roomId = destination.substring("/topic/room/".length());
        if (accessor.getUser() == null) return;
        String displayName = accessor.getUser().getName();

        var online = presenceService.markOnline(accessor.getSessionId(), roomId, displayName);
        messagingTemplate.convertAndSend("/topic/room/" + roomId + "/presence", online);
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        presenceService.markOffline(accessor.getSessionId()).ifPresent(change ->
                messagingTemplate.convertAndSend("/topic/room/" + change.roomId() + "/presence", change.onlineUsers())
        );
    }
}
