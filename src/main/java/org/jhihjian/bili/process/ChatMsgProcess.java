package org.jhihjian.bili.process;

import org.jhihjian.bili.chat.Chat;
import org.jhihjian.bili.chat.ChatStore;
import org.jhihjian.bili.chat.XmlLoader;
import org.jhihjian.bili.mq.AvMessage;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

public class ChatMsgProcess implements MsgProcess {
  private MqContext mqContext;

  public ChatMsgProcess(MqContext mqContext) {
    this.mqContext = mqContext;
  }

  @Override
  public void process(String message) throws Exception {
    AvMessage avMessage = AvMessage.parseMessage(message);
    Chat chat = XmlLoader.loadChatFromXml(new File(avMessage.getPath()));
    ChatStore chatStore = new ChatStore(mqContext.getMySQL());
    chatStore.store(avMessage.getAv(), chat);
  }

  public static void main(String[] args) throws InterruptedException {
    String path = "C:\\opt\\video_download\\245447549\\Videos\\245447549.xml";
    Chat chat = XmlLoader.loadChatFromXml(new File(path));
    Map<Integer, Integer> treeMap = new TreeMap<>();
    int internel = 2;
    for (int i = 0; i < chat.getCount(); i++) {
      int seg = (int) (chat.getTime().get(i) / internel);
      treeMap.put(seg, treeMap.getOrDefault(seg, 0) + 1);
    }
    System.out.println(treeMap);
  }
}
