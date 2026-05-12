package com.together.server.service;

import com.together.server.domain.Character;
import com.together.server.domain.ChatMessage;
import com.together.server.domain.JoinResult;
import com.together.server.domain.UserState;
import com.together.server.repository.RoomRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RoomService {

  private final RoomRepository roomRepository;
  private final SimpMessagingTemplate messaging;

  public synchronized JoinResult join(String roomCode, UserState user, String syncId) {
    boolean alreadyInRoom = roomRepository.containsUser(roomCode, user.getUserId());

    if (!alreadyInRoom) {
      if (roomRepository.isFull(roomCode)) return JoinResult.ROOM_FULL;
      if (roomRepository.isCharacterTaken(roomCode, user.getCharacter(), user.getUserId())) {
        return JoinResult.CHARACTER_TAKEN;
      }
    }

    roomRepository.addUser(roomCode, user);
    sync(syncId, roomCode);

    if (!alreadyInRoom) {
      messaging.convertAndSend("/topic/room/" + roomCode + "/join", user);
    }

    return JoinResult.OK;
  }

  public void updateActivity(String roomCode, String userId, String activity) {
    List<UserState> users = roomRepository.getUsers(roomCode);
    for (UserState u : users) {
      if (u.getUserId().equals(userId)) {
        u.setActivity(activity);
        break;
      }
    }
    messaging.convertAndSend("/topic/room/" + roomCode + "/activity",
        (Object) Map.of("userId", userId, "activity", activity));
  }

  public void chat(String roomCode, ChatMessage message) {
    message.setSentAt(LocalDateTime.now());
    messaging.convertAndSend("/topic/room/" + roomCode + "/chat", message);
  }

  public void leave(String roomCode, String userId) {
    roomRepository.removeUser(roomCode, userId);
    messaging.convertAndSend("/topic/room/" + roomCode + "/leave",
        (Object) Map.of("userId", userId));
  }

  public void disconnected(String sessionId) {
    Optional<String> roomCode = roomRepository.findRoomBySessionId(sessionId);
    if (roomCode.isPresent()) {
      String userId = roomRepository.findUserIdBySessionId(sessionId).orElse(null);
      roomRepository.removeBySessionId(sessionId);
      if (userId != null) {
        messaging.convertAndSend("/topic/room/" + roomCode.get() + "/leave",
            (Object) Map.of("userId", userId));
      }
    }
  }

  public List<Character> getTakenCharacters(String roomCode) {
    return roomRepository.getTakenCharacters(roomCode);
  }

  private void sync(String syncId, String roomCode) {
    if (syncId == null || syncId.isBlank()) return;
    List<UserState> users = roomRepository.getUsers(roomCode);
    messaging.convertAndSend("/topic/join-sync/" + syncId, users);
  }
}
