package com.synora.repositories;

import com.synora.entities.RoomMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface RoomMembershipRepository extends JpaRepository<RoomMembership, Long> {
    List<RoomMembership> findByUserId(Long userId);
    Optional<RoomMembership> findByUserIdAndRoomId(Long userId, String roomId);
    long countByRoomId(String roomId);
}
