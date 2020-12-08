package org.jhihjian.bili.process;

import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.jhihjian.bili.SubtitleStore;
import org.jhihjian.bili.mq.AvMessage;
import org.jhihjian.bili.ocr.OcrProcess;
import org.jhihjian.bili.ocr.TextResult;
import org.jhihjian.bili.util.Conf;
import org.jhihjian.bili.util.HashSimilarity;
import org.jhihjian.bili.util.ISimilarity;
import org.jhihjian.bili.util.ImageUtils;
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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class VideoMsgProcess implements MsgProcess {
  public static final String IMAGE_TYPE = ".jpg";
  Conf conf = new Conf();
  private final int INTERVAL_FRAME_NUM = Integer.parseInt(conf.getProperty("interval_frame_num"));
  private final Logger logger = LoggerFactory.getLogger(this.getClass());
  private final double IMAGE_SIMILAR = Double.parseDouble(conf.getProperty("image_similar"));
  private final double TEXT_SIMILAR = Double.parseDouble(conf.getProperty("text_similar"));

  static {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
  }

  public Map<Long, BufferedImage> processVideo(String path) {
    Map<Long, BufferedImage> result = new TreeMap<>();
    VideoCapture videoCapture = new VideoCapture();
    videoCapture.open(path);
    Mat image = new Mat();
    Stopwatch stopwatch = Stopwatch.createStarted();
    // 帧率
    int frame_num = ((int) videoCapture.get(5)) / 2;
    int index = 0;
    int out_num = 0;
    int read_num = 0;
    Mat preImage = null;
    while (videoCapture.grab()) {
      videoCapture.read(image);
      while (index++ == frame_num) {
        read_num++;
        index = 0;
        Mat cutImage = imageCut(image, 0, image.height() - 200, image.width(), 200);
        if (correlation(preImage, cutImage) > IMAGE_SIMILAR) {
          preImage = cutImage;
          continue;
        }
        preImage = cutImage;
        out_num++;
        // Current position of the video file in milliseconds.
        result.put((long) videoCapture.get(0), Mat2BufImg(cutImage));
      }
    }
    logger.info(
        "video:{} read frame num:{} output frame num:{} cost time:{}",
        path,
        read_num,
        out_num,
        stopwatch.elapsed(TimeUnit.MILLISECONDS));
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

  public static double correlation(Mat image_1, Mat image_2) {
    if (image_1 == null || image_2 == null) {
      return 0;
    }
    // 灰度化
    Mat gray1 = new Mat();
    Mat gray2 = new Mat();
    Imgproc.cvtColor(image_1, gray1, Imgproc.COLOR_BGR2GRAY);
    Imgproc.cvtColor(image_2, gray2, Imgproc.COLOR_BGR2GRAY);
    Mat result = new Mat();
    Imgproc.matchTemplate(gray1, gray2, result, Imgproc.TM_CCORR_NORMED);
    double value = result.get(0, 0)[0];
    return value < 0.9 ? 0 : (value - 0.9) * 10;
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

  public String getTotalText(TreeMap<Long, String> timeTextMap) {
    List<String> result = Lists.newArrayList(timeTextMap.values());
    Iterator<String> iterator = result.iterator();
    String preText = "";
    ISimilarity similarity = new HashSimilarity();
    while (iterator.hasNext()) {
      String text = iterator.next();
      if (similarity.getSimilarity(preText, text) > TEXT_SIMILAR) {
        iterator.remove();
      }
      preText = text;
    }
    String totalText = Joiner.on(",").join(result);
    return totalText;
  }

  private static final DateFormat dateFormat = new SimpleDateFormat("mm:ss");

  // timestamp to 1:23 形式
  public static String castTimestamp(Long timestamp) {
    return dateFormat.format(new Date(timestamp));
  }

  @Override
  public void process(String message) throws Exception {
    logger.info("VideoProcess process message:{}.", message);
    Stopwatch stopwatch = Stopwatch.createStarted();
    AvMessage avMessage = AvMessage.parseMessage(message);
    // 处理
    Map<Long, BufferedImage> bufferedImages = this.processVideo(avMessage.getPath());
    OcrProcess ocrProcess = new OcrProcess();
    // 注意这里map如果是hashmap则values 是无序的 这里的返回值，即使为空也得占位才能保证时间信息不错
    List<BufferedImage> images = new ArrayList<>(bufferedImages.values());
    List<Long> timestampList = new ArrayList<>(bufferedImages.keySet());
    //    for (int i = 0; i < images.size(); i++) {
    //      ImageUtils.writeBufferedImage(images.get(i), "/opt/temp/" + timestampList.get(i) +
    // ".png");
    //    }

    TextResult[][] textResults = ocrProcess.imageProcess(images);
    if (textResults.length == 0) {
      logger.info("process video {} no result", avMessage.getAv());
    }
    // TODO 这里能有序吗?
    TreeMap<Long, String> timeTextMap = new TreeMap<>();
    for (int i = 0; i < textResults.length; i++) {
      if (textResults[i].length == 0) {
        continue;
      }
      String text = textResults[i][0].getText();
      if (Strings.isNullOrEmpty(text)) {
        continue;
      }
      timeTextMap.put(timestampList.get(i), text);
    }
    String totalText = getTotalText(timeTextMap);
    // 保存结果
    SubtitleStore subtitleStore = new SubtitleStore();
    subtitleStore.storeText(avMessage.getAv(), timeTextMap);
    subtitleStore.storeText(avMessage.getAv(), totalText);
    // 处理文件
    File file = new File(avMessage.getPath());
    Files.delete(Paths.get(file.getAbsolutePath()));
    logger.info(
        "process video cost:{} subtitle length:{}",
        stopwatch.elapsed().toMillis(),
        totalText.length());
  }

  public static void main(String[] args) throws Exception {
    String videoJson =
        "{\"av\":288007988,\"path\":\"/opt/video_download/288007988/Videos/288007988.mp4\"}";
    VideoMsgProcess process = new VideoMsgProcess();
    process.process(videoJson);
  }
}
