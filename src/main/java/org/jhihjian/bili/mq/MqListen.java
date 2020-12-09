package org.jhihjian.bili.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.jhihjian.bili.process.ChatMsgProcess;
import org.jhihjian.bili.process.MqContext;
import org.jhihjian.bili.process.MsgProcess;
import org.jhihjian.bili.process.VideoMsgProcess;
import org.jhihjian.bili.util.Conf;
import org.jhihjian.bili.util.MySQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Thread.sleep;

public class MqListen {
  private final String VIDEO_QUEUE_NAME = new Conf().getProperty("video_queue_name");
  private final String CHAT_QUEUE_NAME = new Conf().getProperty("chat_queue_name");
  private final ConnectionFactory factory;
  private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
  private static final ReentrantLock LOCK = new ReentrantLock();
  private static final Condition STOP = LOCK.newCondition();
  private final MqContext mqContext = new MqContext();

  public MqListen() {
    String HOST = new Conf().getProperty("mq_host");
    String USER_NAME = new Conf().getProperty("mq_user_name");
    String PASSWORD = new Conf().getProperty("mq_password");

    factory = new ConnectionFactory();
    factory.setHost(HOST);
    factory.setUsername(USER_NAME);
    factory.setPassword(PASSWORD);
  }

  public MySQL initMysql() {
    Conf conf = new Conf();
    String url = conf.getProperty("url");
    String user = conf.getProperty("username");
    String pw = conf.getProperty("password");
    return new MySQL(url, user, pw);
  }

  public void process() throws IOException, TimeoutException, InterruptedException {
    mqContext.setMySQL(initMysql());
    MsgProcess videoProcess = new VideoMsgProcess(mqContext);
    MsgProcess chatProcess = new ChatMsgProcess(mqContext);

    Connection connection = factory.newConnection();
    Channel videoChannel = connection.createChannel();
    Channel chatChannel = connection.createChannel();

    videoChannel.queueDeclare(VIDEO_QUEUE_NAME, false, false, false, null);
    chatChannel.queueDeclare(CHAT_QUEUE_NAME, false, false, false, null);
    DeliverCallback videoCallback =
        (consumerTag, delivery) -> {
          // TODO video process
          String message = new String(delivery.getBody(), "UTF-8");
          try {
            videoProcess.process(message);
          } catch (Exception e) {
            logger.error("video channel process message:{} failed.", message, e);
          }
        };
    DeliverCallback chatCallback =
        (consumerTag, delivery) -> {
          // TODO video process
          String message = new String(delivery.getBody(), "UTF-8");
          try {
            chatProcess.process(message);
          } catch (Exception e) {
            logger.error("chat channel process message:{} failed.", message, e);
          }
        };
    videoChannel.basicConsume(VIDEO_QUEUE_NAME, true, videoCallback, consumerTag -> {});
    chatChannel.basicConsume(CHAT_QUEUE_NAME, true, chatCallback, consumerTag -> {});
    logger.info("wait mq message...");
    // 主线程阻塞等待，守护线程释放锁后退出
    addHook();
    try {
      LOCK.lock();
      STOP.await();
    } catch (InterruptedException e) {
      logger.warn(" service stopped, interrupted by other thread!", e);
    } finally {
      LOCK.unlock();
    }
  }

  private void addHook() {
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  mqContext.destroy();
                  logger.info("jvm exit, all service stopped.");
                  try {
                    LOCK.lock();
                    STOP.signal();
                  } finally {
                    LOCK.unlock();
                  }
                },
                "MqListen-shutdown-hook"));
  }

  public static void main(String[] args)
      throws InterruptedException, TimeoutException, IOException {
    MqListen mqListen = new MqListen();
    mqListen.process();
  }
}
