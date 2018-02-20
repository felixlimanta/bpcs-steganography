import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.awt.image.BufferedImage;
import org.junit.Test;

public class BpcsEncoderTest {

  private static final String path = "C:\\Users\\ASUS\\Downloads\\zkVHgAM.png";
  private float threshold = (float) 0.3;

  @Test
  public void encodingTest() throws Exception {
    String path1 = "C:\\Users\\ASUS\\Downloads\\zkVHgAM_1.png";

    BufferedImage image = Utility.loadImage(path);
    BpcsEncoder bpcsEncoder = new BpcsEncoder(image, threshold);

    String messagePath = "C:\\Users\\ASUS\\Downloads\\31P828KVQV.txt";
    byte[] messageData = Utility.loadFile(messagePath);
    Message message = new Message(messagePath, messageData, threshold).encodeMessage();

    bpcsEncoder.encodeMessageInImage(message);
    Utility.saveImage(bpcsEncoder.getImage(), path1);

    BufferedImage image1 = Utility.loadImage(path1);
    BpcsEncoder bpcsDecoder = new BpcsEncoder(image1, threshold);

    Message decodedMessage = bpcsDecoder.extractMessageFromImage().decodeMessage();

    assertEquals(messagePath, decodedMessage.getFilename());
    assertArrayEquals(messageData, decodedMessage.getMessage());
  }
}