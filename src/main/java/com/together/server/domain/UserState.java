package com.together.server.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserState {
  private String userId;
  private String nickname;
  private Character character;  // String emoji 대신 enum으로
  private String activity;
  private String sessionId;
}
