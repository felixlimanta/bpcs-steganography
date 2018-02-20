import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.imageio.ImageIO;

class ImageEncodeDecode {
  private final int MIN_KEY_LENGTH = 1;
  private final int MAX_KEY_LENGTH = 25;
  private String imageFilename;
  private BufferedImage image;
  private String messageFilename;
  private byte[] message;

  private String getFormatType (File file) throws IOException {
    String formatType = file.getName().substring(file.getName().lastIndexOf('.') + 1);
    if (formatType.equalsIgnoreCase("bmp") || formatType.equalsIgnoreCase("png")) {
      return formatType;
    }
    else {
      throw new IOException();
    }
  }

  void loadImage(File inputFile) throws IOException {
    getFormatType(inputFile);
    image = ImageIO.read(inputFile);
    imageFilename = inputFile.getName();
  }

  void saveImage(File outputFile) throws IOException {
    ImageIO.write(image, getFormatType(outputFile), outputFile);
  }

  String getImageFilename() {
    return imageFilename;
  }

  BufferedImage getImage() {
    return image;
  }

  boolean isImageLoaded() {
    return image != null && imageFilename != null;
  }

  void loadMessage(File inputFile) throws IOException {
    message = Files.readAllBytes(inputFile.toPath());
    messageFilename = inputFile.getName();
  }

  void saveMessage(File outputFile) throws IOException {
    Files.write(outputFile.toPath(), message);
  }

  String getMessageFilename() {
    return messageFilename;
  }

  boolean isMessageLoaded() {
    return message != null && messageFilename != null;
  }

  private void checkKeyLength(String key) {
    if (key.length() < MIN_KEY_LENGTH) {
      throw new IllegalArgumentException("Key must be at least " + MIN_KEY_LENGTH + " character(s) long");
    }
    else if (key.length() > MAX_KEY_LENGTH) {
      throw new IllegalArgumentException("Key must be at most " + MAX_KEY_LENGTH + " character(s) long");
    }
  }

  ImageEncodeDecode encodeImage(String key, float threshold, boolean encryptedMessage, boolean randomEncoding) throws Exception {
    checkKeyLength(key);

    BpcsEncoder bpcsEncoder;
    if (randomEncoding) {
      bpcsEncoder = new BpcsEncoder(image, threshold, key);
    }
    else {
      bpcsEncoder = new BpcsEncoder(image, threshold);
    }

    Message packedMessage = new Message(messageFilename, message, threshold);
    packedMessage.encodeMessage();
    if (encryptedMessage) {
      VigenereCipher cipher = new VigenereCipher(key);
      packedMessage = new Message(cipher.encrypt(packedMessage.getEncodedMessage()), threshold);
    }

    bpcsEncoder.encodeMessageInImage(packedMessage);

    ImageEncodeDecode encoded = new ImageEncodeDecode();
    encoded.image = bpcsEncoder.getImage();
    encoded.imageFilename = imageFilename;

    return encoded;
  }

  ImageEncodeDecode decodeImage(String key, float threshold, boolean encryptedMessage, boolean randomEncoding) {
    checkKeyLength(key);

    Message packedMessage = new Message(null, threshold);
    if (encryptedMessage) {
      VigenereCipher cipher = new VigenereCipher(key);
      packedMessage = new Message(cipher.decrypt(packedMessage.getEncodedMessage()), threshold);
    }
    packedMessage.decodeMessage();

    ImageEncodeDecode decoded = new ImageEncodeDecode();
    decoded.image = null;
    decoded.imageFilename = imageFilename;
    decoded.message = packedMessage.getMessage();
    decoded.messageFilename = packedMessage.toString();

    return decoded;
  }
}
