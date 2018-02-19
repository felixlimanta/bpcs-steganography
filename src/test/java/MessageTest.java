import org.junit.Before;
import org.junit.Test;

public class MessageTest {

  String filename = "abcde";
  String text = "abcdefghij";
  float threshold = (float) 0.3;

  @Before
  public void setUp() {

  }

  @Test
  public void encodingTest() {
    Message m = new Message(filename, text.getBytes(), threshold);
    m.encodeMessage();

    byte[] res = m.getEncodedMessage();
    for (int i = 0; i < res.length; ++i) {
      System.out.printf("%d:\t%s %c\n", i,
          String.format("%8s", Integer.toBinaryString(Byte.toUnsignedInt(res[i])))
              .replace(' ', '0'),
          (char) Byte.toUnsignedInt(res[i]));
    }

    byte[] res1 = new byte[8];
    System.arraycopy(res, 0, res1, 0, 8);
    int len = Message.decodeMessageLengthHeader(res1);
    System.out.printf("\nNo of blocks: %d\n", len);

    byte[] res2 = new byte[res.length - 8];
    System.arraycopy(res, 8, res2, 0, res.length - 8);

    Message m2 = new Message(res2, threshold);
    m2.decodeMessage();
    System.out.printf("Filename: %s\n", m2.getFilename());
    System.out.printf("Message: %s\n", new String(m2.getMessage()));
  }
}