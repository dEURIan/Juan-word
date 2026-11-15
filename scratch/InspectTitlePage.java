import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class InspectTitlePage {
    public static void main(String[] args) throws Exception {
        BufferedImage titlePage = ImageIO.read(new File("TITLE_PAGE.png"));
        System.out.println("TITLE_PAGE size: " + titlePage.getWidth() + "x" + titlePage.getHeight());
    }
}
