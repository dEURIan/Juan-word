import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class CompareBanners {
    public static void main(String[] args) throws Exception {
        BufferedImage bg = ImageIO.read(new File("scratch/test_map_composite_match.png"));
        int w = 960;
        int h = 540;
        
        String[][] data = {
            {"BLUE BUILDING", "EASY", "#6EE682"},
            {"DAVAO MUSEUM", "MEDIUM", "#F5AF4B"},
            {"SAN PEDRO CATHEDRAL", "HARD", "#FF785A"},
            {"MT. APO", "EXTREME", "#B978FF"}
        };
        
        // 1. Single Line Layout with bullet: "BLUE BUILDING  •  EASY"
        for (int i = 0; i < 4; i++) {
            BufferedImage canvas = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = canvas.createGraphics();
            g2.drawImage(bg, 0, 0, null);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            int bw = 380;
            int bh = 38;
            int bx = (w - bw) / 2;
            int by = 66;
            Color col = Color.decode(data[i][2]);
            
            // Outer glow / shadow
            g2.setColor(new Color(15, 20, 36, 235));
            g2.fillRoundRect(bx, by, bw, bh, 12, 12);
            g2.setColor(col);
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawRoundRect(bx, by, bw, bh, 12, 12);
            
            // Text
            g2.setFont(new Font("Arial", Font.BOLD, 15));
            String name = data[i][0];
            String diff = data[i][1];
            String full = name + "   •   " + diff;
            int tw = g2.getFontMetrics().stringWidth(full);
            
            // Draw name in crisp white/cream, bullet and diff in themed color
            int startX = (w - tw) / 2;
            int nameW = g2.getFontMetrics().stringWidth(name);
            int sepW = g2.getFontMetrics().stringWidth("   •   ");
            
            g2.setColor(new Color(255, 248, 233));
            g2.drawString(name, startX, by + 24);
            g2.setColor(new Color(160, 175, 205));
            g2.drawString("   •   ", startX + nameW, by + 24);
            g2.setColor(col);
            g2.drawString(diff, startX + nameW + sepW, by + 24);
            
            g2.dispose();
            ImageIO.write(canvas, "png", new File("scratch/single_line_banner_" + i + ".png"));
        }
        
        // 2. Two Line Layout: Name on top, Difficulty on bottom
        for (int i = 0; i < 4; i++) {
            BufferedImage canvas = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = canvas.createGraphics();
            g2.drawImage(bg, 0, 0, null);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            int bw = 340;
            int bh = 48;
            int bx = (w - bw) / 2;
            int by = 66;
            Color col = Color.decode(data[i][2]);
            
            g2.setColor(new Color(15, 20, 36, 235));
            g2.fillRoundRect(bx, by, bw, bh, 14, 14);
            g2.setColor(col);
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawRoundRect(bx, by, bw, bh, 14, 14);
            
            String name = data[i][0];
            String diff = data[i][1];
            
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            g2.setColor(new Color(255, 248, 233));
            int nw = g2.getFontMetrics().stringWidth(name);
            g2.drawString(name, (w - nw) / 2, by + 21);
            
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.setColor(col);
            int dw = g2.getFontMetrics().stringWidth(diff);
            g2.drawString(diff, (w - dw) / 2, by + 39);
            
            g2.dispose();
            ImageIO.write(canvas, "png", new File("scratch/two_line_banner_" + i + ".png"));
        }
        
        System.out.println("Rendered comparison banners.");
    }
}
