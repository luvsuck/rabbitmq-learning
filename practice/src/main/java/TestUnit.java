import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @Author: zyy
 * @Date: 2024/4/24 00:23
 * @Version:
 * @Description:
 */
public class TestUnit {
    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
//        RabbitConfig.consumerOfWorkQueue(); //工作队列 一个生产者对应多个消费者
//        RabbitConfig.consumer(); //简单队列 一个生产者对应一个消费者
//        RabbitConfig.publish();//简单队列的生产者
//        RabbitConfig.publishWithInput();//监听键盘输入作为消息的发布者

//        RabbitConfig.publisherAndSubscribeReceiver();//发布订阅队列 日志的消费者
//        RabbitConfig.publisherAndSubscriberSenderWithInput();//发布订阅队列 日志的生产者(扇出交换机) 一个生产者对应多个消费者

        RabbitConfig.routingQueueConsumer();
        RabbitConfig.routingQueueSender();

    }
}
