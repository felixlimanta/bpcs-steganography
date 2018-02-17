import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

class ImageEncodeDecode {
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

  ImageEncodeDecode encodeImage(String key, double threshold, boolean encryptedMessage, boolean randomEncoding) {
    return this;
  }

  ImageEncodeDecode decodeImage(String key, double threshold, boolean encryptedMessage, boolean randomEncoding) {
    return this;
  }
}
