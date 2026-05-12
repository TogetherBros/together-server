package com.together.server.repository;

import com.together.server.domain.Character;
import com.together.server.domain.RoomSession;
import com.together.server.domain.UserState;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class RoomRepository {

  private final Map<String, RoomSession> rooms = new ConcurrentHashMap<>();
  private final Map<String, String> sessionIndex = new ConcurrentHashMap<>();

  public boolean isFull(String roomCode) {
    RoomSession room = rooms.get(roomCode);
    return room != null && room.isFull();
  }

  public boolean containsUser(String roomCode, String userId) {
    RoomSession room = rooms.get(roomCode);
    return room != null && room.containsUser(userId);
  }

  public void addUser(String roomCode, UserState user) {
    RoomSession room = rooms.get(roomCode);
    if (room == null) {
      room = new RoomSession(roomCode);
      rooms.put(roomCode, room);
    }
    room.addUser(user);
    sessionIndex.put(user.getSessionId(), roomCode);
  }

  public void removeUser(String roomCode, String userId) {
    RoomSession room = rooms.get(roomCode);
    if (room != null) {
      for (UserState u : room.getUserList()) {
        if (u.getUserId().equals(userId)) {
          sessionIndex.remove(u.getSessionId());
          break;
        }
      }
      room.removeUser(userId);
      if (room.isEmpty()) rooms.remove(roomCode);
    }
  }
  public void removeBySessionId(String sessionId) {
    String roomCode = sessionIndex.remove(sessionId);
    if (roomCode != null) {
      RoomSession room = rooms.get(roomCode);
      if (room != null) {
        room.removeByUserId(sessionId);
        if (room.isEmpty()) rooms.remove(roomCode);
      }
    }
  }

  public Optional<String> findRoomBySessionId(String sessionId) {
    String roomCode = sessionIndex.get(sessionId);
    return Optional.ofNullable(roomCode);
  }

  public boolean isCharacterTaken(String roomCode, Character character, String excludeUserId) {
    RoomSession room = rooms.get(roomCode);
    if (room == null) return false;
    return room.isCharacterTaken(character, excludeUserId);
  }

  public List<Character> getTakenCharacters(String roomCode) {
    RoomSession room = rooms.get(roomCode);
    if (room == null) return List.of();
    return room.getUserList().stream()
        .map(UserState::getCharacter)
        .collect(Collectors.toList());
  }

  public Optional<String> findUserIdBySessionId(String sessionId) {
    String roomCode = sessionIndex.get(sessionId);
    if (roomCode == null) return Optional.empty();
    RoomSession room = rooms.get(roomCode);
    if (room == null) return Optional.empty();
    return room.getUserList().stream()
        .filter(u -> u.getSessionId().equals(sessionId))
        .map(UserState::getUserId)
        .findFirst();
  }

  public List<UserState> getUsers(String roomCode) {
    RoomSession room = rooms.get(roomCode);
    if (room != null) {
      return room.getUserList();
    }
    return List.of();
  }
}
