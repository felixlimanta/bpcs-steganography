import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class BpcsEncoder {

  public final static int ALPHA = 0;
  public final static int GREY = 1;
  public final static int RED = 1;
  public final static int GREEN = 2;
  public final static int BLUE = 3;

  private int numOfChannels;
  private boolean hasAlpha;
  private BufferedImage image;

  private int channel;

  public BpcsEncoder(BufferedImage image) {
    this.image = image;
    this.numOfChannels = image.getRaster().getNumBands();
    this.hasAlpha = image.getColorModel().hasAlpha();
  }

  public BufferedImage getImage() {
    return image;
  }

  public int getChannel() {
    return channel;
  }

  public void setChannel(int channel) {
    if (!hasAlpha)
      channel--;
    if (channel >= numOfChannels)
      throw new IllegalArgumentException("Invalid channel");
    this.channel = channel;
  }

  public int getNumOfChannels() {
    return numOfChannels;
  }

  public boolean hasAlpha() {
    return hasAlpha;
  }

  public int calculateComplexity(int bitplane) {
    if (bitplane < 0 || bitplane > 7)
      throw new IllegalArgumentException("Bitplane parameter must range from 0 to 7");

    byte[] data = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

    int n = 0, i, j;
    for (i = 0; i < image.getHeight() - 1; ++i) {
      for (j = 0; j < image.getWidth() - 1; ++j) {
        if (getBit(data[getIndex(i, j)], bitplane) ^ getBit(data[getIndex(i, j + 1)], bitplane))
          n++;
        if (getBit(data[getIndex(i, j)], bitplane) ^ getBit(data[getIndex(i + 1, j)], bitplane))
          n++;
      }
      if (getBit(data[getIndex(i, j)], bitplane) ^ getBit(data[getIndex(i + 1, j)], bitplane))
        n++;
    }
    for (j = 0; j < image.getWidth() - 1; ++j) {
      if (getBit(data[getIndex(i, j)], bitplane) ^ getBit(data[getIndex(i, j + 1)], bitplane))
        n++;
    }

    return n;
  }

  private int getIndex(int r, int c) {
    return (r * image.getWidth() + c) * numOfChannels + channel;
  }

  private static boolean getBit(byte value, int position) {
    return ((value & (1 << position)) != 0);
  }
}
