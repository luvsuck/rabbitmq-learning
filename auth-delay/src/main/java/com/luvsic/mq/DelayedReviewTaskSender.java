package com.luvsic.mq;

import com.luvsic.entity.InfoTemplateReview;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * @Author: zyy
 * @Date: 2024/4/29 17:00
 * @Version:
 * @Description:
 */

@Slf4j
@Component
public class DelayedReviewTaskSender {
    private final RabbitTemplate rabbitTemplate;

    public DelayedReviewTaskSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMsg(InfoTemplateReview data) {
        rabbitTemplate.setMandatory(true);
        rabbitTemplate.convertAndSend(DelayedReviewTaskQueueConfig.REVIEW_TASK_DELAYED_EXCHANGE, DelayedReviewTaskQueueConfig.REVIEW_TASK_DELAYED_ROUTING_KEY, data, processor -> {
            processor.getMessageProperties().setDelay(data.getDelay());
            return processor;
        }, new CorrelationData(data.getTaskId() + ""));
        log.warn("任务{}已发送.延迟{}(ms)", data.getTaskId(), data.getDelay());
    }
}
