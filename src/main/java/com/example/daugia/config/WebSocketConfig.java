package com.example.daugia.config;

import com.example.daugia.core.custom.TokenValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.http.server.ServerHttpRequest;

import java.security.Principal;
import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private TokenValidator tokenValidator;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-auction")
                .setAllowedOriginPatterns("*")
                .setHandshakeHandler(new DefaultHandshakeHandler() {
                    @Override
                    protected Principal determineUser(ServerHttpRequest request, org.springframework.web.socket.WebSocketHandler wsHandler, Map<String, Object> attributes) {
                        String query = request.getURI().getQuery();
                        System.out.println("WS query: " + query);
                        if (query != null && query.startsWith("token=")) {
                            String token = query.substring(6);
                            String email = tokenValidator.validateAndGetEmailFromToken(token);
                            System.out.println("Token: " + token + " -> email: " + email);
                            if (email != null) {
                                return new Principal() {
                                    @Override
                                    public String getName() {
                                        return email;
                                    }
                                };
                            }
                        }
                        return super.determineUser(request, wsHandler, attributes);
                    }
                })
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }
}