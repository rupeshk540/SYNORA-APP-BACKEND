package com.synora.services.impl;

import com.synora.dto.MyRoomResponse;
import com.synora.dto.ReadReceiptUpdate;
import com.synora.entities.Message;
import com.synora.entities.Room;
import com.synora.entities.RoomMembership;
import com.synora.entities.User;
import com.synora.repositories.MessageRepository;
import com.synora.repositories.RoomMembershipRepository;
import com.synora.repositories.RoomRepository;
import com.synora.repositories.UserRepository;
import com.synora.services.MembershipService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MembershipServiceImpl implements MembershipService {

    @Autowired private RoomMembershipRepository membershipRepository;
    @Autowired private RoomRepository roomRepository;
    @Autowired private MessageRepository messageRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private SimpMessagingTemplate messagingTemplate;

    @Override
    public RoomMembership joinRoom(Long userId, String roomId) {
        return membershipRepository.findByUserIdAndRoomId(userId, roomId)
                .orElseGet(() -> {
                    RoomMembership membership = new RoomMembership();
                    membership.setUserId(userId);
                    membership.setRoomId(roomId);
                    return membershipRepository.save(membership);
                });
    }

    @Override
    public List<MyRoomResponse> getMyRooms(Long userId) {
        List<RoomMembership> memberships = membershipRepository.findByUserId(userId);

        List<String> roomIds = memberships.stream().map(RoomMembership::getRoomId).toList();
        Map<String, Room> roomsById = roomRepository.findByRoomIdIn(roomIds).stream()
                .collect(Collectors.toMap(Room::getRoomId, r -> r));

        return memberships.stream().map(m -> {
                    Room room = roomsById.get(m.getRoomId());
                    String displayName = (room != null && room.getName() != null) ? room.getName() : m.getRoomId();

                    long unread = messageRepository.countByRoomIdAndTimestampAfter(m.getRoomId(), m.getLastReadAt());
                    long memberCount = membershipRepository.countByRoomId(m.getRoomId());
                    Optional<Message> lastMessage = messageRepository.findTopByRoomIdOrderByTimestampDesc(m.getRoomId());

                    return new MyRoomResponse(
                            m.getRoomId(), displayName,
                            room != null ? room.getCreatedAt() : null,
                            unread, m.getLastReadAt(), memberCount,
                            lastMessage.map(Message::getContent).orElse(null),
                            lastMessage.map(Message::getSender).orElse(null),
                            lastMessage.map(Message::getTimestamp).orElse(null)
                    );
                })
                .sorted(Comparator.comparing(MyRoomResponse::getLastMessageAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }
    @Override
    @Transactional
    public void markAsRead(Long userId, String roomId) {
        membershipRepository.findByUserIdAndRoomId(userId, roomId)
                .ifPresent(m -> {
                    Instant now = Instant.now();
                    m.setLastReadAt(now);
                    membershipRepository.save(m);

                    User reader = userRepository.findById(userId).orElse(null);
                    if (reader != null) {
                        int updated = messageRepository.markSeenUpTo(roomId, reader.getDisplayName(), now);
                        if (updated > 0) {
                            messagingTemplate.convertAndSend(
                                    "/topic/room/" + roomId + "/status",
                                    new ReadReceiptUpdate(roomId, reader.getDisplayName(), now)
                            );
                        }
                    }
                });
    }
}
