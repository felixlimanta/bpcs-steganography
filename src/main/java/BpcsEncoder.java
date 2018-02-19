import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import javax.naming.SizeLimitExceededException;

public class BpcsEncoder {

  public final static int ALPHA = 0;
  public final static int GREY = 1;
  public final static int RED = 1;
  public final static int GREEN = 2;
  public final static int BLUE = 3;

  // Maximum complexity for a 8x8 image (checkerboard pattern)
  private final static int maxComplexity = 112;

  private int numOfChannels;
  private boolean hasAlpha;
  private int numOfPlanes;
  private BufferedImage image;

  private int threshold;

  private BufferedImage[][] imageSegments;
  private int[][][][] imageSegmentsComplexity;

  private Random random;
  private String key;
  private Set<Integer> embeddedSet;

  //region Construction
  //------------------------------------------------------------------------------------------------

  public BpcsEncoder(BufferedImage image, float threshold) {
    this.image = Utility.deepCopy(image);
    this.numOfChannels = image.getRaster().getNumBands();
    this.hasAlpha = image.getColorModel().hasAlpha();
    this.embeddedSet = new HashSet<>();
    this.threshold = (int) (threshold * maxComplexity);
    this.numOfPlanes = image.getWidth() * image.getHeight() * numOfChannels * 8;

    toGreyCode();
    segmentImage();
    toBinary();
  }

  public BpcsEncoder(BufferedImage image, float threshold, String key) {
    this(image, threshold);
    this.key = key;
    this.random = new Random(key.hashCode());

    toGreyCode();
    segmentImage();
    toBinary();
  }

  private void toGreyCode() {
    byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
    System.arraycopy(GreyCodeConverter.toGreyCode(data), 0, data, 0, data.length);
  }

  private void toBinary() {
    byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
    System.arraycopy(GreyCodeConverter.toBinary(data), 0, data, 0, data.length);
  }

  //------------------------------------------------------------------------------------------------
  //endregion

  //region Getter/Setter
  //------------------------------------------------------------------------------------------------

  public BufferedImage getImage() {
    return image;
  }

  public int getNumOfChannels() {
    return numOfChannels;
  }

  public boolean hasAlpha() {
    return hasAlpha;
  }

  public boolean isRandom() {
    return (random != null);
  }

  //------------------------------------------------------------------------------------------------
  //endregion

  //region Image Segmentation
  //------------------------------------------------------------------------------------------------

  private void segmentImage() {
    int h = image.getHeight() / 8;
    int w = image.getWidth() / 8;

    imageSegments = new BufferedImage[h][];
    imageSegmentsComplexity = new int[h][][][];

    for (int i = 0; i < h; ++i) {
      imageSegments[i] = new BufferedImage[w];
      imageSegmentsComplexity[i] = new int[w][][];

      for (int j = 0; j < w; ++j) {
        imageSegments[i][j] = image.getSubimage(j * 8, i * 8, 8, 8);
        imageSegmentsComplexity[i][j] = new int[numOfChannels][];
        byte[] data =
            ((DataBufferByte) imageSegments[i][j]
                .getData(new Rectangle(0, 0, 8, 8)).getDataBuffer()).getData();

        for (int k = 0; k < numOfChannels; ++k) {
          imageSegmentsComplexity[i][j][k] = new int[8];
          for (int l = 0; l < 8; ++l) {
            imageSegmentsComplexity[i][j][k][l] = calculateComplexity(data, k, l);
          }
        }
      }
    }
  }

  public void combineImage() {
    BufferedImage result = new BufferedImage(
        image.getWidth(), image.getHeight(), image.getType());
    Graphics g = result.getGraphics();

    for (int i = 0; i < imageSegments.length; ++i) {
      for (int j = 0; j < imageSegments[i].length; ++j) {
        g.drawImage(imageSegments[i][j], j * 8, i * 8, null);
      }
    }
    image = result;
  }

  //------------------------------------------------------------------------------------------------
  //endregion

  //region Complexity Calculation
  //------------------------------------------------------------------------------------------------

  private int calculateComplexity(byte[] data, int channel, int bitplane) {
    if (bitplane < 0 || bitplane > 7)
      throw new IllegalArgumentException("Bitplane parameter must range from 0 to 7");

    int n = 0;
    boolean b1, b2 = false, b3;

    for (int i = 0; i < 7; ++i) {
      for (int j = 0; j < 7; ++j) {
        b1 = Utility.getBit(data[getIndex(i, j, channel)], bitplane);
        b2 = Utility.getBit(data[getIndex(i, j + 1, channel)], bitplane);
        b3 = Utility.getBit(data[getIndex(i + 1, j, channel)], bitplane);

        if (b1 ^ b2) n++;
        if (b1 ^ b3) n++;
      }

      b1 = Utility.getBit(data[getIndex(i, 7, channel)], bitplane);
      b3 = Utility.getBit(data[getIndex(i + 1, 7, channel)], bitplane);

      if (b1 ^ b3) n++;
    }

    for (int j = 0; j < 7; ++j) {
      b1 = Utility.getBit(data[getIndex(7, j, channel)], bitplane);
      b2 = Utility.getBit(data[getIndex(7, j + 1, channel)], bitplane);

      if (b1 ^ b2) n++;
    }

    return n;
  }

  //------------------------------------------------------------------------------------------------
  //endregion

  //region Message hiding
  //------------------------------------------------------------------------------------------------

  public BpcsEncoder encodeMessageInImage(Message message) throws SizeLimitExceededException {
    byte[] bytes = message.getEncodedMessage();

    int n = -1;
    for (int i = 0; i < bytes.length; i += 8) {
      int[] idx;
      do {
        n = calculateNextSequence(n);
        if (n == -1)
          throw new SizeLimitExceededException("Encoded message exceeds capacity");
        idx = getPlaneIndices(n);
      } while (imageSegmentsComplexity[idx[0]][idx[1]][idx[2]][idx[3]] < threshold);

      encodeMessageInSegment(bytes, i, imageSegments[idx[0]][idx[1]], idx[2], idx[3]);
    }
    return this;
  }

  void encodeMessageInSegment(byte[] data, int startIndex, BufferedImage segment, int channel, int bitplane) {
    byte[] imageData =
        ((DataBufferByte) segment.getRaster().getDataBuffer()).getData();

    for (int i = 0; i < 8; ++i) {
      for (int j = 0; j < 8; ++j) {
        boolean b = Utility.getBit(data[startIndex + i], j);
        int idx = getIndex(i, j, channel);
        imageData[idx] = Utility.setBit(imageData[idx], b, bitplane);
      }
    }
  }

  public Message extractMessageFromImage() throws SizeLimitExceededException {
    int n = -1;
    int[] idx;

    // Get first block of message
    do {
      n = calculateNextSequence(n);
      if (n == -1)
        throw new SizeLimitExceededException("Encoded message exceeds capacity");
      idx = getPlaneIndices(n);
    } while (imageSegmentsComplexity[idx[0]][idx[1]][idx[2]][idx[3]] < threshold);

    // Extract message length from header
    byte[] messageLength = new byte[8];
    extractMessageFromSegment(messageLength, 0, imageSegments[idx[0]][idx[1]], idx[2], idx[3]);
    int len = Message.decodeMessageLengthHeader(messageLength);
    byte[] messageData = new byte[len];

    // Extract rest of message
    for (int i = 0; i < messageData.length; i += 8) {
      do {
        n = calculateNextSequence(n);
        if (n == -1)
          throw new SizeLimitExceededException("Encoded message exceeds capacity");
        idx = getPlaneIndices(n);
      } while (imageSegmentsComplexity[idx[0]][idx[1]][idx[2]][idx[3]] < threshold);

      extractMessageFromSegment(messageData, i, imageSegments[idx[0]][idx[1]], idx[2], idx[3]);
    }

    return new Message(messageData, threshold);
  }

  void extractMessageFromSegment(byte[] data, int startIndex, BufferedImage segment, int channel, int bitplane) {
    byte[] imageData =
        ((DataBufferByte) segment.getRaster().getDataBuffer()).getData();

    for (int i = 0; i < 8; ++i) {
      for (int j = 0; j < 8; ++j) {
        int idx = getIndex(i, j, channel);
        boolean b = Utility.getBit(imageData[idx], bitplane);
        data[startIndex + i] = Utility.setBit(data[startIndex + i], b, j);
      }
    }
  }

  private int calculateNextSequence(int prev) {
    if (embeddedSet.size() >= numOfPlanes)
      return -1;

    int n;
    if (isRandom()) {
      do {
        n = random.nextInt(numOfPlanes);
      } while (embeddedSet.contains(n));
    } else {
      n = prev + 1;
    }
    embeddedSet.add(n);
    return n;
  }

  private int[] getPlaneIndices(int seq) {
    int temp[] = new int[4];

    temp[3] = seq % 8;
    seq /= 8;

    temp[2] = seq % numOfChannels;
    seq /= numOfChannels;

    temp[1] = seq % image.getWidth();
    seq /= image.getWidth();

    temp[0] = seq % image.getHeight();
    return temp;
  }

  //------------------------------------------------------------------------------------------------
  //endregion

  //region Utility functions
  //------------------------------------------------------------------------------------------------

  private int getIndex(int r, int c, int channel) {
    return (r * 8 + c) * numOfChannels + channel;
  }

  //------------------------------------------------------------------------------------------------
  //endregion
}
