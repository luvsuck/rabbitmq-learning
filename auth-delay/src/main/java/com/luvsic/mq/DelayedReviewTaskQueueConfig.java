package com.luvsic.mq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;


/**
 * @Author: zyy
 * @Date: 2024/4/29 16:40
 * @Version:
 * @Description:
 */
@Configuration
public class DelayedReviewTaskQueueConfig {
    private final ConnectionFactory connectionFactory;

    public DelayedReviewTaskQueueConfig(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public static final String REVIEW_TASK_DELAYED_QUEUE = "review-task-delayed-queue";
    public static final String REVIEW_TASK_DELAYED_EXCHANGE = "review-task-delayed-exchange";
    public static final String REVIEW_TASK_DELAYED_ROUTING_KEY = "review-task-delayed_routing_key";

    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter());
        return template;
    }

    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue reviewTaskDelayedQueue() {
        return new Queue(REVIEW_TASK_DELAYED_QUEUE);
    }

    @Bean
    public CustomExchange reviewTaskDelayedExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");
        return new CustomExchange(REVIEW_TASK_DELAYED_EXCHANGE, "x-delayed-message", true, false, args);
    }

    @Bean
    public Binding bindingDelayedQueue(@Qualifier("reviewTaskDelayedQueue") Queue queue,
                                       @Qualifier("reviewTaskDelayedExchange") CustomExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(REVIEW_TASK_DELAYED_ROUTING_KEY).noargs();
    }
}
