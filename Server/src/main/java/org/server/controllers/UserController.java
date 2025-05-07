package org.server.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.server.services.MessageProducer;
import org.server.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Log4j2
@Controller
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;
  private final MessageProducer messageProducer;

  @PostMapping("/send-message")
  public ResponseEntity<String> sendMessage(@RequestBody Map<String, String> request) {
    String message = request.get("message");
    if (message == null || message.trim().isEmpty()) {
      return ResponseEntity.badRequest().body("Сообщение не может быть пустым");
    }

    messageProducer.sendMessage(message);
    return ResponseEntity.ok("Сообщение отправлено в очередь");
  }

  @GetMapping("/meow")
  public ResponseEntity<String> meow() {
    return ResponseEntity.ok("meow");
  }
}
