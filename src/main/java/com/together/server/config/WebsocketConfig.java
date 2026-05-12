package com.together.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

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
  public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
    registry.setSendTimeLimit(30 * 1000)
            .setSendBufferSizeLimit(2 * 1024 * 1024);
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
    scheduler.setPoolSize(1);
    scheduler.setThreadNamePrefix("wss-heartbeat-");
    scheduler.initialize();
    registry.enableSimpleBroker("/topic", "/queue")
        .setHeartbeatValue(new long[]{10000, 10000})
        .setTaskScheduler(scheduler);
    registry.setApplicationDestinationPrefixes("/app");
  }

}
