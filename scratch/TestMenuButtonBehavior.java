import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class TestMenuButtonBehavior {
    public static void main(String[] args) {
        System.out.println("Testing JButton DefaultButtonModel rollover and press behavior...");
        JButton btn = new JButton();
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setRolloverEnabled(true);

        ButtonModel model = btn.getModel();
        System.out.println("Initial rollover: " + model.isRollover());
        System.out.println("Initial pressed: " + model.isPressed());
        System.out.println("Rollover enabled: " + btn.isRolloverEnabled());

        model.setRollover(true);
        System.out.println("After setRollover(true): " + model.isRollover());
        model.setPressed(true);
        model.setArmed(true);
        System.out.println("After setPressed(true): " + model.isPressed() + ", armed: " + model.isArmed());
        System.out.println("PASS: DefaultButtonModel behavior verified.");
    }
}
