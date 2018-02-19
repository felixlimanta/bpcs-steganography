import static org.junit.Assert.assertArrayEquals;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.naming.SizeLimitExceededException;
import org.junit.Test;

public class BpcsEncoderTest {

  private static final String path = "C:\\Users\\ASUS\\Downloads\\zkVHgAM.png";
  private float threshold = (float) 0.3;

  @Test
  public void segmentEncodingTest() throws IOException {
    BufferedImage image = Utility.loadImage(path);
    BufferedImage imageSegment = image.getSubimage(0, 0, 8, 8);

    String messagePath = "C:\\Users\\ASUS\\Downloads\\31P828KVQV.txt";
    byte[] messageData = Utility.loadFile(messagePath);
    Message message = new Message(messagePath, messageData, threshold).encodeMessage();
    messageData = message.getEncodedMessage();

    byte[] origMessage = new byte[8];
    System.arraycopy(messageData, 0, origMessage, 0, 8);

    BpcsEncoder bpcsEncoder = new BpcsEncoder(imageSegment, threshold);
    bpcsEncoder.encodeMessageInSegment(messageData, 0, imageSegment, 0, 0);

    byte[] extractedMessage = new byte[8];
    bpcsEncoder.extractMessageFromSegment(extractedMessage, 0, imageSegment, 0, 0);
//    System.out.println(Message.decodeMessageLengthHeader(extractedMessage));

    assertArrayEquals(origMessage, extractedMessage);
  }

  @Test
  public void encodingTest() throws IOException, SizeLimitExceededException {
    String path1 = "C:\\Users\\ASUS\\Downloads\\zkVHgAM_1.png";

    BufferedImage image = Utility.loadImage(path);
    BpcsEncoder bpcsEncoder = new BpcsEncoder(image, threshold);

    String messagePath = "C:\\Users\\ASUS\\Downloads\\31P828KVQV.txt";
    byte[] messageData = Utility.loadFile(messagePath);
    Message message = new Message(messagePath, messageData, threshold).encodeMessage();

    bpcsEncoder.encodeMessageInImage(message);
    bpcsEncoder.combineImage();
    Utility.saveImage(bpcsEncoder.getImage(), path1);

    BufferedImage image1 = Utility.loadImage(path1);
    BpcsEncoder bpcsDecoder = new BpcsEncoder(image1, threshold);
    Message decodedMessage = bpcsDecoder.extractMessageFromImage();
//
//    System.out.println(Utility.isEqual(bpcsEncoder.bimage, bpcsDecoder.bimage));
//    byte[] b1 = ((DataBufferByte) bpcsEncoder.bimage.getRaster().getDataBuffer()).getData();
//    byte[] b2 = ((DataBufferByte) bpcsDecoder.bimage.getRaster().getDataBuffer()).getData();
//    assertArrayEquals(b1, b2);

//
//    byte[] orig = new byte[8];
//    System.arraycopy(message.getEncodedMessage(), 0, orig, 0, 8);
////    bpcsEncoder.encodeMessageInSegment(orig, 0, bpcsDecoder.bimage, 0, 0);
//
//    System.out.println();
//    for (int k = 0; k < 8; ++k) {
//      System.out.println(Utility.toBinaryString(orig[k]));
//    }
//    System.out.println();
//
//    byte[] extracted = new byte[8];
////    bpcsEncoder.extractMessageFromSegment(extracted, 0, bpcsEncoder.bimage, 0, 0);
//    extracted = bpcsEncoder.extracted;
//
//    System.out.println();
//    for (int k = 0; k < 8; ++k) {
//      System.out.println(Utility.toBinaryString(extracted[k]));
//    }
//    System.out.println();
//
//    System.out.println(Message.decodeMessageLengthHeader(extracted));
//
//    assertArrayEquals(orig, extracted);
//
//    assertEquals(messagePath, decodedMessage.getFilename());
//    assertArrayEquals(messageData, decodedMessage.getMessage());
  }
}