package stima;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class CoverImage {
  private BufferedImage bufferedImage;

  CoverImage(File file) throws IOException {
    bufferedImage = ImageIO.read(file);
  }

  CoverImage(BufferedImage bufferedImage) {
    this.bufferedImage = bufferedImage;
  }

  StegoImage encodeImage(EmbeddedMessage embeddedMessage, String stegoKey) {
    return new StegoImage(bufferedImage);
  }

  BufferedImage getBufferedImage() {
    return bufferedImage;
  }
}
