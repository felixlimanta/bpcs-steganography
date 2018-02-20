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

  private int[][][][] imageSegmentsComplexity;

  private BufferedImage image;
  private int numOfChannels;
  private boolean hasAlpha;
  private int numOfPlanes;
  private double threshold;

  private Random random;
  private String key;

  private Set<Integer> embeddedSet;

  //region Construction
  //------------------------------------------------------------------------------------------------

  public BpcsEncoder(BufferedImage image, double threshold) {
    this.image = Utility.deepCopy(image);
    this.numOfChannels = image.getRaster().getNumBands();
    this.hasAlpha = image.getColorModel().hasAlpha();
    this.embeddedSet = new HashSet<>();
    this.threshold = threshold * maxComplexity;
    this.numOfPlanes = image.getWidth() * image.getHeight() * numOfChannels * 8;

//    toGreyCode();
    calculateImageComplexity();
//    toBinary();
  }

  public BpcsEncoder(BufferedImage image, double threshold, String key) {
    this(image, threshold);
    this.key = key;
    this.random = new Random(key.hashCode());

//    toGreyCode();
    calculateImageComplexity();
//    toBinary();
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

  public double getThreshold() {
    return threshold;
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

  private void calculateImageComplexity() {
    int h = image.getHeight() / 8;
    int w = image.getWidth() / 8;

    imageSegmentsComplexity = new int[h][][][];

    for (int y = 0; y < h; ++y) {
      imageSegmentsComplexity[y] = new int[w][][];

      for (int x = 0; x < w; ++x) {
        imageSegmentsComplexity[y][x] = new int[numOfChannels][];

        for (int channel = 0; channel < numOfChannels; ++channel) {
          imageSegmentsComplexity[y][x][channel] = new int[8];

          for (int bitplane = 0; bitplane < 8; ++bitplane) {
            RasterIndex ri = new RasterIndex(x * 8, y * 8, channel, bitplane);
            imageSegmentsComplexity[y][x][channel][bitplane] =
                calculateSegmentComplexity(ri);
          }
        }
      }
    }
  }

  private int calculateSegmentComplexity(RasterIndex ri) {
    if (ri.bitplane < 0 || ri.bitplane > 7) {
      throw new IllegalArgumentException("Bitplane parameter must range from 0 to 7");
    }

    int n = 0;
    boolean b1, b2, b3;
    byte[] data = getImageData();

    for (int i = ri.y; i < ri.y + 7; ++i) {
      for (int j = ri.x; j < ri.x + 7; ++j) {
        b1 = Utility.getBit(data[getImageDataIndex(i, j, ri.channel)], ri.bitplane);
        b2 = Utility.getBit(data[getImageDataIndex(i, j + 1, ri.channel)], ri.bitplane);
        b3 = Utility.getBit(data[getImageDataIndex(i + 1, j, ri.channel)], ri.bitplane);

        if (b1 ^ b2) {
          n++;
        }
        if (b1 ^ b3) {
          n++;
        }
      }

      b1 = Utility.getBit(data[getImageDataIndex(i, ri.x + 7, ri.channel)], ri.bitplane);
      b3 = Utility.getBit(data[getImageDataIndex(i + 1, ri.x + 7, ri.channel)], ri.bitplane);

      if (b1 ^ b3) {
        n++;
      }
    }

    for (int j = ri.x; j < ri.x + 7; ++j) {
      b1 = Utility.getBit(data[getImageDataIndex(ri.y + 7, j, ri.channel)], ri.bitplane);
      b2 = Utility.getBit(data[getImageDataIndex(ri.y + 7, j + 1, ri.channel)], ri.bitplane);

      if (b1 ^ b2) {
        n++;
      }
    }

    return n;
  }

  private int getSegmentComplexity(RasterIndex ri) {
    return imageSegmentsComplexity[ri.y / 8][ri.x / 8][ri.channel][ri.bitplane];
  }

  public int getMaximumCapacity() {
    int h = image.getHeight() / 8;
    int w = image.getWidth() / 8;
    int n = 0;

    for (int y = 0; y < h; ++y) {
      for (int x = 0; x < w; ++x) {
        for (int channel = 0; channel < numOfChannels; ++channel) {
          for (int bitplane = 0; bitplane < 8; ++bitplane) {
            if (imageSegmentsComplexity[y][x][channel][bitplane] > threshold)
              n++;
          }
        }
      }
    }

    n *= 8;
    return (int) (0.984375 * n - 278.076955);
  }

  //------------------------------------------------------------------------------------------------
  //endregion

  //region Message Hiding
  //------------------------------------------------------------------------------------------------

  public BpcsEncoder encodeMessageInImage(Message message) throws Exception {
    if (!message.areAllSegmentsComplex()) {
      throw new Exception("Message not complex in some segments");
    }

    byte[] bytes = message.getEncodedMessage();
    RasterIndex ri = new RasterIndex(-1);

    if (bytes.length > getMaximumCapacity())
      throw new SizeLimitExceededException("Message larger than image capacity at " + threshold / maxComplexity + " threshold");

    for (int i = 0; i < bytes.length; i += 8) {
      do {
        ri = ri.next();
      } while (getSegmentComplexity(ri) < threshold);

      encodeMessageInSegment(bytes, i, ri);
    }

    return this;
  }

  private void encodeMessageInSegment(byte[] data, int dataStartIndex, RasterIndex ri) {
    byte[] imageData = getImageData();

    for (int i = 0; i < 8; ++i) {
      for (int j = 0; j < 8; ++j) {
        boolean b = Utility.getBit(data[dataStartIndex + i], j);
        int idx = getImageDataIndex(ri.y + i, ri.x + j, ri.channel);
        imageData[idx] = Utility.setBit(imageData[idx], b, ri.bitplane);
      }
    }
  }

  public Message extractMessageFromImage() throws SizeLimitExceededException {
    RasterIndex ri = new RasterIndex(-1);

    // Get first block of message
    do {
      ri = ri.next();
    } while (getSegmentComplexity(ri) < threshold);

    // Extract message length from header
    byte[] messageLength = new byte[8];
    extractMessageFromSegment(messageLength, 0, ri);
    int len = Message.decodeMessageLengthHeader(messageLength);

    // Extract rest of message
    byte[] messageData = new byte[len];
    for (int i = 0; i < messageData.length; i += 8) {
      do {
        ri = ri.next();
      } while (imageSegmentsComplexity[ri.y / 8][ri.x / 8][ri.channel][ri.bitplane] < threshold);

      extractMessageFromSegment(messageData, i, ri);
    }

    return new Message(messageData, threshold);
  }

  private void extractMessageFromSegment(byte[] data, int dataStartIndex, RasterIndex ri) {
    byte[] imageData = getImageData();

    for (int i = 0; i < 8; ++i) {
      for (int j = 0; j < 8; ++j) {
        int idx = getImageDataIndex(ri.y + i, ri.x + j, ri.channel);
        boolean b = Utility.getBit(imageData[idx], ri.bitplane);
        data[dataStartIndex + i] = Utility.setBit(data[dataStartIndex + i], b, j);
      }
    }
  }

  //------------------------------------------------------------------------------------------------
  //endregion

  //region Utility functions
  //------------------------------------------------------------------------------------------------

  private int getImageDataIndex(int r, int c, int channel) {
    if (r < 0 || r >= image.getHeight() || c < 0 || c >= image.getWidth() || channel < 0
        || channel >= numOfChannels) {
      throw new IllegalArgumentException("Invalid values");
    }

    return (r * image.getWidth() + c) * numOfChannels + channel;
  }

  private byte[] getImageData() {
    return ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
  }

  private void toGreyCode() {
    byte[] data = getImageData();
    System.arraycopy(GreyCodeConverter.toGreyCode(data), 0, data, 0, data.length);
  }


  private void toBinary() {
    byte[] data = getImageData();
    System.arraycopy(GreyCodeConverter.toBinary(data), 0, data, 0, data.length);
  }

  //------------------------------------------------------------------------------------------------
  //endregion

  static class PublicIndex {

  }

  class RasterIndex extends PublicIndex {

    int x;
    int y;
    int channel;
    int bitplane;

    int index;

    RasterIndex(int x, int y, int channel, int bitplane) {
      this.x = x;
      this.y = y;
      this.channel = channel;
      this.bitplane = bitplane;
    }

    RasterIndex(int index) {
      this.index = index;
    }

    RasterIndex next() throws SizeLimitExceededException {
      int i = calculateNextSequence(this.index);

      if (i == -1) {
        throw new SizeLimitExceededException("Encoded message exceeds capacity");
      }

      return getPlaneParams(i);
    }

    private int calculateNextSequence(int prev) {
      if (embeddedSet.size() >= numOfPlanes) {
        return -1;
      }

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

    private RasterIndex getPlaneParams(int index) {
      RasterIndex ri = new RasterIndex(index);

      ri.bitplane = index % 8;
      index /= 8;

      ri.channel = index % numOfChannels;
      index /= numOfChannels;

      ri.x = (index % (image.getWidth() / 8)) * 8;
      index /= (image.getWidth() / 8);

      ri.y = (index % (image.getHeight() / 8)) * 8;
      return ri;
    }
  }
}
