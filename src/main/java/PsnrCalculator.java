import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class PsnrCalculator {

  public static double calculatePSNR(BufferedImage i1, BufferedImage i2) {
    double rms = calculateRMS(i1, i2);
    return 20 * Math.log10(256 / rms);
  }

  private static double calculateRMS(BufferedImage i1, BufferedImage i2) {
    long s = 0;

    byte[] data1 = ((DataBufferByte) i1.getRaster().getDataBuffer()).getData();
    byte[] data2 = ((DataBufferByte) i2.getRaster().getDataBuffer()).getData();

    if (data1.length != data2.length) {
      throw new IllegalArgumentException("Different image size");
    }

    for (int i = 0; i < data1.length; ++i) {
      int d = (data1[i] & 0xff) - (data2[i] & 0xff);
      s += (d * d);
    }

    return Math.sqrt(((double) s) / data1.length);
  }
}
