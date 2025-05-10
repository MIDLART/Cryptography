package org.server.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.server.configurations.RabbitMQConfig;
import org.server.dto.ChatMessage;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageConsumer {
  private final RabbitAdmin rabbitAdmin;

  private final RabbitTemplate rabbitTemplate;
  private final ObjectMapper objectMapper;

//  private Connection connection;
//  private Channel channel;
//
//  @PostConstruct
//  public void initialize() {
//    try {
//      ConnectionFactory connectionFactory = rabbitTemplate.getConnectionFactory();
//      connection = connectionFactory.createConnection();
//      channel = connection.createChannel();
//    } catch (IOException | TimeoutException e) {
//      log.error("Failed to initialize RabbitMQ connection and channel: {}", e.getMessage());
//    }
//  }


  public void createQueue(String recipient) {
    String queueName = RabbitMQConfig.QUEUE_PREFIX + recipient;

    if (!queueExists(queueName)) {
      Queue queue = new Queue(queueName, true, false, false);
      rabbitAdmin.declareQueue(queue);
      log.info("Queue created: {}", queueName);
    }
  }

//  public ChatMessage getMessageFromQueue(String username) {
//    String queueName = RabbitMQConfig.QUEUE_PREFIX + username;
//
//    if (!queueExists(queueName)) {
//      return null;
//    }
//
//    ChatMessage message = null;
//
//    GetResponse response = null;
//    try {
//      response = channel.basicGet(queueName, false);
//    } catch (IOException e) {
//      log.error("Error getting message from queue: {}.", queueName);
//    }
//
//    if (response == null) {
//      return null;
//    }
//
//    byte[] body = response.getBody();
//    String msg = new String(body, StandardCharsets.UTF_8);
//
//    try {
//      message = objectMapper.readValue(msg, ChatMessage.class);
//    } catch (IOException e) {
//      log.error("Error deserializing message from queue: {}.", queueName);
//    }
//
//    return message;
//  }

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

          messages.add(chatMessage);
        } catch (IOException e) {
          log.error("Error deserializing message from queue: {}. Message body: {}", queueName, new String(message.getBody()));
        }
      }
    } while (message != null);

    return messages;
  }

//  public void deleteMessage(long deliveryTag) {
//    try {
//      channel.basicAck(deliveryTag, false);
//    } catch (IOException e) {
//      log.error("Error deleting message from queue");
//    }
//  }

  public boolean queueExists(String queueName) {
    try {
      Properties queueProperties = rabbitAdmin.getQueueProperties(queueName);
      return queueProperties != null;
    } catch (Exception e) {
      return false;
    }
  }
}
