package org.server.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.server.configurations.RabbitMQConfig;
import org.server.dto.ChatMessage;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageConsumer {
  private final RabbitAdmin rabbitAdmin;

  private final RabbitTemplate rabbitTemplate;
  private final ObjectMapper objectMapper;

  public void createQueue(String recipient) {
    String queueName = RabbitMQConfig.QUEUE_PREFIX + recipient;

    if (!queueExists(queueName)) {
      Queue queue = new Queue(queueName, true, false, false);
      rabbitAdmin.declareQueue(queue);
      log.info("Queue created: {}", queueName);
    }
  }

  public List<ChatMessage> getAllMessagesFromQueue(String username) {
    String queueName = RabbitMQConfig.QUEUE_PREFIX + username;

    if (!queueExists(queueName)) {
      return new ArrayList<>();
    }

    List<ChatMessage> messages = new ArrayList<>();
    Message message;

    do {
      message = rabbitTemplate.receive(queueName);
      if (message != null) {
        try {
          ChatMessage chatMessage = objectMapper.readValue(message.getBody(), ChatMessage.class);
//          chatMessage.setDeliveryTag(message.getMessageProperties().getDeliveryTag());

          messages.add(chatMessage);
        } catch (IOException e) {
          log.error("Error deserializing message from queue: {}. Message body: {}", queueName, new String(message.getBody()));
        }
      }
    } while (message != null);

    return messages;
  }

  public boolean queueExists(String queueName) {
    try {
      Properties queueProperties = rabbitAdmin.getQueueProperties(queueName);
      return queueProperties != null;
    } catch (Exception e) {
      return false;
    }
  }
}
