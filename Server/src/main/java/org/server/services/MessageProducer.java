package org.server.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.server.configurations.RabbitMQConfig;
import org.server.dto.*;
import org.server.models.ChatSettings;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

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

  public void sendFileMessage(String sender, String recipient, byte[] fileName,
                              byte[] fileContent, int chunkNumber, int totalChunks, UUID fileId) {

    String queueName = RabbitMQConfig.QUEUE_PREFIX + recipient;

    try {
      String jsonMessage = objectMapper.writeValueAsString(
              new ChatFileMessage(sender, fileId, fileName, fileContent, chunkNumber, totalChunks));
      String queueMessage = objectMapper.writeValueAsString(new QueueMessage("ChatFileMessage", jsonMessage));

      rabbitTemplate.convertAndSend(queueName, queueMessage);
    } catch (JsonProcessingException e) {
      log.error("Parsing error {}", e.getMessage());
    }

    log.info("Отправлено в очередь '{}' | От: {} | Для: {} | Фвйл: {} [{}/{}]",
            queueName, sender, recipient, fileName, chunkNumber, totalChunks);
  }

  public void sendInvitation(InvitationRequest request) {
    Invitation invitation = request.getInvitation();
    ChatSettings chatSettings = request.getChatSettings();
    String recipient = chatSettings.getRecipient();

    String queueName = RabbitMQConfig.QUEUE_PREFIX + recipient;

    try {
      String jsonMessage = objectMapper.writeValueAsString(request);
      String queueMessage = objectMapper.writeValueAsString(new QueueMessage("Invitation", jsonMessage));

      rabbitTemplate.convertAndSend(queueName, queueMessage);
    } catch (JsonProcessingException e) {
      log.error("Parsing error {}", e.getMessage());
    }

    log.info("Отправлено приглашение | От: {} | Для: {}", invitation.getSender(), recipient);
  }

  public void sendDeleteChat(DeleteRequest request) {
    String recipient = request.getRecipient();

    String queueName = RabbitMQConfig.QUEUE_PREFIX + recipient;

    try {
      String jsonMessage = objectMapper.writeValueAsString(request);
      String queueMessage = objectMapper.writeValueAsString(new QueueMessage("Delete", jsonMessage));

      rabbitTemplate.convertAndSend(queueName, queueMessage);
    } catch (JsonProcessingException e) {
      log.error("Parsing error {}", e.getMessage());
    }

    log.info("Отправлено сообщение о удаление | От: {} | Для: {}", request.getSender(), recipient);
  }
}
