package com.dzenthai.cryptora.analyze.facade;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class MessageSender {

    private final AmqpTemplate amqpTemplate;

    @Value("${queue.name}")
    private String queueName;

    public MessageSender(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    public void send(String message) {
        amqpTemplate.convertAndSend(queueName, message);
    }
}
