package com.together.server.controller;

import com.together.server.domain.ActivityRequest;
import com.together.server.domain.Character;
import com.together.server.domain.ChatMessage;
import com.together.server.domain.JoinRequest;
import com.together.server.domain.JoinResult;
import com.together.server.domain.LeaveRequest;
import com.together.server.domain.UserState;
import com.together.server.service.RoomService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RoomController {

  private final RoomService roomService;
  private final SimpMessagingTemplate messaging;

  @MessageMapping("/room/{roomCode}/join")
  public void join(@DestinationVariable String roomCode,
      @Payload JoinRequest req,
      SimpMessageHeaderAccessor accessor) {
    UserState user = new UserState(
        req.getUserId(), req.getNickname(), req.getCharacter(),
        req.getActivity(), accessor.getSessionId()
    );
    JoinResult result = roomService.join(roomCode, user, req.getSyncId());
    switch (result) {
      case ROOM_FULL ->
          messaging.convertAndSend("/topic/join-error/" + req.getSyncId(), "방이 꽉 찼습니다.");
      case CHARACTER_TAKEN ->
          messaging.convertAndSend("/topic/join-error/" + req.getSyncId(),
              "이미 선택된 캐릭터입니다. 다른 캐릭터를 선택해주세요.");
      default -> {}
    }
  }

  @MessageMapping("/room/{roomCode}/activity")
  public void activity(@DestinationVariable String roomCode,
      @Payload ActivityRequest req) {
    roomService.updateActivity(roomCode, req.getUserId(), req.getActivity());
  }

  @MessageMapping("/room/{roomCode}/chat")
  public void chat(@DestinationVariable String roomCode,
      @Payload ChatMessage message) {
    roomService.chat(roomCode, message);
  }

  @MessageMapping("/room/{roomCode}/leave")
  public void leave(@DestinationVariable String roomCode,
      @Payload LeaveRequest req) {
    roomService.leave(roomCode, req.getUserId());
  }

  @GetMapping("/api/room/{roomCode}/characters")
  public List<Character> getTakenCharacters(@PathVariable String roomCode) {
    return roomService.getTakenCharacters(roomCode);
  }
}
