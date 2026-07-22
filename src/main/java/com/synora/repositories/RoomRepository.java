package com.synora.repositories;

import com.synora.entities.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    Room findByRoomId(String roomId);
    List<Room> findByRoomIdIn(List<String> roomIds);
}
