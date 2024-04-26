import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.TimeoutException;

/**
 * @Author: zyy
 * @Date: 2024/4/23 23:39
 * @Version:
 * @Description:
 */
@Slf4j
public class RabbitConfig {
    /**
     * 创建客户端连接
     *
     * @return Server connection
     */
    public static Connection getConnection() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setVirtualHost("test");
        factory.setUsername("luvsic");
        factory.setPassword("Luvsic..");
        factory.setHost("localhost");
        factory.setPort(5672);
        Connection connection = null;
        try {
            connection = factory.newConnection();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public static void publish() throws IOException, TimeoutException {
        //获取客户端连接
        Connection connection = RabbitConfig.getConnection();
        //创建channel
        Channel channel = connection.createChannel();
        String msg = "Halo World";

        //param1: exchange 指定消息发送到哪台交换机，为空字符串则表示直接发送到队列而不经过交换机
        //param2: routingKey 路由键，对于直接发送到队列的消息，路由键通常和队列名称相同。
        //        通常路由键作用是当生产者发送消息时需要指定消息的路由键以及消息要发送给哪台交换机。交换机会根据路由键把消息路由到一个或多个队列。
        //        若为直接交换机direct exchange,则消息的路由键回纥队列的绑定键binding key进行匹配，若成功匹配，消息被路由到相应队列
        //        若为其他类型交换机，比如扇出交换机fanout exchange或者主题交换机topic exchange，则交换机会根据不同路由规则把消息路由到一个或多个队列
        //param3: props 消息的属性
        //param4: body 消息内容，通常为一个字节数组
        channel.basicPublish("", "testQueue", null, msg.getBytes(StandardCharsets.UTF_8));
        log.warn("生产者发布了一条消息");
        channel.close();
        connection.close();
    }


    //监听键盘输入并且作为消息内容来发送到消息队列
    public static void publishWithInput() throws IOException, TimeoutException {
        Connection connection = RabbitConfig.getConnection();
        Channel channel = connection.createChannel();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("请输入消息: ");
        String msg;
        while (!(msg = reader.readLine()).equalsIgnoreCase("exit")) {
            if (!msg.isEmpty()) {
                channel.basicPublish("", "testQueue", null, msg.getBytes(StandardCharsets.UTF_8));
                log.warn("生产者发布了一条消息");
            } else
                log.warn("未输入消息");
        }
        channel.close();
        connection.close();
        log.warn("发布者退出,再见");
    }


    //简单队列
    public static void consumer() throws IOException, TimeoutException, InterruptedException {
        Connection connection = RabbitConfig.getConnection();
        Channel channel = connection.createChannel();

        //声明队列
        //param1 queue 队列名称
        //param2 durable 开启持久化 rabbitmq服务器重启后队列仍会存在
        //param3 exclusive 是否独占队列 若为true只有当前连接可以用，其他连接不能用
        //param4 autoDelete 自动删除 在最后一个消费者断开连接后，会自动删除这个队列
        //param5 arguments 其他属性
        channel.queueDeclare("testQueue", true, false, false, null);

        //开启一个监听队列
        DefaultConsumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, StandardCharsets.UTF_8);
                log.warn("简单队列消费者 接收到消息{},开始执行", message);
                try {
                    doWork(new String(body));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    log.warn("简单队列消费者 {} 执行结束", message);
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            }
        };
//        channel.basicQos(1);
        //定义消费者
        //param1 queue 指定队列
        //param2 autoAck true收到消息立马告诉rabbitmq false手动告诉
        //param3 callback 消费回调
        channel.basicConsume("testQueue", false, consumer);

        //不释放资源 持续接收消息
        //channel.close();
        //connection.close();
    }

    //工作队列 1个发布者将消息发到一个队列，会有多个消费者来参与消费，但是一条消息只有一个消费者会获取到，这里是幂等性的
    //工作队列有两种模式，默认为轮询，会依次交替任务给消费者们。还有一种为公平分发模式，这个模式执行速度快的消费者会更早的获取到下一条
    //                消息，这个模式必须要消费者们关闭autoAck以及设置qos为1才行
    // autoAck只有设置为false，也就是手动确认(任务执行完了手动ack)之后，消息才能保证不丢，否则在消费的过程中由于自动ack，此时出现消费
    // 宕机就会导致消息丢失。手动确认ack的消费者会在宕机恢复后，重新获取到这条消息来消费
    public static void consumerOfWorkQueue() throws IOException {
        Connection connection = RabbitConfig.getConnection();
        Channel channel = connection.createChannel();
        //指定消费者一次消费1条消息
        channel.basicQos(1);

        //抽象方法写消费者
        DefaultConsumer consumer1 = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, StandardCharsets.UTF_8);
                log.warn("消费者1号接收到消息:{}", message);
                try {
                    doWork(new String(body));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    log.warn("消费者一号执行 {} 结束", message);
                    channel.basicAck(envelope.getDeliveryTag(), false);
                }
            }
        };
        channel.basicConsume("testQueue", false, consumer1);

        //lambda写法起消费者
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            log.warn("消费者2号接收到消息:{}", message);
            try {
                doWork(message);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                log.warn("消费者二号执行 {} 结束", message);
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        };
        channel.basicConsume("testQueue", false, deliverCallback, consumerTag -> {
        });
    }

    //发布订阅队列-扮演日志的生产者
    public static void publisherAndSubscriberSenderWithInput() throws IOException, TimeoutException {
        Connection connection = getConnection();
        Channel channel = connection.createChannel();
        //声明一个名为logs的扇出交换机
        channel.exchangeDeclare("logs", "fanout");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("请输入消息: ");
        String msg;
        while (!(msg = reader.readLine()).equalsIgnoreCase("exit")) {
            if (!msg.isEmpty()) {
                //将消息发到logs交换机，不指定路由键
                channel.basicPublish("logs", "", null, msg.getBytes(StandardCharsets.UTF_8));
            } else log.warn("请输入消息...");
        }
        channel.close();
        connection.close();
        log.warn("logs交换机发布者退出,再见");
    }

    //发布订阅队列-扮演日志的接收者
    public static void publisherAndSubscribeReceiver() throws IOException {
        Connection connection = getConnection();
        Channel channel = connection.createChannel();
        channel.exchangeDeclare("logs", "fanout");

        //queueDeclare()会主动声明一个独占、自动删除、非持久的队列。相当于是一个临时队列。这里因为是日志采集业务，关注的是新的消息，每次伴随一个新的连接进来，
        // 创建一个新的临时日志队列会更合适
        String logQueueName = channel.queueDeclare().getQueue();
        //把临时日志队列绑定到logs交换机上
        channel.queueBind(logQueueName, "logs", "");

        //回调
        DeliverCallback callback = (tag, delivery) -> {
            String msg = new String(delivery.getBody());
            log.warn("消费者1 接收到日志: {},执行存储", msg);
            try {
                doWork(msg);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                log.warn("消费者1 日志存储完毕,存储内容{}", msg);
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        };
        channel.basicConsume(logQueueName, false, callback, consumerTag -> {
        });

        //回调二 模拟消费者二 这里建立一个新的临时队列并且绑定该队列到扇出交换机 因为到目前为止仍然
        // 只有一个队列被绑定到了logs这个交换机上，因此即使是扇出的靠交换机，也只会被一个消费者接收到
        String logQueue2Name = channel.queueDeclare().getQueue();
        channel.queueBind(logQueue2Name, "logs", "");
        DeliverCallback callback2 = (tag, delivery) -> {
            String msg = new String(delivery.getBody());
            log.warn("消费者2 接收到日志: {},执行存储", msg);
            try {
                doWork(msg);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                log.warn("消费者2 日志存储完毕,存储内容{}", msg);
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        };
        channel.basicConsume(logQueue2Name, false, callback2, consumerTag -> {
        });
    }

    //路由队列-消息发布
    public static void routingQueueSender() throws IOException, TimeoutException {
        Connection connection = getConnection();
        Channel channel = connection.createChannel();
        //声明直接交换机
        channel.exchangeDeclare("routing-logs", "direct");
        //把需要的几个队列声明一下
        channel.queueDeclare("info-log-queue", false, false, false, null);
        channel.queueDeclare("warn-log-queue", false, false, false, null);
        channel.queueDeclare("error-log-queue", false, false, false, null);
        channel.queueDeclare("un_type-log-queue", false, false, false, null);

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("请输入日志-等级");
        String msg;
        while (!(msg = reader.readLine()).equalsIgnoreCase("exit")) {
            if (!msg.isEmpty()) {
                if (msg.contains("-")) {
                    String[] splits = msg.split("-");
                    String log = splits[0];
                    String level = splits[1];
                    AMQP.BasicProperties property = new AMQP.BasicProperties.Builder().headers(Collections.singletonMap("level", level)).build();
                    switch (level) {
                        case "info":
                            channel.basicPublish("routing-logs", "INFO", property, log.getBytes(StandardCharsets.UTF_8));
                            break;
                        case "warn":
                            channel.basicPublish("routing-logs", "WARN", property, log.getBytes(StandardCharsets.UTF_8));
                            break;
                        case "error":
                            channel.basicPublish("routing-logs", "ERROR", property, log.getBytes(StandardCharsets.UTF_8));
                            break;
                        default:
                            property = new AMQP.BasicProperties().builder().headers(Collections.singletonMap("level", "un_type")).build();
                            channel.basicPublish("routing-logs", "UN_TYPE", property, msg.getBytes(StandardCharsets.UTF_8));
                            break;
                    }
                } else {
                    AMQP.BasicProperties property = new AMQP.BasicProperties().builder().headers(Collections.singletonMap("level", "un_type")).build();
                    channel.basicPublish("routing-logs", "UN_TYPE", property, msg.getBytes(StandardCharsets.UTF_8));
                }
            } else log.warn("请输入日志...");
        }
        channel.close();
        connection.close();
        log.warn("多级日志发布者退出,再见");
    }

    public static void routingQueueConsumer() throws IOException {
        Connection connection = getConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare("info-log-queue", false, false, false, null);
        channel.queueDeclare("warn-log-queue", false, false, false, null);
        channel.queueDeclare("error-log-queue", false, false, false, null);
        channel.queueDeclare("un_type-log-queue", false, false, false, null);
        channel.exchangeDeclare("routing-logs", "direct");
        channel.queueBind("info-log-queue", "routing-logs", "INFO");
        channel.queueBind("warn-log-queue", "routing-logs", "WARN");
        channel.queueBind("error-log-queue", "routing-logs", "ERROR");
        channel.queueBind("un_type-log-queue", "routing-logs", "UN_TYPE");
        DeliverCallback callback = (consumerTag, delivery) -> {
            String msg = new String(delivery.getBody());
            String level = String.valueOf(delivery.getProperties().getHeaders().get("level"));
            log.warn("消费者1 接收到日志: 等级: {},内容: {},执行存储", level, msg);
            try {
                doWork(msg);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                log.warn("消费者1 日志存储完毕,存储内容等级: {},内容: {}", level, msg);
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        };
        channel.basicConsume("info-log-queue", callback, consumerTag -> {
        });
        channel.basicConsume("warn-log-queue", callback, consumerTag -> {
        });
        channel.basicConsume("error-log-queue", callback, consumerTag -> {
        });
        channel.basicConsume("un_type-log-queue", callback, consumerTag -> {
        });

        DeliverCallback callback2 = (consumerTag, delivery) -> {
            String msg = new String(delivery.getBody());
            String level = String.valueOf(delivery.getProperties().getHeaders().get("level"));
            log.warn("消费者2 接收到日志: 等级: {},内容: {},执行存储", level, msg);
            try {
                doWork(msg);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                log.warn("消费者2 日志存储完毕,存储内容等级: {},内容: {}", level, msg);
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        };
        channel.basicConsume("info-log-queue", callback2, consumerTag -> {
        });
        channel.basicConsume("warn-log-queue", callback2, consumerTag -> {
        });
        channel.basicConsume("error-log-queue", callback2, consumerTag -> {
        });
        channel.basicConsume("un_type-log-queue", callback2, consumerTag -> {
        });
    }

    private static void doWork(String task) throws InterruptedException {
        for (char c : task.toCharArray()) {
            if (c == 'o') Thread.sleep(1000);
        }
    }

    
}
