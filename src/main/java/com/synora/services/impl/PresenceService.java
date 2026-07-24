package com.synora.services.impl;


import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.*;

@Service
public class PresenceService {

    private final Map<String, Set<String>> roomPresence = new ConcurrentHashMap<>();
    private final Map<String, SessionInfo> sessionRegistry = new ConcurrentHashMap<>();

    public record SessionInfo(String roomId, String displayName) {}
    public record PresenceChange(String roomId, Set<String> onlineUsers) {}

    public Set<String> markOnline(String sessionId, String roomId, String displayName) {
        roomPresence.computeIfAbsent(roomId, r -> ConcurrentHashMap.newKeySet()).add(displayName);
        sessionRegistry.put(sessionId, new SessionInfo(roomId, displayName));
        return getOnlineUsers(roomId);
    }

    public Optional<PresenceChange> markOffline(String sessionId) {
        SessionInfo info = sessionRegistry.remove(sessionId);
        if (info == null) return Optional.empty();

        Set<String> users = roomPresence.get(info.roomId());
        if (users != null) {
            users.remove(info.displayName());
            if (users.isEmpty()) roomPresence.remove(info.roomId());
        }
        return Optional.of(new PresenceChange(info.roomId(), getOnlineUsers(info.roomId())));
    }

    public Set<String> getOnlineUsers(String roomId) {
        return roomPresence.getOrDefault(roomId, Set.of());
    }
}
