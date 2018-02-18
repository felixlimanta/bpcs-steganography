import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.Before;
import org.junit.Test;

public class BpcsEncoderTest {

  private static final String path = "C:\\Users\\ASUS\\Downloads\\64166790_p0.jpg";
  BpcsEncoder bpcsEncoder;

  @Before
  public void setUp() throws Exception {
    BufferedImage image = ImageIO.read(new File(path));
    bpcsEncoder = new BpcsEncoder(image);
  }

  @Test
  public void calculateComplexity() throws Exception {
    int w = bpcsEncoder.getImage().getWidth();
    int h = bpcsEncoder.getImage().getHeight();
    int t = 2 * (h - 1) * (w - 1) + (h - 1) + (w - 1);

    for (int j = 0; j < bpcsEncoder.getNumOfChannels(); ++j) {
      System.out.printf("Channel %d\n", j);
      bpcsEncoder.setChannel(BpcsEncoder.RED + j);
      for (int i = 0; i < 8; ++i) {
        int c = bpcsEncoder.calculateComplexity(i);
        System.out.printf("\t%d: %d -> %.2f\n", i, c, ((float) c / t));
      }
      System.out.println();
    }
  }

  @Test
  public void checkNoOfChannels() throws IOException {
    String path1 = "C:\\Users\\ASUS\\Downloads\\64166790_p0 - Copy.jpg";
    String path2 = "D:\\My Documents\\Work\\Kuliah\\ITB_1.png";
    String path3 = "D:\\My Documents\\Work\\Kuliah\\ITB_2a.png";

    System.out.printf("Channels: %d\n", bpcsEncoder.getImage().getRaster().getNumBands());
    System.out.printf("Channels: %d\n", ImageIO.read(new File(path1)).getRaster().getNumBands());
    System.out.printf("Channels: %d\n", ImageIO.read(new File(path2)).getRaster().getNumBands());
    System.out.printf("Channels: %d\n", ImageIO.read(new File(path3)).getRaster().getNumBands());
  }
}