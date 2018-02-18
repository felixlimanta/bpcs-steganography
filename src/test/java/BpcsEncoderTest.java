import java.awt.image.BufferedImage;
import java.io.File;
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

    System.out.println("RED:");
    bpcsEncoder.setChannel(BpcsEncoder.Channel.RED);
    for (int i = 0; i < 8; ++i) {
      int c = bpcsEncoder.calculateComplexity(i);
      System.out.printf("\t%d: %d -> %.2f\n", i, c, ((float) c / t));
    }

    System.out.println("\nGREEN:");
    bpcsEncoder.setChannel(BpcsEncoder.Channel.GREEN);
    for (int i = 0; i < 8; ++i) {
      int c = bpcsEncoder.calculateComplexity(i);
      System.out.printf("\t%d: %d -> %.2f\n", i, c, ((float) c / t));
    }

    System.out.println("\nBLUE:");
    bpcsEncoder.setChannel(BpcsEncoder.Channel.BLUE);
    for (int i = 0; i < 8; ++i) {
      int c = bpcsEncoder.calculateComplexity(i);
      System.out.printf("\t%d: %d -> %.2f\n", i, c, ((float) c / t));
    }
  }
}