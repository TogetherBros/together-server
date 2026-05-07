package com.together.server.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ChatMessage {
  private String userId;
  private String nickname;
  private Character character;
  private String message;
  private LocalDateTime sentAt;
}