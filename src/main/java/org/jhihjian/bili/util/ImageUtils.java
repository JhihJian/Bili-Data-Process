package org.jhihjian.bili.util;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class ImageUtils {
  // convert BufferedImage to byte[]
  public static byte[] toByteArray(BufferedImage bi, String format) throws IOException {

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ImageIO.write(bi, format, baos);
    byte[] bytes = baos.toByteArray();
    return bytes;
  }

  // convert byte[] to BufferedImage
  public static BufferedImage toBufferedImage(byte[] bytes) throws IOException {

    InputStream is = new ByteArrayInputStream(bytes);
    BufferedImage bi = ImageIO.read(is);
    return bi;
  }

  public static void writeBufferedImage(BufferedImage bufferedImage, String filePath)
      throws IOException {
    ImageIO.write(bufferedImage, "png", new File(filePath));
  }
}
