import static org.junit.Assert.assertArrayEquals;

import java.awt.image.BufferedImage;
import org.hamcrest.core.IsEqual;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

public class BpcsEncoderTest {

  private static final String imgPath = "src/test/resources/img_512x512_rgb.bmp";
  private static final String messagePath = "src/test/resources/msg_1KB.txt";
  private double threshold = 0.5;

  @Rule
  public ErrorCollector collector = new ErrorCollector();

  @Test
  public void simpleEncodingTest() throws Exception {
    for (threshold = 0.1; threshold <= 0.5; threshold += 0.05) {
      String imgPath1 = "src/test/resources/img_512x512_rgb_1.png";

      BufferedImage image = Utility.loadImage(imgPath);
      BpcsEncoder bpcsEncoder = new BpcsEncoder(image, threshold);

      byte[] messageData = Utility.loadFile(messagePath);
      Message message = new Message(messagePath, messageData, threshold).encodeMessage();

      bpcsEncoder.encodeMessageInImage(message);
      Utility.saveImage(bpcsEncoder.getImage(), imgPath1);

      BufferedImage image1 = Utility.loadImage(imgPath1);
      BpcsEncoder bpcsDecoder = new BpcsEncoder(image1, threshold);

      Message decodedMessage = bpcsDecoder.extractMessageFromImage().decodeMessage();

      collector.checkThat("Threshold: " + threshold, messagePath, IsEqual.equalTo(decodedMessage.getFilename()));
      collector.checkSucceeds(() -> {
        assertArrayEquals("Threshold: " + threshold, messageData, decodedMessage.getMessage());
        return null;
      });

      image = Utility.loadImage(imgPath);
      image1 = Utility.loadImage(imgPath1);
      System.out.printf("Threshold: %f\t\tPSNR: %f\n", threshold, PsnrCalculator.calculatePSNR(image, image1));
    }
  }

  @Test
  public void randomPlacementEncodingTest() throws Exception {
    for (threshold = 0.1; threshold <= 0.5; threshold += 0.05) {
      String imgPath1 = "src/test/resources/img_512x512_rgb_1.png";

      BufferedImage image = Utility.loadImage(imgPath);
      BpcsEncoder bpcsEncoder = new BpcsEncoder(image, threshold, "key");

      byte[] messageData = Utility.loadFile(messagePath);
      Message message = new Message(messagePath, messageData, threshold).encodeMessage();

      bpcsEncoder.encodeMessageInImage(message);
      Utility.saveImage(bpcsEncoder.getImage(), imgPath1);

      BufferedImage image1 = Utility.loadImage(imgPath1);
      BpcsEncoder bpcsDecoder = new BpcsEncoder(image1, threshold, "key");

      Message decodedMessage = bpcsDecoder.extractMessageFromImage().decodeMessage();

      collector.checkThat(messagePath, IsEqual.equalTo(decodedMessage.getFilename()));
      collector.checkSucceeds(() -> {
        assertArrayEquals("Threshold: " + threshold, messageData, decodedMessage.getMessage());
        return null;
      });

      image = Utility.loadImage(imgPath);
      image1 = Utility.loadImage(imgPath1);
      System.out.printf("Threshold: %f\t\tPSNR: %f\n", threshold, PsnrCalculator.calculatePSNR(image, image1));
    }
  }
}