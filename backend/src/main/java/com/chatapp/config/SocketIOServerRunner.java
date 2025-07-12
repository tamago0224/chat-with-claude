package com.chatapp.config;

import com.chatapp.socket.SocketIOEventHandler;
import com.corundumstudio.socketio.SocketIOServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class SocketIOServerRunner implements CommandLineRunner {

  private static final Logger logger = LoggerFactory.getLogger(SocketIOServerRunner.class);

  @Autowired private SocketIOServer socketIOServer;

  @Autowired private SocketIOEventHandler socketIOEventHandler;

  @Override
  public void run(String... args) throws Exception {
    socketIOEventHandler.addEventListeners();
    socketIOServer.start();
    logger.info("Socket.IO server started on port {}", socketIOServer.getConfiguration().getPort());

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  logger.info("Stopping Socket.IO server...");
                  socketIOServer.stop();
                }));
  }
}
