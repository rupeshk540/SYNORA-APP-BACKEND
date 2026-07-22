package com.synora.services;

import com.synora.entities.Room;

public interface RoomService {

    Room createRoom(String roomId, String name);

    Room findByRoomId(String roomId);

    Room saveRoom(Room room);
}