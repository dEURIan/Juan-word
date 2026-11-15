import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class TestClipBypass {
    public static void main(String[] args) throws Exception {
        JPanel parent = new JPanel(null);
        parent.setSize(400, 200);
        parent.setBackground(Color.BLACK);
        
        JPanel child = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                // Test 1: with default clip
                // Test 2: with setClip(null)
                g2.setClip(null);
                g2.setColor(Color.RED);
                // Child bounds are 50x50 at (100, 50). Try drawing outside: at x=80 (which is 180 on parent)
                g2.fillRect(0, 0, 150, 50);
                g2.dispose();
            }
        };
        child.setBounds(100, 50, 50, 50);
        parent.add(child);
        
        BufferedImage img = new BufferedImage(400, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        parent.paint(g);
        g.dispose();
        
        // Check pixel at (100 + 100 = 200, 75). If RED, setClip(null) worked!
        int rgb = img.getRGB(200, 75);
        boolean isRed = ((rgb >> 16) & 0xFF) > 200 && ((rgb >> 8) & 0xFF) < 50;
        System.out.println("Pixel at (200, 75) isRed: " + isRed);
    }
}
