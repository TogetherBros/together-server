package com.together.server.domain;

import lombok.Data;

@Data
public class JoinRequest {
    private String userId;
    private String nickname;
    private Character character;
    private String activity;
    private String syncId;
}
