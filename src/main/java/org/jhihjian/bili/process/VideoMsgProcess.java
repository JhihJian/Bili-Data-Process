package org.jhihjian.bili.process;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.jhihjian.bili.SubtitleReader;
import org.jhihjian.bili.SubtitleStore;
import org.jhihjian.bili.mq.AvMessage;
import org.jhihjian.bili.ocr.OcrProcess;
import org.jhihjian.bili.ocr.TextResult;
import org.jhihjian.bili.util.Conf;
import org.jhihjian.bili.util.HashSimilarity;
import org.jhihjian.bili.util.ISimilarity;
import org.opencv.core.*;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class VideoMsgProcess implements MsgProcess {
  public static final String IMAGE_TYPE = ".jpg";
  Conf conf = new Conf();
  private final int INTERVAL_FRAME_NUM = Integer.parseInt(conf.getProperty("interval_frame_num"));
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  static {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
  }

  public Map<Long, BufferedImage> processVideo(String path) {
    Map<Long, BufferedImage> result = new HashMap<>();
    VideoCapture videoCapture = new VideoCapture();
    videoCapture.open(path);
    Mat image = new Mat();

    System.out.println("帧率:" + videoCapture.get(5));
    int frame_num = INTERVAL_FRAME_NUM;
    int index = 0;
    int out_num = 0;
    while (videoCapture.grab()) {
      videoCapture.read(image);
      while (index++ == frame_num) {
        index = 0;
        Mat cutImage = imageCut(image, 0, image.height() - 200, image.width(), 200);
        out_num++;
        System.out.println("milliseconds:" + videoCapture.get(0));

        // Current position of the video file in milliseconds.
        result.put((long) videoCapture.get(0), Mat2BufImg(cutImage));
      }
    }
    logger.info("video:{} output frame num:{}", path, out_num);
    return result;
  }

  public static Mat imageCut(Mat image, int posX, int posY, int width, int height) {
    // 原始图像
    // 截取的区域：参数,坐标X,坐标Y,截图宽度,截图长度
    Rect rect = new Rect(posX, posY, width, height);
    // 两句效果一样
    Mat sub = image.submat(rect); // Mat sub = new Mat(image,rect);
    Mat mat = new Mat();
    Size size = new Size(width, height);
    Imgproc.resize(sub, mat, size); // 将人脸进行截图并保存
    return mat;
  }

  /**
   * Mat转换成BufferedImage
   *
   * @param matrix 要转换的Mat //@param fileExtension 格式为 ".jpg", ".png", etc
   * @return
   */
  private BufferedImage Mat2BufImg(Mat matrix) {
    String fileExtension = IMAGE_TYPE;
    // convert the matrix into a matrix of bytes appropriate for
    // this file extension
    MatOfByte mob = new MatOfByte();
    Imgcodecs.imencode(fileExtension, matrix, mob);
    // convert the "matrix of bytes" into a byte array
    byte[] byteArray = mob.toArray();
    BufferedImage bufImage = null;
    try {
      InputStream in = new ByteArrayInputStream(byteArray);
      bufImage = ImageIO.read(in);
    } catch (Exception e) {
      logger.error("", e);
    }
    return bufImage;
  }

  private final double SIMILAR_VALUE = 0.81;

  public String getTotalText(TreeMap<Long, String> timeTextMap) {
    List<String> result = Lists.newArrayList(timeTextMap.values());
    Iterator<String> iterator = result.iterator();
    String preText = "";
    ISimilarity similarity = new HashSimilarity();
    while (iterator.hasNext()) {
      String text = iterator.next();
      if (similarity.getSimilarity(preText, text) > SIMILAR_VALUE) {
        iterator.remove();
      }
      preText = text;
    }
    String totalText = Joiner.on(",").join(result);
    totalText = totalText.replaceAll(",,", "~~~");
    totalText = totalText.replaceAll(",", "");
    totalText = totalText.replaceAll("~~~", ",");
    return totalText;
  }

  @Override
  public void process(String message) throws Exception {
    logger.info("VideoProcess process message:{}.", message);
    AvMessage avMessage = AvMessage.parseMessage(message);
    // 处理
    Map<Long, BufferedImage> bufferedImages = this.processVideo(avMessage.getPath());
    OcrProcess ocrProcess = new OcrProcess();
    // TODO 这里的返回值，即使为空也得占位才能保证时间信息不错
    TextResult[][] textResults = ocrProcess.imageProcess(new ArrayList<>(bufferedImages.values()));
    // TODO 这里能有序吗?
    List<Long> timestampList = new ArrayList<>(bufferedImages.keySet());
    TreeMap<Long, String> timeTextMap = new TreeMap<>();
    for (int i = 0; i < textResults.length; i++) {
      String text = textResults[i][0].getText();
      if (!Strings.isNullOrEmpty(text)) {
        timeTextMap.put(timestampList.get(i), text);
      }
    }
    String totalText = getTotalText(timeTextMap);
    // 保存结果
    SubtitleStore subtitleStore = new SubtitleStore();
    subtitleStore.storeText(avMessage.getAv(), timeTextMap);
    subtitleStore.storeText(avMessage.getAv(), totalText);
    // 处理文件
    File file = new File(avMessage.getPath());
    Files.delete(Paths.get(file.getAbsolutePath()));
  }
}
