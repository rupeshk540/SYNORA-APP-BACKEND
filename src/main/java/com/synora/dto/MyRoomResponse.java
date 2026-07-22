package com.synora.dto;

import lombok.*;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MyRoomResponse {

    private String roomId;
    private String name;
    private Instant createdAt;
    private long unreadCount;
    private Instant lastReadAt;
    private long memberCount;
    private String lastMessageContent;
    private String lastMessageSender;
    private Instant lastMessageAt;
}
