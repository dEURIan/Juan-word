import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import juanword.*;

public class CheckBottomPanelHeight {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                JuanWord app = new JuanWord();
                Player p = new Player("P1", "Juan");
                BattlePanel bp = new BattlePanel(app, DataManager.getInstance());
                bp.setupBattle(p, "Blue Building", 1, "Sayon");

                JFrame f = new JFrame();
                f.setSize(960, 540);
                f.setContentPane(bp);
                f.pack();
                f.setSize(960, 540);
                f.validate();

                Field bcpField = BattlePanel.class.getDeclaredField("battleContentPanel");
                bcpField.setAccessible(true);
                JPanel bcp = (JPanel) bcpField.get(bp);
                System.out.println("battleContentPanel size: " + bcp.getSize());
                for (Component c : bcp.getComponents()) {
                    System.out.println("  " + c.getClass().getSimpleName() + " bounds=" + c.getBounds() + " pref=" + c.getPreferredSize());
                }

                f.dispose();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        System.exit(0);
    }
}
