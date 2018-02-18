import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class BpcsEncoder {

  private int numOfChannels;
  private BufferedImage image;
  private Channel channel;

  public BpcsEncoder(BufferedImage image) {
    this.image = image;
    if (image.getColorModel().hasAlpha()) {
      numOfChannels = 4;
    } else {
      if (channel == Channel.ALPHA) {
        throw new IllegalArgumentException("No alpha channel in image");
      } else {
        numOfChannels = 3;
      }
    }
  }

  public BufferedImage getImage() {
    return image;
  }

  public Channel getChannel() {
    return channel;
  }

  public void setChannel(Channel channel) {
    this.channel = channel;
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
    return (r * image.getWidth() + c) * numOfChannels
        + channel.getValue()
        - ((numOfChannels == 3) ? 1 : 0);
  }

  private static boolean getBit(byte value, int position) {
    return ((value & (1 << position)) != 0);
  }

  public enum Channel {
    ALPHA(0), RED(1), GREEN(2), BLUE(3);

    private int value;

    Channel(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }
  }
}
