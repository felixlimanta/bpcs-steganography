import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.Test;

public class MessageTest {

  float threshold = (float) 0.3;

  @Test
  public void textEncodingTest() {
    String filename = "abcde";
    String text = "abcdefghij";

    Message m = new Message(filename, text.getBytes(), threshold);
    m.encodeMessage();

    byte[] res = m.getEncodedMessage();

    byte[] res1 = new byte[8];
    System.arraycopy(res, 0, res1, 0, 8);
    int len = Message.decodeMessageLengthHeader(res1);

    byte[] res2 = new byte[res.length - 8];
    System.arraycopy(res, 8, res2, 0, res.length - 8);

    Message m2 = new Message(res2, threshold);
    m2.decodeMessage();

    assertEquals(len, res2.length);
    assertEquals(filename, m2.getFilename());
    assertEquals(text, new String(m2.getMessage()));
  }

  @Test
  public void fileEncodingTest() throws IOException {
    String path = "C:\\Users\\ASUS\\Downloads\\64166790_p0.jpg";
    BufferedImage image = ImageIO.read(new File(path));
    byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

    byte[] orig = new byte[data.length];
    System.arraycopy(data, 0, orig, 0, data.length);

    Message m = new Message(path, orig, threshold).encodeMessage();
    byte[] encoded = m.getEncodedMessage();

    byte[] encodedLen = new byte[8];
    System.arraycopy(encoded, 0, encodedLen, 0, 8);

    byte[] encodedData = new byte[encoded.length - 8];
    System.arraycopy(encoded, 8, encodedData, 0, encodedData.length);

    Message m2 = new Message(encodedData, threshold).decodeMessage();

    String path2 = "C:\\Users\\ASUS\\Downloads\\64166790_p1.jpg";
    System.arraycopy(m2.getMessage(), 0, data, 0, m2.getMessage().length);
    ImageIO.write(image, "jpg", new File(path2));

    assertEquals(path, m2.getFilename());
    assertArrayEquals(orig, m2.getMessage());
  }
}