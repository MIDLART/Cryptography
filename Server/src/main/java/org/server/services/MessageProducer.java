package org.server.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.server.configurations.RabbitMQConfig;
import org.server.dto.ChatMessage;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageProducer {
  private final AmqpTemplate rabbitTemplate;
  private final ObjectMapper objectMapper;

  public void sendMessage(String sender, String recipient, String message) {
    String queueName = RabbitMQConfig.QUEUE_PREFIX + recipient;

    try {
      String jsonMessage = objectMapper.writeValueAsString(new ChatMessage(sender, message));
      rabbitTemplate.convertAndSend(queueName, jsonMessage);
    } catch (JsonProcessingException e) {
      log.error("Parsing error {}", e.getMessage());
    }

    log.info("Отправлено в очередь '{}' | От: {} | Для: {} | Сообщение: {}",
            queueName, sender, recipient, message);
  }
}
