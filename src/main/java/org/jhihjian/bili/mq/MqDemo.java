package org.jhihjian.bili.mq;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import org.jhihjian.bili.util.Conf;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class MqDemo {
  private final String VIDEO_QUEUE_NAME = new Conf().getProperty("video_queue_name");
  private final String SUBTITLE_QUEUE_NAME = new Conf().getProperty("subtitle_queue_name");
  private final ConnectionFactory factory;

  public MqDemo() {
    String HOST = new Conf().getProperty("mq_host");
    String USER_NAME = new Conf().getProperty("mq_user_name");
    String PASSWORD = new Conf().getProperty("mq_password");

    factory = new ConnectionFactory();
    factory.setHost(HOST);
    factory.setUsername(USER_NAME);
    factory.setPassword(PASSWORD);
  }

  public void send() throws IOException, TimeoutException {
    try (Connection connection = factory.newConnection();
        Channel channel = connection.createChannel()) {
      channel.queueDeclare(VIDEO_QUEUE_NAME, false, false, false, null);
      String message = "Hello World!";
      channel.basicPublish("", VIDEO_QUEUE_NAME, null, message.getBytes());
      System.out.println(" [x] Sent '" + message + "'");
    }
  }

  public void receive() throws IOException, TimeoutException {
    Connection connection = factory.newConnection();
    Channel channel = connection.createChannel();

    channel.queueDeclare(VIDEO_QUEUE_NAME, false, false, false, null);
    System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
    DeliverCallback deliverCallback =
        (consumerTag, delivery) -> {
          String message = new String(delivery.getBody(), "UTF-8");
          System.out.println(" [x] Received '" + message + "'");
        };

    channel.basicConsume(VIDEO_QUEUE_NAME, true, deliverCallback, consumerTag -> {});
    while (true) {}
    // TODO 这里的connection应该没有关闭
  }

  public static void main(String[] args) throws IOException, TimeoutException {
    MqDemo mqDemo = new MqDemo();
    mqDemo.receive();
  }
}
