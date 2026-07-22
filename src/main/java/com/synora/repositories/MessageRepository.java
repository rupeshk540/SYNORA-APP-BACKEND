package com.synora.repositories;

import com.synora.entities.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByRoomIdOrderByTimestampDesc(String roomId, Pageable pageable);
    long countByRoomIdAndTimestampAfter(String roomId, Instant after);
    Optional<Message> findTopByRoomIdOrderByTimestampDesc(String roomId);
}