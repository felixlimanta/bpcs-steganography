import java.nio.charset.Charset;
import java.util.ArrayList;

public class Message {

  // Checkerboard pattern in binary integer
  private final static int checkerboard[] = {
      0b01010101, 0b10101010
  };

  // Maximum complexity for a 8x8 image (checkerboard pattern)
  private final static int maxComplexity = 112;
  private int[] messageSegmentComplexity;
  private String filename;
  private byte[] message;
  private byte[] encodedMessage;
  private double threshold;

  private boolean[] conjugationMap;
  private boolean encoded;

  //region Public functions
  //------------------------------------------------------------------------------------------------

  public Message(String filename, byte[] message, double threshold) {
    this.filename = filename;
    this.message = new byte[message.length];
    this.threshold = threshold * maxComplexity;
    System.arraycopy(message, 0, this.message, 0, message.length);
    encoded = false;
  }

  public Message(byte[] encodedMessage, double threshold) {
    this.encodedMessage = new byte[encodedMessage.length];
    System.arraycopy(encodedMessage, 0, this.encodedMessage, 0, encodedMessage.length);
    this.threshold = threshold * maxComplexity;
    encoded = true;
  }

  public Message encodeMessage() {
    if (!encoded) {
      processMessageEncoding();
      calculateMessageComplexity();
      conjugateMessage();
      encodeConjugationMap();
      encoded = true;
    }
    return this;
  }

  public Message decodeMessage() {
    if (encoded) {
      decodeConjugationMap();
      deconjugateMessage();
      processMessageDecoding();
      encoded = false;
    }
    return this;
  }

  //------------------------------------------------------------------------------------------------
  //endregion

  //region Getter/setter
  //------------------------------------------------------------------------------------------------

  public boolean isEncoded() {
    return encoded;
  }

  public String getFilename() {
    return filename;
  }

  public byte[] getMessage() {
    return message;
  }

  public byte[] getEncodedMessage() {
    return encodedMessage;
  }

  //------------------------------------------------------------------------------------------------
  //endregion

  //region Message encoding/decoding
  //------------------------------------------------------------------------------------------------

  private void processMessageEncoding() {
    int len = filename.length();
    byte[] b = new byte[message.length + len + 2];

    b[0] = (byte) (len >> 8);
    b[1] = (byte) (len);
    byte[] filenameBytes = filename.getBytes(Charset.forName("UTF-8"));
    System.arraycopy(filenameBytes, 0, b, 2, len);

    System.arraycopy(message, 0, b, len + 2, message.length);
    b = Utility.addPadding(b);
    encodedMessage = GreyCodeConverter.toGreyCode(b);
  }

  private void processMessageDecoding() {
    byte[] b = GreyCodeConverter.toBinary(encodedMessage);
    b = Utility.removePadding(b);

    int len = ((b[0] & 0xff) << 8) + (b[1] & 0xff);
    byte[] filename = new byte[len];
    System.arraycopy(b, 2, filename, 0, len);
    this.filename = new String(filename);

    this.message = new byte[b.length - len - 2];
    System.arraycopy(b, len + 2, message, 0, b.length - len - 2);
  }

  //------------------------------------------------------------------------------------------------
  //endregion

  //region Complexity calculation
  //------------------------------------------------------------------------------------------------

  private void calculateMessageComplexity() {
    messageSegmentComplexity = new int[encodedMessage.length / 8];
    for (int i = 0; i < messageSegmentComplexity.length; ++i) {
      messageSegmentComplexity[i] = calculateSegmentComplexity(encodedMessage, i * 8);
    }
  }

  private static int calculateSegmentComplexity(byte[] data, int startIndex) {
    int n = 0;
    boolean b1, b2, b3;
    for (int i = startIndex; i < startIndex + 7; ++i) {
      for (int j = 0; j < 7; ++j) {
        b2 = Utility.getBit(data[i], j + 1);
        b1 = Utility.getBit(data[i], j);
        b3 = Utility.getBit(data[i + 1], j);

        if (b1 ^ b2) {
          n++;
        }
        if (b1 ^ b3) {
          n++;
        }
      }

      b1 = Utility.getBit(data[i], 7);
      b3 = Utility.getBit(data[i + 1], 7);

      if (b1 ^ b3) {
        n++;
      }
    }

    for (int j = 0; j < 7; ++j) {
      b1 = Utility.getBit(data[startIndex + 7], j);
      b2 = Utility.getBit(data[startIndex + 7], j + 1);

      if (b1 ^ b2) {
        n++;
      }
    }
    return n;
  }

  public boolean areAllSegmentsComplex() {
    for (int i = 0; i < encodedMessage.length; i += 8) {
      int complexity = calculateSegmentComplexity(encodedMessage, i);
      if (complexity < threshold) {
        return false;
      }
    }
    return true;
  }

  //------------------------------------------------------------------------------------------------
  //endregion

  //region Message conjugation
  //------------------------------------------------------------------------------------------------

  private void conjugateMessage() {
    conjugationMap = new boolean[messageSegmentComplexity.length];

    for (int i = 0; i < conjugationMap.length; ++i) {
      conjugationMap[i] = (messageSegmentComplexity[i] < threshold);
      if (conjugationMap[i]) {
        conjugateBlock(encodedMessage, i * 8);
      }
    }
  }

  private void deconjugateMessage() {
    for (int i = 0; i < conjugationMap.length; ++i) {
      if (conjugationMap[i]) {
        conjugateBlock(encodedMessage, i * 8);
        conjugationMap[i] = false;
      }
    }
  }

  private static void conjugateBlock(byte[] data, int startIndex) {
    for (int i = startIndex; i < startIndex + 8; ++i) {
      int temp = Byte.toUnsignedInt(data[i]);
      temp ^= checkerboard[i % 2];
      data[i] = (byte) temp;
    }
  }

  //------------------------------------------------------------------------------------------------
  //endregion

  //region Header encoding/decoding
  //------------------------------------------------------------------------------------------------

  private void encodeConjugationMap() {
    ArrayList<Byte> conjugationList = new ArrayList<>();
    int len = conjugationMap.length;

    // Add length header
    // Leave one byte for conjugation info
    conjugationList.add((byte) 0);
    conjugationList.add((byte) (len >> 24));
    conjugationList.add((byte) (len >> 16));
    conjugationList.add((byte) (len >> 8));
    conjugationList.add((byte) (len));

    // Construct conjugation list
    int n = 5;
    int j = 0;
    byte temp = 0;
    for (int i = 0; i < len; ++i) {
      if (j >= 8) {
        conjugationList.add(temp);
        temp = 0;
        j = 0;
        n++;
      }

      // Leave bit for conjugation flag
      if (n % 8 == 0 && j == 0) {
        j++;
      }

      temp = Utility.setBit(temp, conjugationMap[i], j++);
    }
    conjugationList.add(temp);

    // Convert to byte array, padding
    byte[] conjugationBlock = Utility.toByteArray(conjugationList);
    conjugationBlock = Utility.addPadding(conjugationBlock);

    // Conjugate conjugation block
    for (int i = 0; i < conjugationBlock.length; i += 8) {
      int complexity = calculateSegmentComplexity(conjugationBlock, i);
      if (complexity < threshold) {
        conjugateBlock(conjugationBlock, i);
      }
    }

    // Add message length header
    byte[] lenHeader = new byte[8];
    len = conjugationBlock.length + encodedMessage.length;
    lenHeader[0] = lenHeader[5] = lenHeader[6] = lenHeader[7] = 0;
    lenHeader[1] = (byte) (len >> 24);
    lenHeader[2] = (byte) (len >> 16);
    lenHeader[3] = (byte) (len >> 8);
    lenHeader[4] = (byte) (len);

    // Conjugate message length header
    int complexity = calculateSegmentComplexity(lenHeader, 0);
    if (complexity < threshold) {
      conjugateBlock(lenHeader, 0);
    }

    // Concatenate with message
    byte[] concatedMessage = new byte[conjugationBlock.length + encodedMessage.length + 8];
    System.arraycopy(lenHeader, 0, concatedMessage, 0, 8);
    System.arraycopy(conjugationBlock, 0, concatedMessage, 8, conjugationBlock.length);
    System.arraycopy(encodedMessage, 0, concatedMessage, conjugationBlock.length + 8,
        encodedMessage.length);
    encodedMessage = concatedMessage;
  }

  private void decodeConjugationMap() {
    // Conjugate first block if necessary
    if (Utility.getBit(encodedMessage[0], 0)) {
      conjugateBlock(encodedMessage, 0);
    }

    // Calculate length
    int len = encodedMessage[1] << 24
        | (encodedMessage[2] & 0xFF) << 16
        | (encodedMessage[3] & 0xFF) << 8
        | (encodedMessage[4] & 0xFF);

    // Reconstruct conjugation map
    conjugationMap = new boolean[len];
    int n = 5;
    int j = 0;
    for (int i = 0; i < len; ++i) {
      if (j >= 8) {
        j = 0;
        n++;
      }

      // Conjugate block if flag is set
      if (n % 8 == 0 && j == 0) {
        if (Utility.getBit(encodedMessage[n], 0)) {
          conjugateBlock(encodedMessage, n);
        }
        j++;
      }

      conjugationMap[i] = Utility.getBit(encodedMessage[n], j++);
    }

    // Skip padding
    while (n % 8 != 0) {
      n++;
    }

    // Extract message
    byte[] msg = new byte[encodedMessage.length - n];
    System.arraycopy(encodedMessage, n, msg, 0, msg.length);
    encodedMessage = msg;
  }

  public static int decodeMessageLengthHeader(byte[] lengthHeader) {
    if (lengthHeader.length != 8) {
      throw new IllegalArgumentException("Length header must have length of 8");
    }

    byte[] len = new byte[8];
    System.arraycopy(lengthHeader, 0, len, 0, 8);

    // Conjugate first block if necessary
    if (Utility.getBit(len[0], 0)) {
      conjugateBlock(len, 0);
    }

    return len[1] << 24
        | (len[2] & 0xFF) << 16
        | (len[3] & 0xFF) << 8
        | (len[4] & 0xFF);
  }

  //------------------------------------------------------------------------------------------------
  //endregion
}
