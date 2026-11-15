import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class TestScreenBanners {
    public static void main(String[] args) throws Exception {
        BufferedImage base = ImageIO.read(new File("scratch/current_base_screen.png"));
        int w = 960;
        int h = 540;
        
        // 1. Render Screen with Option 1: Two-line Banner
        // Line 1: DAVAO MUSEUM
        // Line 2: MEDIUM
        BufferedImage img1 = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g1 = img1.createGraphics();
        g1.drawImage(base, 0, 0, null);
        g1.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g1.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        int bw1 = 280;
        int bh1 = 44;
        int bx1 = (w - bw1) / 2;
        int by1 = 66;
        Color medCol = new Color(245, 175, 75);
        
        g1.setColor(new Color(15, 20, 36, 235));
        g1.fillRoundRect(bx1, by1, bw1, bh1, 12, 12);
        g1.setColor(medCol);
        g1.setStroke(new BasicStroke(1.8f));
        g1.drawRoundRect(bx1, by1, bw1, bh1, 12, 12);
        
        g1.setFont(new Font("Arial", Font.BOLD, 14));
        g1.setColor(new Color(255, 248, 233));
        String title1 = "DAVAO MUSEUM";
        int t1w = g1.getFontMetrics().stringWidth(title1);
        g1.drawString(title1, (w - t1w) / 2, by1 + 19);
        
        g1.setFont(new Font("Arial", Font.BOLD, 12));
        g1.setColor(medCol);
        String sub1 = "MEDIUM";
        int s1w = g1.getFontMetrics().stringWidth(sub1);
        g1.drawString(sub1, (w - s1w) / 2, by1 + 36);
        g1.dispose();
        ImageIO.write(img1, "png", new File("scratch/screen_banner_two_lines.png"));
        
        // 2. Render Screen with Option 2: Single-line Banner
        // DAVAO MUSEUM  -  MEDIUM
        BufferedImage img2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img2.createGraphics();
        g2.drawImage(base, 0, 0, null);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        int bw2 = 320;
        int bh2 = 34;
        int bx2 = (w - bw2) / 2;
        int by2 = 66;
        
        g2.setColor(new Color(15, 20, 36, 235));
        g2.fillRoundRect(bx2, by2, bw2, bh2, 12, 12);
        g2.setColor(medCol);
        g2.setStroke(new BasicStroke(1.8f));
        g2.drawRoundRect(bx2, by2, bw2, bh2, 12, 12);
        
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        String full2 = "DAVAO MUSEUM   -   MEDIUM";
        int f2w = g2.getFontMetrics().stringWidth(full2);
        int sx2 = (w - f2w) / 2;
        int t2w = g2.getFontMetrics().stringWidth("DAVAO MUSEUM");
        int dashW = g2.getFontMetrics().stringWidth("   -   ");
        
        g2.setColor(new Color(255, 248, 233));
        g2.drawString("DAVAO MUSEUM", sx2, by2 + 22);
        g2.setColor(new Color(160, 175, 205));
        g2.drawString("   -   ", sx2 + t2w, by2 + 22);
        g2.setColor(medCol);
        g2.drawString("MEDIUM", sx2 + t2w + dashW, by2 + 22);
        g2.dispose();
        ImageIO.write(img2, "png", new File("scratch/screen_banner_single_line.png"));
        
        System.out.println("Rendered screen_banner_two_lines.png and screen_banner_single_line.png");
    }
}
