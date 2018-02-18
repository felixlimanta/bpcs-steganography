import java.nio.charset.Charset;

public class Message {

  private String filename;
  private byte[] message;
  private byte[] encodedMessage;
  private int[] messageSegmentComplexity;

  public Message(String filename, byte[] message) {
    this.filename = filename;
    this.message = new byte[message.length];
    System.arraycopy(message, 0, this.message, 0, message.length);

    encodeMessage();
  }

  public void encodeMessage() {
    int len = filename.length();
    byte[] b = new byte[message.length + len + 2];

    b[0] = (byte) (len >> 8);
    b[1] = (byte) (len);
    byte[] filenameBytes = filename.getBytes(Charset.forName("UTF-8"));
    System.arraycopy(filenameBytes, 0, b, 2, len);

    System.arraycopy(message, 0, b, len + 2, message.length);
    b = addPadding(b);
    encodedMessage = GreyCodeConverter.toGreyCode(b);
  }

  public void decodeMessage() {
    byte[] b = GreyCodeConverter.toBinary(encodedMessage);
    b = removePadding(b);

    int len = ((b[0] & 0xff) << 8) + (b[1] & 0xff);
    byte[] filename = new byte[len];
    System.arraycopy(b, 2, filename, 0, len);
    this.filename = new String(filename);

    this.message = new byte[b.length - len - 2];
    System.arraycopy(b, len + 2, message, 0, b.length - len - 2);
  }

  public void calculateComplexity() {
    messageSegmentComplexity = new int[encodedMessage.length / 8];
    for (int i = 0; i < encodedMessage.length; i += 8) {
      messageSegmentComplexity[i] = 0;
      boolean b1, b2, b3;

      for (int j = i; j < i + 7; ++j) {
        for (int k = 0; k < 7; ++k) {
          b1 = getBit(encodedMessage[j], k);
          b2 = getBit(encodedMessage[j], k + 1);
          b3 = getBit(encodedMessage[j + 1], k);

          if (b1 ^ b2)
            messageSegmentComplexity[i]++;
          if (b1 ^ b3)
            messageSegmentComplexity[i]++;
        }

        b1 = getBit(encodedMessage[j], 7);
        b3 = getBit(encodedMessage[j + 1], 7);

        if (b1 ^ b3)
          messageSegmentComplexity[i]++;
      }

      for (int k = 0; k < 7; ++k) {
        b1 = getBit(encodedMessage[i + 7], k);
        b2 = getBit(encodedMessage[i + 7], k + 1);

        if (b1 ^ b2)
          messageSegmentComplexity[i]++;
      }
    }
  }

  private static byte[] addPadding(byte[] b) {
    if (b.length % 8 == 0)
      return b;

    byte[] padded = new byte[b.length + (8 - (b.length % 8))];
    System.arraycopy(b, 0, padded, 0, b.length);
    return padded;
  }

  private static byte[] removePadding(byte[] b) {
    int i = b.length - 1;
    while (b[i] != 0)
      i--;

    int len = i + 1;
    byte[] depadded = new byte[len];
    System.arraycopy(b, 0, depadded, 0, len);
    return depadded;
  }

  private static boolean getBit(byte value, int position) {
    return ((value & (1 << position)) != 0);
  }
}
