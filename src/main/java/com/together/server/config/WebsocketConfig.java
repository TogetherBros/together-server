package com.together.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {

  private static final String[] ALLOWED_ORIGINS = {
      "http://localhost:1420",   // Tauri dev
      "tauri://localhost",       // Tauri prod (Windows / Linux)
      "https://tauri.localhost", // Tauri prod (macOS / iOS)
      "http://tauri.localhost",  // Tauri prod (macOS / iOS, http)
  };

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws")
        .setAllowedOrigins(ALLOWED_ORIGINS);
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.enableSimpleBroker("/topic");
    registry.setApplicationDestinationPrefixes("/app");
  }

}
