package com.together.server.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;

@Data
public class RoomSession {
  private final String roomCode;
  private final Map<String, UserState> users = new ConcurrentHashMap<>();

  public boolean isFull() {
    return users.size() >= Character.MAX_ROOM_SIZE;
  }

  public void addUser(UserState user) {
    users.put(user.getUserId(), user);
  }

  public void removeUser(String userId) {
    users.remove(userId);
  }

  public void removeByUserId(String sessionId) {
    users.values().removeIf(u -> u.getSessionId().equals(sessionId));
  }

  public List<UserState> getUserList() {
    return new ArrayList<>(users.values());
  }

  public boolean isEmpty() {
    return users.isEmpty();
  }
}
