import static org.junit.Assert.assertArrayEquals;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;
import org.junit.Before;
import org.junit.Test;

public class GreyCodeConverterTest {

  private Random r;

  private static final String[] paths = {
      "C:\\Users\\ASUS\\Downloads\\64166790_p0.jpg",
      "C:\\Users\\ASUS\\Downloads\\64166790_p1.jpg",
      "C:\\Users\\ASUS\\Downloads\\64166790_p2.jpg"
  };
  private BufferedImage image;
  private byte[] data;
  private byte[][] result;

  @Before
  public void setUp() throws IOException {
    r = new Random();

    image = ImageIO.read(new File(paths[0]));
    WritableRaster raster = image.getRaster();
    data = ((DataBufferByte) raster.getDataBuffer()).getData();
  }

  @Test
  public void singleByteTest() {
    for (int i = 0; i < 10; ++i) {
      byte[] a = new byte[1];
      a[0] = (byte) r.nextInt();

      byte[] b = GreyCodeConverter.toGreyCode(a);
      byte[] c = GreyCodeConverter.toBinary(b);

//      System.out.printf("%s %s %s\n",
//          Byte.toString(a[0]),
//          Byte.toString(b[0]),
//          Byte.toString(c[0]));
//      System.out.printf("%s\n%s\n%s\n",
//          Integer.toBinaryString(Byte.toUnsignedInt(a[0])),
//          Integer.toBinaryString(Byte.toUnsignedInt(b[0])),
//          Integer.toBinaryString(Byte.toUnsignedInt(c[0])));

      assertArrayEquals(a, c);
    }
  }

  @Test
  public void imageTest() throws IOException {
    result = new byte[3][];

    result[0] = new byte[data.length];
    System.arraycopy(data, 0, result[0], 0, data.length);

    result[1] = GreyCodeConverter.toGreyCode(result[0]);
    result[2] = GreyCodeConverter.toBinary(result[1]);

    assertArrayEquals("Wrong conversion", result[0], result[2]);

    System.arraycopy(result[1], 0, data, 0, result[1].length);
    ImageIO.write(image, "jpg", new File(paths[1]));

    System.arraycopy(result[2], 0, data, 0, result[2].length);
    ImageIO.write(image, "jpg", new File(paths[2]));
  }
}