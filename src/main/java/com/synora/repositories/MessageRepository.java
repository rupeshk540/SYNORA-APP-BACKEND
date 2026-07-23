package com.synora.repositories;

import com.synora.entities.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByRoomIdOrderByTimestampDesc(String roomId, Pageable pageable);
    long countByRoomIdAndTimestampAfter(String roomId, Instant after);
    Optional<Message> findTopByRoomIdOrderByTimestampDesc(String roomId);

    @Modifying
    @Query("UPDATE Message m SET m.status = 'DELIVERED' WHERE m.id = :messageId AND m.status = 'SENT'")
    int markDelivered(@Param("messageId") Long messageId);

    @Modifying
    @Query("UPDATE Message m SET m.status = 'SEEN' WHERE m.roomId = :roomId " +
            "AND m.sender <> :reader AND m.timestamp <= :upTo AND m.status <> 'SEEN'")
    int markSeenUpTo(@Param("roomId") String roomId, @Param("reader") String reader, @Param("upTo") Instant upTo);
}