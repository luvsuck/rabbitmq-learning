package com.luvsic.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luvsic.entity.InfoTemplateReview;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Author: zyy
 * @Date: 2024/4/30 11:16
 * @Version:
 * @Description:
 */
@Slf4j
@Component
public class MsgConfirmCallback implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnsCallback {
    private final RabbitTemplate rabbitTemplate;

    public MsgConfirmCallback(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnsCallback(this);
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        String id = correlationData != null ? correlationData.getId() : "";
        if (ack) log.error("交换机接收到任务:{}", id);
        else log.warn("交换机未接收到任务{},失败原因{}", id, cause);
    }

    @Override
    public void returnedMessage(ReturnedMessage returned) {
        int replyCode = returned.getReplyCode();
        String replyText = returned.getReplyText();
        String exchange = returned.getExchange();
        String routingKey = returned.getRoutingKey();
        Message msg = returned.getMessage();
        ObjectMapper objectMapper = new ObjectMapper();
        InfoTemplateReview task = null;
        try {
            task = objectMapper.readValue(msg.getBody(), InfoTemplateReview.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (!DelayedReviewTaskQueueConfig.REVIEW_TASK_DELAYED_EXCHANGE.equals(exchange)) {
            log.error("任务{}被交换机:{}回退，回退码:{}，回退原因:{}:路由键{}", task.getTaskId(), exchange, replyCode, replyText, routingKey);
        } else
            log.error("任务{}为延迟队列任务,已排除", task.getTaskId());

    }
}
