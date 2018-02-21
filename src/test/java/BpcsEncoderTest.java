import static org.junit.Assert.assertArrayEquals;

import java.awt.image.BufferedImage;
import java.io.IOException;
import org.hamcrest.core.IsEqual;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

public class BpcsEncoderTest {

  private final static String path = "src/test/resources/";
  private final static String[] imgNames = {
//      "img_512x512_gra.png",
//      "img_512x512_rgba.png",
      "img_1024x1024_rgb.bmp",
//      "img_512x512_gr.png",
      "img_512x512_rgb.png",
      "img_1920x1080_rgb.png"
  };
  private final static String[] msgNames = {
      "msg_128B.txt",
      "msg_256B.txt",
      "msg_1KB.txt",
      "msg_16KB.txt",
      "msg_725KB.gif"
  };
  private final static String tempImgName = "temp.png";
  
  @Rule
  public ErrorCollector collector = new ErrorCollector();
  private double threshold = 0.5;

  @Test
  public void simpleEncodingTest() throws Exception {
    for (String imgName: imgNames) {
      for (String msgName: msgNames) {
        System.out.println(imgName);
        System.out.println(msgName);
        for (threshold = 0.1; threshold <= 0.5; threshold += 0.1) {
          String imgPath = path + imgName;
          String msgPath = path + msgName;
          String imgPath1 = path + tempImgName;

          BufferedImage image = Utility.loadImage(imgPath);
          BpcsEncoder bpcsEncoder = new BpcsEncoder(image, threshold);

          final long startTime = System.currentTimeMillis();

          byte[] messageData = Utility.loadFile(msgPath);
          Message message = new Message(msgPath, messageData, threshold).encodeMessage();

          Message decodedMessage;
          BufferedImage image1;
          try {
            bpcsEncoder.encodeMessageInImage(message);
            Utility.saveImage(bpcsEncoder.getImage(), imgPath1);

            image1 = Utility.loadImage(imgPath1);
            BpcsEncoder bpcsDecoder = new BpcsEncoder(image1, threshold);

            decodedMessage = bpcsDecoder.extractMessageFromImage().decodeMessage();

            collector.checkThat("Threshold: " + threshold, msgPath,
                IsEqual.equalTo(decodedMessage.getFilename()));
            collector.checkSucceeds(() -> {
              assertArrayEquals("Threshold: " + threshold, messageData, decodedMessage.getMessage());
              return null;
            });

            final long endTime = System.currentTimeMillis();

            image = Utility.loadImage(imgPath);
            image1 = Utility.loadImage(imgPath1);
            System.out.printf("Threshold: %f\t\tPSNR: %f\t\tTime: %dms\n", threshold,
                PsnrCalculator.calculatePSNR(image, image1), endTime - startTime);

          } catch (Exception e) {
            e.printStackTrace();
          }
          return;
        }
        System.out.println();
      }
      System.out.println("============");
    }
  }

  @Test
  public void capacityTest() throws IOException {
    for (String imgName: imgNames) {
      for (String msgName: msgNames) {
        System.out.println(imgName);
        System.out.println(msgName);
        for (threshold = 0.1; threshold <= 0.5; threshold += 0.1) {
          String imgPath = path + imgName;
          String msgPath = path + msgName;
          String imgPath1 = path + tempImgName;

          BufferedImage image = Utility.loadImage(imgPath);
          BpcsEncoder bpcsEncoder = new BpcsEncoder(image, threshold, "miyazonokaori");
          System.out.println(bpcsEncoder.getMaximumCapacity());

        }
        System.out.println();
      }
      System.out.println("============");
    }
  }

  @Ignore
  @Test
  public void randomPlacementEncodingTest() throws Exception {
    for (String imgName: imgNames) {
      for (String msgName: msgNames) {
        System.out.println(imgName);
        System.out.println(msgName);
        for (threshold = 0.1; threshold <= 0.5; threshold += 0.1) {
          String imgPath = path + imgName;
          String msgPath = path + msgName;
          String imgPath1 = path + tempImgName;

          BufferedImage image = Utility.loadImage(imgPath);
          BpcsEncoder bpcsEncoder = new BpcsEncoder(image, threshold, "miyazonokaori");

          final long startTime = System.currentTimeMillis();

          byte[] messageData = Utility.loadFile(msgPath);
          Message message = new Message(msgPath, messageData, threshold).encodeMessage();

          bpcsEncoder.encodeMessageInImage(message);
          Utility.saveImage(bpcsEncoder.getImage(), imgPath1);

          BufferedImage image1 = Utility.loadImage(imgPath1);
          BpcsEncoder bpcsDecoder = new BpcsEncoder(image1, threshold, "miyazonokaori");

          Message decodedMessage = bpcsDecoder.extractMessageFromImage().decodeMessage();

          final long endTime = System.currentTimeMillis();

          collector.checkThat("Threshold: " + threshold, msgPath,
              IsEqual.equalTo(decodedMessage.getFilename()));
          collector.checkSucceeds(() -> {
            assertArrayEquals("Threshold: " + threshold, messageData, decodedMessage.getMessage());
            return null;
          });

          image = Utility.loadImage(imgPath);
          image1 = Utility.loadImage(imgPath1);
          System.out.printf("Threshold: %f\t\tPSNR: %f\t\tTime: %dms\n", threshold,
              PsnrCalculator.calculatePSNR(image, image1), endTime - startTime);
        }
        System.out.println();
      }
      System.out.println("============");
    }
  }
}