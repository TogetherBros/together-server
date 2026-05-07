package com.together.server.controller;

import com.together.server.domain.ActivityRequest;
import com.together.server.domain.ChatMessage;
import com.together.server.domain.LeaveRequest;
import com.together.server.domain.UserState;
import com.together.server.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class RoomController {

  private final RoomService roomService;
  private final SimpMessagingTemplate messaging;
  // 입장
  @MessageMapping("/room/{roomCode}/join")
  public void join(@DestinationVariable String roomCode,
      @Payload UserState user,
      SimpMessageHeaderAccessor accessor) {
    user.setSessionId(accessor.getSessionId());
    boolean joined = roomService.join(roomCode, user);
    if (!joined) {
      // 인원 초과 시 해당 유저에게만 에러 전송
      messaging.convertAndSendToUser(
          accessor.getSessionId(),
          "/queue/error",
          "방이 꽉 찼습니다."
      );
    }
  }

  // 활동 업데이트
  @MessageMapping("/room/{roomCode}/activity")
  public void activity(@DestinationVariable String roomCode,
      @Payload ActivityRequest req) {
    roomService.updateActivity(roomCode, req.getUserId(), req.getActivity());
  }

  // 채팅
  @MessageMapping("/room/{roomCode}/chat")
  public void chat(@DestinationVariable String roomCode,
      @Payload ChatMessage message) {
    roomService.chat(roomCode, message);
  }

  // 퇴장
  @MessageMapping("/room/{roomCode}/leave")
  public void leave(@DestinationVariable String roomCode,
      @Payload LeaveRequest req) {
    roomService.leave(roomCode, req.getUserId());
  }
}