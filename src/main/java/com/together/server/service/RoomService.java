package com.together.server.service;

import com.together.server.domain.ChatMessage;
import com.together.server.domain.UserState;
import com.together.server.repository.RoomRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomService {

  private final RoomRepository roomRepository;
  private final SimpMessagingTemplate messaging;

  public synchronized boolean join(String roomCode, UserState user) {
    boolean alreadyInRoom = roomRepository.containsUser(roomCode, user.getUserId());
    if (!alreadyInRoom && roomRepository.isFull(roomCode)) {
      return false; // 신규 진입자만 차단, 기존 유저 재배정은 허용
    }
    roomRepository.addUser(roomCode, user);
    broadcast(roomCode);
    return true;
  }

  public void updateActivity(String roomCode, String userId, String activity) {
    List<UserState> users = roomRepository.getUsers(roomCode);
    for (UserState u : users) {
      if (u.getUserId().equals(userId)) {
        u.setActivity(activity);
        break;
      }
    }
    broadcast(roomCode);
  }

  public void chat(String roomCode, ChatMessage message) {
    message.setSentAt(LocalDateTime.now()); // 서버에서 시간 주입
    messaging.convertAndSend("/topic/room/" + roomCode + "/chat", message);
    // 채팅은 유저 목록 broadcast 불필요
  }

  public void leave(String roomCode, String userId) {
    roomRepository.removeUser(roomCode, userId);
    broadcast(roomCode);
  }

  public void disconnected(String sessionId) {
    Optional<String> roomCode = roomRepository.findRoomBySessionId(sessionId);
    if (roomCode.isPresent()) {
      roomRepository.removeBySessionId(sessionId);
      broadcast(roomCode.get());
    }
  }

  private void broadcast(String roomCode) {
    List<UserState> users = roomRepository.getUsers(roomCode);
    messaging.convertAndSend("/topic/room/" + roomCode, users);
  }
}