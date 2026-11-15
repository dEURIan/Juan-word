import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class TestPerfectComposition {
    public static void main(String[] args) throws Exception {
        BufferedImage bg = ImageIO.read(new File("bg.png"));
        BufferedImage mtapo = ImageIO.read(new File("mtapo.png"));
        BufferedImage frame = ImageIO.read(new File("indigframe.png"));
        BufferedImage titleRaw = ImageIO.read(new File("Title.png"));
        BufferedImage startBtn = ImageIO.read(new File("startbtn.png"));
        BufferedImage continueBtn = ImageIO.read(new File("continuebtn.png"));
        BufferedImage settingBtn = ImageIO.read(new File("settingbtn.png"));
        BufferedImage exitBtn = ImageIO.read(new File("exitbtn.png"));

        // Crop title logo tightly
        BufferedImage title = titleRaw.getSubimage(386, 243, 1162, 585);

        int w = 960;
        int h = 540;

        BufferedImage screen = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = screen.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Sky background
        g2.drawImage(bg, 0, 0, w, h, null);

        // 2. Mt Apo (mountain and foothills)
        g2.drawImage(mtapo, 0, 0, w, h, null);

        // 3. Indigenous Frame
        g2.drawImage(frame, 0, 0, w, h, null);

        // 4. Title Logo (52% width, perfectly proportioned)
        int titleW = (int) (w * 0.52);
        int titleH = titleW * title.getHeight() / title.getWidth();
        int titleX = (w - titleW) / 2;
        int titleY = 28; // right inside the top frame border!
        g2.drawImage(title, titleX, titleY, titleW, titleH, null);

        // 5. Buttons (192x48)
        int btnW = 192;
        int btnH = 48;
        int btnX = (w - btnW) / 2;
        int gap = 6;
        int bottomMargin = 26; // just above bottom frame
        int totalBtnH = (btnH * 4) + (gap * 3);
        int startY = h - bottomMargin - totalBtnH;

        g2.drawImage(continueBtn, btnX, startY, btnW, btnH, null);
        g2.drawImage(startBtn, btnX, startY + (btnH + gap), btnW, btnH, null);
        g2.drawImage(settingBtn, btnX, startY + (btnH + gap) * 2, btnW, btnH, null);
        g2.drawImage(exitBtn, btnX, startY + (btnH + gap) * 3, btnW, btnH, null);

        g2.dispose();

        File out = new File("scratch/perfect_menu_composite.png");
        ImageIO.write(screen, "png", out);
        System.out.printf("Title drawn at: [%d, %d] size %dx%d%n", titleX, titleY, titleW, titleH);
        System.out.printf("Buttons drawn from Y=%d to %d%n", startY, startY + totalBtnH);
        System.out.println("Saved to " + out.getAbsolutePath());
    }
}
