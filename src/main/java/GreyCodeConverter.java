public class GreyCodeConverter {

  public static byte[] toGreyCode(byte[] binary) {
    byte[] res = new byte[binary.length];
    for (int i = 0; i < binary.length; ++i) {
      int temp = Byte.toUnsignedInt(binary[i]);
      res[i] = (byte) (temp ^ (temp >> 1));
    }
    return res;
  }

  public static byte[] toBinary(byte[] grey) {
    byte[] res = new byte[grey.length];
    for (int i = 0; i < grey.length; ++i) {
      int temp = Byte.toUnsignedInt(grey[i]);
      for (int mask = temp >> 1; mask != 0; mask >>= 1) {
        temp ^= mask;
      }
      res[i] = (byte) temp;
    }
    return res;
  }
}
