package stima;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class ImageEncodeDecode {
  private String imageFilename;
  private BufferedImage image;
  private String messageFilename;
  private byte[] message;

  void loadImage(File inputFile) throws IOException {
    image = ImageIO.read(inputFile);
    imageFilename = inputFile.getName();
  }

  void saveImage(File outputFile) throws IOException {
    ImageIO.write(image, outputFile.getName().substring(outputFile.getName().lastIndexOf('.') + 1), outputFile);
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

  ImageEncodeDecode encodeImage(String stegoKey) {
    return this;
  }

  ImageEncodeDecode decodeImage(String stegoKey) {
    return this;
  }
}
