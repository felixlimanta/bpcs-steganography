import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.Before;
import org.junit.Test;

public class BpcsEncoderTest {

  private static final String path = "C:\\Users\\ASUS\\Downloads\\64166790_p0.jpg";
  BpcsEncoder bpcsEncoder;
  float threshold = (float) 0.3;

  @Before
  public void setUp() throws Exception {
    BufferedImage image = ImageIO.read(new File(path));
    bpcsEncoder = new BpcsEncoder(image, threshold * 112);
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