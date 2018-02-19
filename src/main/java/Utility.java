import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

public class Utility {

  public static String toBinaryString(byte b) {
    return String.format("%8s", Integer.toBinaryString(Byte.toUnsignedInt(b)))
        .replace(' ', '0');
  }

  public static byte[] addPadding(byte[] b) {
    if (b.length % 8 == 0) {
      return b;
    }

    byte[] padded = new byte[b.length + (8 - (b.length % 8))];
    Arrays.fill(padded, (byte) 0);
    System.arraycopy(b, 0, padded, 0, b.length);
    return padded;
  }

  public static byte[] removePadding(byte[] b) {
    int i = b.length - 1;
    while (b[i] == 0) {
      i--;
    }

    int len = i + 1;
    byte[] depadded = new byte[len];
    System.arraycopy(b, 0, depadded, 0, len);
    return depadded;
  }

  public static boolean getBit(byte value, int position) {
    return ((value & (1 << position)) != 0);
  }

  public static byte setBit(byte b, boolean value, int position) {
    int temp = Byte.toUnsignedInt(b);
    if (value) {
      return (byte) (temp | (1 << position));
    } else {
      return (byte) (temp & ~(1 << position));
    }
  }

  public static byte[] toByteArray(ArrayList<Byte> list) {
    Byte[] array = list.toArray(new Byte[list.size()]);
    byte[] byteArray = new byte[array.length];
    IntStream.range(0, array.length).forEach(i -> byteArray[i] = array[i]);
    return byteArray;
  }
}
