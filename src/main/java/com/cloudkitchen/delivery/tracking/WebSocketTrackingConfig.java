package com.cloudkitchen.delivery.tracking;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket STOMP configuration for live delivery tracking.
 * <p>
 * Exposes a STOMP endpoint at /ws/tracking and a simple broker with
 * topic destination prefix /topic.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketTrackingConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/tracking")
                .setAllowedOriginPatterns("*")
                // Enable SockJS support so SockJS JavaScript client can connect
                .withSockJS();
    }
}



