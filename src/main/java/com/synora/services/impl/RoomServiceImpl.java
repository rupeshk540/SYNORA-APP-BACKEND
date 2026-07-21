package com.synora.services.impl;

import com.synora.entities.Room;
import com.synora.repositories.RoomRepository;
import com.synora.services.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoomServiceImpl implements RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Override
    public Room createRoom(String roomId) {
        Room room = new Room();
        room.setRoomId(roomId);
        Room savedRoom = roomRepository.save(room);
        return savedRoom;
    }

    @Override
    public Room findByRoomId(String roomId) {
        Room room = roomRepository.findByRoomId(roomId);
        return room;
    }

    @Override
    public Room saveRoom(Room room) {
        return roomRepository.save(room);
    }
}
