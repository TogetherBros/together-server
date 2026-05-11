package com.together.server.repository;

import com.together.server.domain.RoomSession;
import com.together.server.domain.UserState;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
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

  public List<UserState> getUsers(String roomCode) {
    RoomSession room = rooms.get(roomCode);
    if (room != null) {
      return room.getUserList();
    }
    return List.of();
  }
}
