import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

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
  private BufferedImage image;

  private BufferedImage[][] imageSegments;
  private int[][][][] imageSegmentsComplexity;

  //region Construction
  //------------------------------------------------------------------------------------------------

  public BpcsEncoder(BufferedImage image) {
    this.image = image;
    this.numOfChannels = image.getRaster().getNumBands();
    this.hasAlpha = image.getColorModel().hasAlpha();
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

  //------------------------------------------------------------------------------------------------
  //endregion

  //region Image Segmentation
  //------------------------------------------------------------------------------------------------

  public void segmentImage() {
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
            ((DataBufferByte) imageSegments[i][j].getRaster().getDataBuffer()).getData();

        for (int k = 0; k < numOfChannels; ++k) {
          imageSegmentsComplexity[i][j][k] = new int[8];
          for (int l = 0; l < 8; ++l) {
            imageSegmentsComplexity[i][j][k][l] = calculateComplexity(data, k, l);
          }
        }
      }
    }
  }

  //------------------------------------------------------------------------------------------------
  //endregion

  //region Complexity Calculation
  //------------------------------------------------------------------------------------------------

  public int calculateComplexity(byte[] data, int channel, int bitplane) {
    if (data.length != 64) {
      throw new IllegalArgumentException("Data length must be 64 (from a 8x8 image)");
    }

    if (bitplane < 0 || bitplane > 7)
      throw new IllegalArgumentException("Bitplane parameter must range from 0 to 7");

    int n = 0;
    boolean b1, b2, b3;

    for (int i = 0; i < 7; ++i) {
      for (int j = 0; j < 7; ++j) {
        b1 = getBit(data[getIndex(i, j, channel)], bitplane);
        b2 = getBit(data[getIndex(i, j + 1, channel)], bitplane);
        b3 = getBit(data[getIndex(i + 1, j, channel)], bitplane);

        if (b1 ^ b2) n++;
        if (b1 ^ b3) n++;
      }

      b1 = getBit(data[getIndex(i, 7, channel)], bitplane);
      b3 = getBit(data[getIndex(i + 1, 7, channel)], bitplane);

      if (b1 ^ b3) n++;
    }

    for (int j = 0; j < image.getWidth() - 1; ++j) {
      b1 = getBit(data[getIndex(7, j, channel)], bitplane);
      b2 = getBit(data[getIndex(7, j + 1, channel)], bitplane);

      if (b1 ^ b2) n++;
    }

    return n;
  }

  //------------------------------------------------------------------------------------------------
  //endregion

  //region Utility functions
  //------------------------------------------------------------------------------------------------

  private int getIndex(int r, int c, int channel) {
    return (r * 8 + c) * numOfChannels + channel;
  }

  private static boolean getBit(byte value, int position) {
    return ((value & (1 << position)) != 0);
  }

  //------------------------------------------------------------------------------------------------
  //endregion
}
