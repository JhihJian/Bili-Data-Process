package org.jhihjian.bili.mq;

import com.google.gson.Gson;

public class AvMessage {
  private Long av;
  private String path;

  private AvMessage() {}

  public String getPath() {
    return path;
  }

  public Long getAv() {
    return av;
  }

  public static AvMessage parseMessage(String str) {
    Gson gson = new Gson();
    return gson.fromJson(str, AvMessage.class);
  }
}
