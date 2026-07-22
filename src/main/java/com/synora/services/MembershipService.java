package com.synora.services;

import com.synora.dto.MyRoomResponse;
import com.synora.entities.RoomMembership;

import java.util.List;

public interface MembershipService {

    RoomMembership joinRoom(Long userId, String roomId);
    List<MyRoomResponse> getMyRooms(Long userId);
    void markAsRead(Long userId, String roomId);
}