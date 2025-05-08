package org.server.controllers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.server.services.MessageProducer;
import org.server.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Log4j2
@Controller
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;
  private final MessageProducer messageProducer;

  @PostMapping("/send-message")
  public ResponseEntity<String> sendMessage(@RequestBody MessageRequest request) {
    String message = request.getMessage();
    String username = request.getSender();
    String recipient = request.getRecipient();

    if (message == null || message.trim().isEmpty()) {
      return ResponseEntity.badRequest().body("Сообщение не может быть пустым");
    }

    if (username == null || username.trim().isEmpty()) {
      return ResponseEntity.badRequest().body("Не указан отправитель");
    }

    if (recipient == null || recipient.trim().isEmpty()) {
      return ResponseEntity.badRequest().body("Не указан получатель");
    }

    if (!userService.findUser(recipient)) {
      return ResponseEntity.badRequest().body("Пользователь не найден");
    }

    log.info("Сообщение от {} для {}: {}", username, recipient, message);

    messageProducer.sendMessage(message);
    return ResponseEntity.ok("Сообщение отправлено в очередь");
  }

  @GetMapping("/meow")
  public ResponseEntity<String> meow() {
    return ResponseEntity.ok("meow");
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class MessageRequest {
    private String sender;
    private String recipient;
    private String message;
  }
}
