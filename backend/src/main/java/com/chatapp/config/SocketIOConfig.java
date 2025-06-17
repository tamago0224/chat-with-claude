package com.chatapp.config;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@org.springframework.context.annotation.Configuration
public class SocketIOConfig {

    @Value("${socketio.hostname:localhost}")
    private String hostname;

    @Value("${socketio.port:8081}")
    private Integer port;

    @Value("${socketio.cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

    @Bean
    @Profile("!test")
    public SocketIOServer socketIOServer() {
        Configuration config = new Configuration();
        config.setHostname(hostname);
        config.setPort(port);
        
        // CORS configuration
        config.setOrigin(allowedOrigins);
        
        // Connection configuration
        config.setMaxFramePayloadLength(1024 * 1024); // 1MB
        config.setMaxHttpContentLength(1024 * 1024); // 1MB
        config.setPingTimeout(60000); // 60 seconds
        config.setPingInterval(25000); // 25 seconds
        
        // Authentication configuration will be added later
        
        return new SocketIOServer(config);
    }
}