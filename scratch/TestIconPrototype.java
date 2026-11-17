import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class TestIconPrototype extends JPanel implements Icon {
    private final JButton testBtn;
    private final BufferedImage testSprite;

    public TestIconPrototype() {
        setLayout(null);
        testSprite = new BufferedImage(196, 48, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = testSprite.createGraphics();
        g.setColor(Color.ORANGE);
        g.fillRoundRect(0, 0, 196, 48, 16, 16);
        g.setColor(Color.WHITE);
        g.drawString("TEST BUTTON", 50, 28);
        g.dispose();

        testBtn = new JButton();
        testBtn.setIcon(this);
        testBtn.setOpaque(false);
        testBtn.setContentAreaFilled(false);
        testBtn.setBorderPainted(false);
        testBtn.setFocusPainted(false);
        testBtn.setRolloverEnabled(true);
        testBtn.setMargin(new Insets(0, 0, 0, 0));
        testBtn.setBounds(50, 50, 200, 50);
        add(testBtn);
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        if (!(c instanceof JButton)) return;
        JButton btn = (JButton) c;
        ButtonModel model = btn.getModel();
        int w = btn.getWidth();
        int h = btn.getHeight();
        System.out.println("x=" + x + ", y=" + y); int drawY = (model.isPressed() && model.isArmed() && btn.isEnabled()) ? 2 : 0;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (!btn.isEnabled()) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.38f));
            g2.drawImage(testSprite, 0, drawY, w, h, null);
        } else if (model.isRollover()) {
            g2.drawImage(testSprite, 0, drawY, w, h, null);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.14f));
            g2.setColor(Color.WHITE);
            int corner = Math.max(12, (int) Math.round(24 * (h / 48.0)));
            g2.fillRoundRect(0, drawY, w, h, corner, corner);
        } else {
            g2.drawImage(testSprite, 0, drawY, w, h, null);
        }
        g2.dispose();
    }

    @Override
    public int getIconWidth() { return 196; }

    @Override
    public int getIconHeight() { return 48; }

    public static void main(String[] args) {
        TestIconPrototype p = new TestIconPrototype();
        p.setSize(300, 150);
        p.doLayout();

        BufferedImage img = new BufferedImage(300, 150, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        p.paint(g2);
        g2.dispose();

        // Check if orange pixel was painted
        int rgb = img.getRGB(100, 75);
        Color c = new Color(rgb, true);
        System.out.println("Pixel color at (100, 75): " + c + " (alpha=" + c.getAlpha() + ")");
        if (c.getRed() > 200 && c.getGreen() > 100) {
            System.out.println("PASS: Icon rendered perfectly via paintIcon!");
        } else {
            System.out.println("FAIL: Unexpected pixel color");
            System.exit(1);
        }
    }
}
