package com.luvsic.mq;

import com.luvsic.constant.MessageConstant;
import com.luvsic.entity.InfoTemplateReview;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * @Author: zyy
 * @Date: 2024/4/26 15:46
 * @Version:
 * @Description:
 */
@Slf4j
@Component
public class ReviewTaskProducer {
    private final RabbitTemplate rabbitTemplate;
    public ReviewTaskProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
    public void sendReviewTask(InfoTemplateReview task) {
        rabbitTemplate.convertAndSend(MessageConstant.DOMAIN_REGISTRATION_INFO_TEMPLATE_REVIEW_DQUEUE, task);
        log.warn("{}-{} 认证任务已发布", task.getRid(), task.getTldName());
    }
}
