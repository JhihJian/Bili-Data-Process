package org.jhihjian.bili.process;

import org.jhihjian.bili.chat.Chat;
import org.jhihjian.bili.chat.ChatStore;
import org.jhihjian.bili.chat.XmlLoader;
import org.jhihjian.bili.mq.AvMessage;
import org.jhihjian.bili.util.Conf;
import org.jhihjian.bili.util.MySQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

public class ChatMsgProcess implements MsgProcess {
  Logger logger = LoggerFactory.getLogger(this.getClass().getName());
  private MqContext mqContext;

  public ChatMsgProcess(MqContext mqContext) {
    this.mqContext = mqContext;
  }

  @Override
  public void process(String message) throws Exception {
    AvMessage avMessage = AvMessage.parseMessage(message);
    Chat chat = XmlLoader.loadChatFromXml(new File(avMessage.getPath()));
    ChatStore chatStore = new ChatStore(mqContext.getMySQL());
    logger.info("chat store result:{}", chatStore.store(avMessage.getAv(), chat));
  }

  public static void main(String[] args) throws Exception {

    Conf conf = new Conf();
    String url = conf.getProperty("url");
    String user = conf.getProperty("username");
    String pw = conf.getProperty("password");
    MySQL mysql = new MySQL(url, user, pw);
    MqContext context = new MqContext();
    context.setMySQL(mysql);
    String chatJson =
        "{\"av\":245447549,\"path\":\"/opt/video_download/245447549/Videos/245447549.xml\"}";

    ChatMsgProcess chatMsgProcess = new ChatMsgProcess(context);
    chatMsgProcess.process(chatJson);
  }
}
