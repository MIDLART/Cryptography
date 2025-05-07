package org.server.services;

import lombok.extern.slf4j.Slf4j;
import org.server.rabbitmq.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MessageConsumer {
  @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
  public void receiveMessage(String message) {
    log.info("Получено сообщение из очереди: {}", message);
  }
}
