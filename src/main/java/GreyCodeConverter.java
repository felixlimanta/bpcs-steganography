import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import javax.imageio.ImageIO;

public class GreyCodeConverter {

  public static byte[] toGreyCode(byte[] binary) {
    byte[] res = new byte[binary.length];
    for (int i = 0; i < binary.length; ++i) {
      int temp = Byte.toUnsignedInt(binary[i]);
      res[i] = (byte) (temp ^ (temp >> 1));
    }
    return res;
  }

  public static byte[] toBinary(byte[] grey) {
    byte[] res = new byte[grey.length];
    for (int i = 0; i < grey.length; ++i) {
      int temp = Byte.toUnsignedInt(grey[i]);
      for (int mask = temp >> 1; mask != 0; mask >>= 1) {
        temp ^= mask;
      }
      res[i] = (byte) temp;
    }
    return res;
  }

  public static void main(String[] args) throws IOException {
    String path0 = "C:\\Users\\ASUS\\Downloads\\64166790_p0.jpg";
    String path1 = "C:\\Users\\ASUS\\Downloads\\64166790_p1.jpg";
    String path2 = "C:\\Users\\ASUS\\Downloads\\64166790_p2.jpg";

    BufferedImage image = ImageIO.read(new File(path0));
    WritableRaster raster = image.getRaster();
    byte[] data = ((DataBufferByte) raster.getDataBuffer()).getData();

    byte[] orig = new byte[data.length];
    System.arraycopy(data, 0, orig, 0, data.length);

    byte[] res1 = toGreyCode(data);
    System.arraycopy(res1, 0, data, 0, res1.length);
    ImageIO.write(image, "jpg", new File(path1));

    byte[] res2 = toBinary(res1);
    System.arraycopy(res2, 0, data, 0, res2.length);
    ImageIO.write(image, "jpg", new File(path2));

    System.out.println(Arrays.equals(orig, res2));

    Random r = new Random();

    for (int i = 0; i < 5; ++i) {
      byte[] a = new byte[1];
      a[0] = (byte) r.nextInt();
      byte[] b = toGreyCode(a);
      byte[] c = toBinary(b);
      System.out.printf("%s %s %s\n", Byte.toString(a[0]), Byte.toString(b[0]), Byte.toString(c[0]));
      System.out.printf("%s\n%s\n%s\n", Integer.toBinaryString(Byte.toUnsignedInt(a[0])), Integer.toBinaryString(Byte.toUnsignedInt(b[0])), Integer.toBinaryString(Byte.toUnsignedInt(c[0])));
    }

  }



}
