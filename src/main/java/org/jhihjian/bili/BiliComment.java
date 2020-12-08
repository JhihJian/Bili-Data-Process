package org.jhihjian.bili;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jhihjian.bili.process.VideoMsgProcess;
import org.jhihjian.bili.util.Conf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.util.List;
import java.util.Map;

import static java.lang.Thread.sleep;

public class BiliComment {
  private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
  // test url "https://httpbin.org/post";
  private static final String COMMENT_URL = "https://api.bilibili.com/x/v2/reply/add";
  private static final String COOKIES;

  static {
    Conf conf = new Conf();
    COOKIES = conf.getProperty("cookies");
  }

  private void commentAv(String av, String message) throws Exception {
    URI uri = URI.create(COMMENT_URL);
    Map<String, String> params = Maps.newHashMap();
    params.put("oid", av);
    params.put("type", "1");
    params.put("plat", "1");
    params.put("message", message);
    params.put("ordering", "heat");
    params.put("jsonp", "jsonp");
    params.put("csrf", getCsrfFromCookies(COOKIES));
    String[] headers = {
      "content-type", "application/x-www-form-urlencoded; " + "charset=UTF-8",
      "referer", "https://www.bilibili.com",
      "user-agent",
          "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.66 Safari/537.36",
      "origin", "https://www.bilibili.com",
      "cookie", COOKIES
    };
    HttpClient client =
        HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .proxy(ProxySelector.getDefault())
            .version(HttpClient.Version.HTTP_2)
            //                .cookieHandler(new
            // CookieManager(initCookies(uri,cookies).getCookieStore(), CookiePolicy.ACCEPT_ALL))
            .build();
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(uri)
            .headers(headers)
            .version(HttpClient.Version.HTTP_2)
            .POST(HttpRequest.BodyPublishers.ofString(getParamsString(params)))
            .build();

    HttpResponse<?> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    logger.info("comment av:{} message:{} result:{}", av, message, response.body());
  }

  public static String getCsrfFromCookies(String cookies) {
    cookies = cookies.replaceAll(" ", "");
    for (String line : cookies.split(";")) {
      String[] keyValue = line.split("=");
      if ("bili_jct".equals(keyValue[0])) {
        return keyValue[1];
      }
    }
    return "";
  }

  private static final int MESSAGE_UPPER_LIMIT = 1000;
  private static final int COMMENT_SLEEP_TIME = 40 * 1000;

  // 结果长度会少 result.size() ，因为分割的逗号没了
  public static List<String> splitMessage(String message) {
    String str = message;
    List<String> result = Lists.newArrayList();
    while (str.length() > MESSAGE_UPPER_LIMIT) {
      int index = str.substring(0, MESSAGE_UPPER_LIMIT).lastIndexOf(",");
      if (index < 0) {
        index = str.substring(0, MESSAGE_UPPER_LIMIT).lastIndexOf("\n");
        if (index < 0) {
          index = MESSAGE_UPPER_LIMIT;
        }
      }
      result.add(str.substring(0, index));
      str = str.substring(index + 1);
    }
    result.add(str);
    return result;
  }

  public boolean commentTimeText(String av) throws Exception {
    Map<Long, String> map = subtitleStore.queryTimeTextByAv(Long.valueOf(av));
    if (map.isEmpty()) {
      return false;
    }
    StringBuilder sb = new StringBuilder();
    sb.append("此视频字幕如下：\n");
    for (Map.Entry<Long, String> entry : map.entrySet()) {
      sb.append(VideoMsgProcess.castTimestamp(entry.getKey()));
      sb.append("\t");
      sb.append(entry.getValue());
      sb.append("\n");
    }
    String message = sb.toString();
    for (String str : splitMessage(message)) {
      BiliComment biliComment = new BiliComment();
      biliComment.commentAv(av, str);
      System.out.println();
      logger.info("sleep {} ms", COMMENT_SLEEP_TIME);
      sleep(COMMENT_SLEEP_TIME);
    }
    return true;
  }

  private SubtitleStore subtitleStore = new SubtitleStore();

  public boolean commentText(String av) throws Exception {
    String text = subtitleStore.queryTextByAv(Long.valueOf(av));
    if (Strings.isNullOrEmpty(text)) {
      return false;
    }
    String message = "此视频字幕如下：\n" + text;
    for (String str : splitMessage(message)) {
      BiliComment biliComment = new BiliComment();
      biliComment.commentAv(av, str);
      logger.info("sleep {} ms", COMMENT_SLEEP_TIME);
      sleep(COMMENT_SLEEP_TIME);
    }
    return true;
  }

  public static void main(String[] args) {
    Logger logger = LoggerFactory.getLogger(BiliComment.class.getName());
    String av = "797860601";
    BiliComment biliComment = new BiliComment();
    //        biliComment.commentText(av);
    try {
      biliComment.commentTimeText(av);
    } catch (Exception e) {
      logger.error("", e);
    }
  }

  public static String getParamsString(Map<String, String> params)
      throws UnsupportedEncodingException {
    StringBuilder result = new StringBuilder();

    for (Map.Entry<String, String> entry : params.entrySet()) {
      result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
      result.append("=");
      result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
      result.append("&");
    }

    String resultString = result.toString();
    return resultString.length() > 0
        ? resultString.substring(0, resultString.length() - 1)
        : resultString;
  }
}
