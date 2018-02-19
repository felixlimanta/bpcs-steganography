public class VigenereCipher {

  private final static Operation encipherOp =
      (text, key) -> (byte) ((text + key) % 256);
  private final static Operation decipherOp =
      (text, key) -> (byte) ((text - key + 256) % 256);
  private String key;

  public VigenereCipher(String key) {
    setKey(key);
  }

  public void setKey(String key) {
    this.key = key;
  }

  public byte[] encrypt(byte[] text) {
    return processVigenere(text, encipherOp);
  }

  public byte[] decrypt(byte[] text) {
    return processVigenere(text, decipherOp);
  }

  private byte[] processVigenere(byte[] text, Operation operation) {
    byte[] bytes = new byte[text.length];

    for (int i = 0, j = 0; i < text.length; ++i, j = (j + 1) % key.length()) {
      byte c = text[i];
      bytes[i] = operation.operate(c, key.charAt(j));
    }

    return bytes;
  }

  private interface Operation {
    byte operate(byte text, char key);
  }
}
