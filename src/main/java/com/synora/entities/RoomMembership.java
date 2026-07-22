package com.synora.entities;


import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "room_memberships",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "room_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RoomMembership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "room_id", nullable = false)
    private String roomId;

    @Column(name = "last_read_at")
    private Instant lastReadAt;

    @Column(name = "joined_at")
    private Instant joinedAt;

    @PrePersist
    protected void onCreate() {
        this.joinedAt = Instant.now();
        this.lastReadAt = Instant.now();   // nothing to catch up on for a brand-new member
    }
}