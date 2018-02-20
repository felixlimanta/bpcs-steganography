import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Random;
import org.junit.Test;

public class MessageTest {

  String path = "C:\\Users\\ASUS\\Downloads\\64166790_p0.jpg";
  String path1 = "C:\\Users\\ASUS\\Downloads\\64166790_p0_1.jpg";
  String path2 = "C:\\Users\\ASUS\\Downloads\\64166790_p0_2.jpg";
  float threshold = (float) 0.3;

  @Test
  public void complexityTest() throws IOException {
    byte[] data = Utility.loadFile(path);

    Message m = new Message(path, data, threshold).encodeMessage();
    assertTrue(m.areAllSegmentsComplex());
  }

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
    byte[] orig = Utility.loadFile(path);

    Message m = new Message(path, orig, threshold).encodeMessage();
    byte[] encoded = m.getEncodedMessage();

    byte[] encodedData = new byte[encoded.length - 8];
    System.arraycopy(encoded, 8, encodedData, 0, encodedData.length);

    Message m2 = new Message(encodedData, threshold).decodeMessage();
    Utility.saveFile(path2, m2.getMessage());

    assertEquals(path, m2.getFilename());
    assertArrayEquals(orig, m2.getMessage());
  }

  @Test
  public void encodedMessageSizeRegression() {
    // 256 bytes
    String filename = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Suspend"
        + "isse efficitur quis odio iaculis consequat. Nunc ullamcorper odi"
        + "o in purus imperdiet condimentum. Cras nunc sapien, efficitur qu"
        + "is aliquet nec, gravida eu odio. Etiam dui risus, pulvinar amet.";

    final int n = 10000;
    int[] x = new int[n];
    int[] y = new int[n];

    for (int i = 0; i < n; ++i) {
      Random r = new Random();
      int len = r.nextInt(1 << 20);

      byte[] data = new byte[len];
      r.nextBytes(data);

      Message m = new Message(filename, data, 0.3);
      m.encodeMessage();

      y[i] = data.length;
      x[i] = m.getEncodedMessage().length;
    }

    double[] reg = Utility.calculateLinearRegression(x, y);
    System.out.printf("Formula: y = %f x + %f\n", reg[0], reg[1]);
  }
}