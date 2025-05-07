package org.server.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.server.rabbitmq.RabbitMQConfig;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageProducer {
  private final AmqpTemplate rabbitTemplate;

  public void sendMessage(String message) {
    rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, message);
    log.info("Сообщение отправлено: {}", message);
  }
}
