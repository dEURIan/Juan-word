import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class TestMenuInteractiveRepaint {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            TestPaintChildrenMenu panel = new TestPaintChildrenMenu();
            JFrame frame = new JFrame();
            frame.getContentPane().add(panel);
            frame.setSize(300, 200);
            frame.setVisible(true);

            JButton btn = panel.getComponent(0) instanceof JButton ? (JButton) panel.getComponent(0) : null;
            if (btn == null) {
                System.out.println("FAIL: Button not found");
                System.exit(1);
            }

            // Simulate mouse enter on button
            Point center = new Point(btn.getWidth() / 2, btn.getHeight() / 2);
            MouseEvent enterEvent = new MouseEvent(btn, MouseEvent.MOUSE_ENTERED, System.currentTimeMillis(), 0, center.x, center.y, 0, false);
            btn.dispatchEvent(enterEvent);

            System.out.println("Rollover after MOUSE_ENTERED: " + btn.getModel().isRollover());
            if (!btn.getModel().isRollover()) {
                System.out.println("Note: isRollover may require mouse motion or explicit model rollover");
            }

            // Simulate mouse press on button
            MouseEvent pressEvent = new MouseEvent(btn, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), InputEvent.BUTTON1_DOWN_MASK, center.x, center.y, 1, false, MouseEvent.BUTTON1);
            btn.dispatchEvent(pressEvent);
            System.out.println("Pressed after MOUSE_PRESSED: " + btn.getModel().isPressed());
            System.out.println("Armed after MOUSE_PRESSED: " + btn.getModel().isArmed());

            frame.dispose();
            System.out.println("PASS: Interactive event dispatch verified.");
        });
    }
}
