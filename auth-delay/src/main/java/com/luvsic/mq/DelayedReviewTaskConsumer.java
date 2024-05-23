package com.luvsic.mq;

import com.luvsic.entity.InfoTemplateReview;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * @Author: zyy
 * @Date: 2024/4/29 17:11
 * @Version:
 * @Description:
 */
@Slf4j
@Component
public class DelayedReviewTaskConsumer {
    @RabbitListener(queues = DelayedReviewTaskQueueConfig.REVIEW_TASK_DELAYED_QUEUE)
    public void handlerMsg(InfoTemplateReview task, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        log.warn("消费者1收到任务{},tag:{},执行处理", task.getTaskId(), deliveryTag);
        try {
            doWork(task, channel, deliveryTag);
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        } finally {
            channel.basicAck(deliveryTag, false);
            log.warn("消费者1执行任务{}结束，任务出队", task.getTaskId());
        }
    }

    private void doWork(InfoTemplateReview task, Channel channel, long deliverTag) throws InterruptedException, IOException {
        if (StringUtils.hasText(task.getOwnerFullname()) && task.getOwnerFullname().contains("张三")) {
            //休眠一秒,重新进入队列
            Thread.sleep(1000);
//            channel.basicReject(deliverTag, true);
            log.warn("消息:{}被拒绝消费，已重新入队", task);
        }
    }
}
