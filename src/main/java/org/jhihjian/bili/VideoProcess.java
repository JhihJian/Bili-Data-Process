package org.jhihjian.bili;

import org.jhihjian.bili.util.Conf;
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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class VideoProcess {
  public static final String IMAGE_TYPE = ".jpg";
  Conf conf = new Conf();
  private final int INTERVAL_FRAME_NUM = Integer.parseInt(conf.getProperty("interval_frame_num"));
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  static {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
  }

  public List<BufferedImage> ProcessVideo(String path) {
    List<BufferedImage> result = new ArrayList<>();
    VideoCapture videoCapture = new VideoCapture();
    videoCapture.open(path);
    Mat image = new Mat();
    int frame_num = INTERVAL_FRAME_NUM;
    int index = 0;
    int out_num = 0;
    while (videoCapture.grab()) {
      videoCapture.read(image);
      while (index++ == frame_num) {
        index = 0;
        Mat cutImage = imageCut(image, 0, image.height() - 200, image.width(), 200);
        out_num++;
        result.add(Mat2BufImg(cutImage));
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
}
