package com.luvsic.mq;

import com.luvsic.constant.MessageConstant;
import com.luvsic.entity.InfoTemplateReview;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;


/**
 * @Author: zyy
 * @Date: 2024/4/26 15:49
 * @Version:
 * @Description:
 */
@Slf4j
@Component
@RabbitListener(queues = MessageConstant.DOMAIN_REGISTRATION_INFO_TEMPLATE_REVIEW_DQUEUE)
public class ReviewTaskConsumer2 {
    @RabbitHandler
    public void receiveAndHandlerReview(InfoTemplateReview task, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        log.warn("消费者2-收到消息:{}-{}-", deliveryTag, task);
        try {
            doWork(task, channel, deliveryTag);
            log.warn("消费者2-执行结束-{}-{}-{}", deliveryTag, task.getRid(), task.getTldName());
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (!StringUtils.hasText(task.getOwnerFullname()) || !task.getOwnerFullname().contains("张三")) {
                channel.basicAck(deliveryTag, false);
                log.warn("消费者2-消费完成，消息出队:{}-{}-", deliveryTag, task);
            }
        }
    }

    private void doWork(InfoTemplateReview task, Channel channel, long deliverTag) throws InterruptedException, IOException {
        if (StringUtils.hasText(task.getOwnerFullname()) && task.getOwnerFullname().contains("张三")) {
            //休眠一秒,重新进入队列
            Thread.sleep(1000);
            channel.basicReject(deliverTag, true);
            log.warn("消息:{}被拒绝消费，已重新入队", task);
        }
    }
}