package org.server.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.server.configurations.RabbitMQConfig;
import org.server.dto.ChatMessage;
import org.server.dto.Invitation;
import org.server.dto.QueueMessage;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageProducer {
  private final AmqpTemplate rabbitTemplate;
  private final ObjectMapper objectMapper;

  public void sendMessage(String sender, String recipient, byte[] message) {
    String queueName = RabbitMQConfig.QUEUE_PREFIX + recipient;

    try {
      String jsonMessage = objectMapper.writeValueAsString(new ChatMessage(sender, message));
      String queueMessage = objectMapper.writeValueAsString(new QueueMessage("ChatMessage", jsonMessage));

      rabbitTemplate.convertAndSend(queueName, queueMessage);
    } catch (JsonProcessingException e) {
      log.error("Parsing error {}", e.getMessage());
    }

    log.info("Отправлено в очередь '{}' | От: {} | Для: {} | Сообщение: {}",
            queueName, sender, recipient, message);
  }

  public void sendInvitation(String recipient, Invitation invitation) {
    String queueName = RabbitMQConfig.QUEUE_PREFIX + recipient;

    try {
      String jsonMessage = objectMapper.writeValueAsString(invitation);
      String queueMessage = objectMapper.writeValueAsString(new QueueMessage("Invitation", jsonMessage));

      rabbitTemplate.convertAndSend(queueName, queueMessage);
    } catch (JsonProcessingException e) {
      log.error("Parsing error {}", e.getMessage());
    }

    log.info("Отправлено приглашение | От: {} | Для: {}", invitation.getSender(), recipient);
  }
}
