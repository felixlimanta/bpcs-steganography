import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;

public class Utility {

  public static BufferedImage loadImage(String path) throws IOException {
    return ImageIO.read(new File(path));
  }

  public static void saveImage(BufferedImage image, String path) throws IOException {
    String extension = getFileExtension(path);
    ImageIO.write(image, extension, new File(path));
  }

  public static byte[] loadFile(String path) throws IOException {
    return Files.readAllBytes(Paths.get(path));
  }

  public static void saveFile(String path, byte[] data) throws IOException {
    try (FileOutputStream fos = new FileOutputStream(path)) {
      fos.write(data);
      fos.close();
    }
  }

  public static String getFileExtension(String path) {
    try {
      return path.substring(path.lastIndexOf(".") + 1);
    } catch (Exception e) {
      return "";
    }
  }

  public static BufferedImage deepCopy(BufferedImage bi) {
    ColorModel cm = bi.getColorModel();
    boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
    WritableRaster raster = bi.copyData(bi.getRaster().createCompatibleWritableRaster());
    return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
  }

  public static boolean isEqual(BufferedImage img1, BufferedImage img2) {
    if (img1.getWidth() == img2.getWidth() && img1.getHeight() == img2.getHeight()) {
      for (int x = 0; x < img1.getWidth(); x++) {
        for (int y = 0; y < img1.getHeight(); y++) {
          if (img1.getRGB(x, y) != img2.getRGB(x, y)) {
            return false;
          }
        }
      }
    } else {
      return false;
    }
    return true;
  }

  public static String toBinaryString(byte b) {
    return String.format("%8s", Integer.toBinaryString(Byte.toUnsignedInt(b)))
        .replace(' ', '0');
  }

  public static byte[] addPadding(byte[] b) {
    if (b.length % 8 == 0) {
      return b;
    }

    byte[] padded = new byte[b.length + (8 - (b.length % 8))];
    Arrays.fill(padded, (byte) 0);
    System.arraycopy(b, 0, padded, 0, b.length);
    return padded;
  }

  public static byte[] removePadding(byte[] b) {
    int i = b.length - 1;
    while (b[i] == 0) {
      i--;
    }

    int len = i + 1;
    byte[] depadded = new byte[len];
    System.arraycopy(b, 0, depadded, 0, len);
    return depadded;
  }

  public static boolean getBit(byte value, int position) {
    return ((value & (1 << position)) != 0);
  }

  public static byte setBit(byte b, boolean value, int position) {
    int temp = Byte.toUnsignedInt(b);
    if (value) {
      return (byte) (temp | (1 << position));
    } else {
      return (byte) (temp & ~(1 << position));
    }
  }

  public static byte[] toByteArray(ArrayList<Byte> list) {
    Byte[] array = list.toArray(new Byte[list.size()]);
    byte[] byteArray = new byte[array.length];
    IntStream.range(0, array.length).forEach(i -> byteArray[i] = array[i]);
    return byteArray;
  }

  public static double[] calculateLinearRegression(int[] x, int[] y) {
    if (x.length != y.length)
      throw new IllegalArgumentException("Different array sizes");

    int n = x.length;

    // first pass: read in data, compute xbar and ybar
    double sumx = 0.0, sumy = 0.0, sumx2 = 0.0;
    for (int i = 0; i < n; ++i) {
      sumx  += x[i];
      sumx2 += x[i] * x[i];
      sumy  += y[i];
    }

    double xbar = sumx / n;
    double ybar = sumy / n;

    // second pass: compute summary statistics
    double xxbar = 0.0, yybar = 0.0, xybar = 0.0;
    for (int i = 0; i < n; i++) {
      xxbar += (x[i] - xbar) * (x[i] - xbar);
      yybar += (y[i] - ybar) * (y[i] - ybar);
      xybar += (x[i] - xbar) * (y[i] - ybar);
    }
    double beta1 = xybar / xxbar;
    double beta0 = ybar - beta1 * xbar;

    // print results
    return new double[]{beta1, beta0};
  }
}
