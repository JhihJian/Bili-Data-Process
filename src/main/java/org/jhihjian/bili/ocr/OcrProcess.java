package org.jhihjian.bili.ocr;

import com.google.common.base.Stopwatch;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jhihjian.bili.util.Conf;
import org.jhihjian.bili.util.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class OcrProcess {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  Conf conf = new Conf();
  private final String url = conf.getProperty("ocr_url");
  private final URI uri = URI.create(url);
  private final HttpClient client = HttpClient.newBuilder().build();

  private String postToServer(List<BufferedImage> images) {
    String[] headers = new String[] {"Content-type", "application/json;charset=UTF-8"};
    try {
      List<String> imageStrings = new ArrayList<>();
      for (BufferedImage image : images) {
        imageStrings.add(imageToBase64(image));
      }
      String requestBody = buildPostData(imageStrings);
      HttpRequest request =
          HttpRequest.newBuilder()
              .headers(headers)
              .uri(uri)
              .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
              .build();
      HttpResponse<?> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      return response.body().toString();

    } catch (IOException e) {
      logger.error("", e);
      e.printStackTrace();
    } catch (InterruptedException e) {
      logger.error("", e);
    }
    return "";
  }

  private String imageToBase64(BufferedImage image) throws IOException {
    return Base64.getEncoder().encodeToString(ImageUtils.toByteArray(image, "png"));
  }

  private String buildPostData(List<String> imageBase64) {
    JsonObject jsonObject = new JsonObject();
    JsonArray jsonArray = new JsonArray();
    for (String str : imageBase64) {
      jsonArray.add(str);
    }
    jsonObject.add("images", jsonArray);
    return jsonObject.toString();
  }

  public TextResult[][] imageProcess(List<BufferedImage> images) {
    logger.info("ocr process images....");
    Stopwatch stopwatch = Stopwatch.createStarted();
    if (images.size() == 0) {
      return new TextResult[][] {};
    }
    String responseBody = postToServer(images);
    Gson gson = new Gson();
    OcrResponse ocrResponse = gson.fromJson(responseBody, OcrResponse.class);
    TextResult[][] result = ocrResponse.getResults();
    logger.info("orc process cost time:{}", stopwatch.elapsed(TimeUnit.MILLISECONDS));
    return result;
  }
}
