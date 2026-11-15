import juanword.*;
import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;

public class MeasureDizonBox {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                JuanWord game = new JuanWord();
                BattlePanel bp = new BattlePanel(game, DataManager.getInstance());
                bp.setSize(960, 540);
                bp.doLayout();
                
                Field dboxField = BattlePanel.class.getDeclaredField("dizonBox");
                dboxField.setAccessible(true);
                JPanel dbox = (JPanel) dboxField.get(bp);
                
                Point screenPt = SwingUtilities.convertPoint(dbox, 0, 0, bp);
                System.out.println("DizonBox in BattlePanel coords: " + screenPt + ", size: " + dbox.getWidth() + "x" + dbox.getHeight());
                
                Field dsideField = BattlePanel.class.getDeclaredField("dizonHealthBar");
                dsideField.setAccessible(true);
                Component dhb = (Component) dsideField.get(bp);
                Point dhbPt = SwingUtilities.convertPoint(dhb, 0, 0, bp);
                System.out.println("DizonHealthBar in BattlePanel coords: " + dhbPt + ", size: " + dhb.getWidth() + "x" + dhb.getHeight());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        System.exit(0);
    }
}
