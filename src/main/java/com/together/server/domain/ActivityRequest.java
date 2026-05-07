package com.together.server.domain;

import lombok.Data;

@Data
public class ActivityRequest {
  private String userId;
  private String activity;
}