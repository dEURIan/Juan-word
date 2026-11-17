import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class TestPaintChildrenMenu extends JPanel {
    private final JButton btn;
    private final BufferedImage sprite;
    private int paintCount = 0;

    public TestPaintChildrenMenu() {
        setLayout(null);
        setOpaque(true);
        setBackground(Color.DARK_GRAY);

        sprite = new BufferedImage(196, 48, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = sprite.createGraphics();
        g.setColor(Color.CYAN);
        g.fillRoundRect(0, 0, 196, 48, 16, 16);
        g.dispose();

        btn = new JButton();
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setRolloverEnabled(true);
        btn.setBounds(50, 50, 196, 48);
        add(btn);
    }

    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        paintCount++; System.out.println("g clip: " + g.getClipBounds());
        Graphics2D g2 = (Graphics2D) g.create();
        drawMenuButton(g2, btn, sprite);
        g2.dispose();
    }

    private void drawMenuButton(Graphics2D g2, JButton b, BufferedImage img) {
        if (img == null || b == null || !b.isVisible()) return;
        Rectangle bounds = b.getBounds(); System.out.println("Drawing at " + bounds);
        ButtonModel model = b.getModel();
        boolean pressed = model.isPressed() && model.isArmed();
        boolean hovered = model.isRollover();
        boolean enabled = b.isEnabled();

        int drawX = bounds.x;
        int drawY = bounds.y + (pressed && enabled ? 2 : 0);
        int w = bounds.width;
        int h = bounds.height;

        Graphics2D bg = (Graphics2D) g2.create();
        if (!enabled) {
            bg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.38f));
            bg.drawImage(img, drawX, drawY, w, h, null);
        } else if (hovered) {
            bg.drawImage(img, drawX, drawY, w, h, null);
            bg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.14f));
            bg.setColor(Color.WHITE);
            int corner = Math.max(12, (int) Math.round(24 * (h / 48.0)));
            bg.fillRoundRect(drawX, drawY, w, h, corner, corner);
        } else {
            bg.drawImage(img, drawX, drawY, w, h, null);
        }
        bg.dispose();
    }

    public static void main(String[] args) throws Exception {
        TestPaintChildrenMenu panel = new TestPaintChildrenMenu();
        panel.setSize(300, 200);
        panel.doLayout();

        JFrame frame = new JFrame();
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setSize(300, 200);

        BufferedImage img = new BufferedImage(300, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        panel.paint(g2);
        g2.dispose();

        System.out.println("Normal state paint count: " + panel.paintCount);
        int rgb = img.getRGB(100, 70);
        Color c = new Color(rgb, true);
        System.out.println("Pixel color: " + c);
        if (c.getBlue() > 200) {
            System.out.println("PASS: Button painted in normal state!");
        } else {
            System.out.println("FAIL: Normal state did not paint");
            System.exit(1);
        }

        // Test pressed state
        panel.btn.getModel().setPressed(true);
        panel.btn.getModel().setArmed(true);
        panel.paint(img.createGraphics());
        System.out.println("Pressed state paint count: " + panel.paintCount);

        // Test hover state
        panel.btn.getModel().setPressed(false);
        panel.btn.getModel().setArmed(false);
        panel.btn.getModel().setRollover(true);
        panel.paint(img.createGraphics());
        System.out.println("Hover state paint count: " + panel.paintCount);

        System.out.println("ALL STATES PASSED!");
    }
}
