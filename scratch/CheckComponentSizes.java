import juanword.*;
import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;

public class CheckComponentSizes {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                JFrame frame = new JFrame();
                JuanWord game = new JuanWord();
                BattlePanel bp = new BattlePanel(game, DataManager.getInstance());
                frame.setContentPane(bp);
                frame.setSize(960, 540);
                frame.doLayout();
                frame.setVisible(true);
                
                Field dboxField = BattlePanel.class.getDeclaredField("dizonBox");
                dboxField.setAccessible(true);
                JPanel dbox = (JPanel) dboxField.get(bp);
                
                Point p = SwingUtilities.convertPoint(dbox, 0, 0, bp);
                System.out.println("dizonBox bounds in bp: x=" + p.x + ", y=" + p.y + ", w=" + dbox.getWidth() + ", h=" + dbox.getHeight());
                
                frame.dispose();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        System.exit(0);
    }
}
