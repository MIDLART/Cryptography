package org.server.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.server.dto.ChatMessage;
import org.server.dto.MessageRequest;
import org.server.services.MessageConsumer;
import org.server.services.MessageProducer;
import org.server.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Log4j2
@Controller
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;
  private final MessageProducer messageProducer;
  private final MessageConsumer messageConsumer;

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

    messageConsumer.createQueue(recipient);

    messageProducer.sendMessage(username, recipient, message);
    return ResponseEntity.ok("Сообщение отправлено в очередь");
  }

  @GetMapping("/load-message")
  public ResponseEntity<List<ChatMessage>> loadMessage(@RequestParam("username") String username) {
    List<ChatMessage> messages = messageConsumer.getAllMessagesFromQueue(username);
    log.info("Найдено сообщений: {}", messages.size());

    messages.forEach(msg -> log.debug("Отправляем сообщение: {}", msg));

    return ResponseEntity.ok(messages);
  }

  @GetMapping("/meow")
  public ResponseEntity<String> meow() {
    return ResponseEntity.ok("meow");
  }
}
