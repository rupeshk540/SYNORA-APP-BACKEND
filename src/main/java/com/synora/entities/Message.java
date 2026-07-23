package com.synora.entities;


import com.synora.entities.enums.MessageStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "messages", indexes = {
        @Index(name = "idx_room_created", columnList = "room_id, created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private String roomId;

    private String sender;

    @Column(columnDefinition = "TEXT" ,nullable = false)
    private String content;

    @Column(name = "created_at")
    private Instant timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageStatus status = MessageStatus.SENT;

    @PrePersist
    protected void onCreate() {
        this.timestamp = Instant.now();
    }
}
