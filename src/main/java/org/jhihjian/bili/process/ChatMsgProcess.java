package org.jhihjian.bili.process;

import org.jhihjian.bili.chat.Chat;
import org.jhihjian.bili.chat.ChatStore;
import org.jhihjian.bili.chat.XmlLoader;
import org.jhihjian.bili.mq.AvMessage;

import java.io.File;

public class ChatMsgProcess implements MsgProcess {

  @Override
  public void process(String message) throws Exception {
    AvMessage avMessage = AvMessage.parseMessage(message);
    Chat chat = XmlLoader.loadChatFromXml(new File(avMessage.getPath()));
    ChatStore chatStore = new ChatStore();
    chatStore.store(avMessage.getAv(), chat);
  }
}
