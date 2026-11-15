package scratch;

import java.awt.Component;
import java.awt.Container;
import javax.swing.JFrame;
import juanword.BattlePanel;
import juanword.DataManager;
import juanword.JuanWord;
import juanword.Player;

public class CheckBattleLayout {
    public static void main(String[] args) throws Exception {
        JuanWord app = new JuanWord();
        Player juan = new Player("P_ANIM", "JuanTester");
        BattlePanel bp = new BattlePanel(app, DataManager.getInstance());
        bp.setupBattle(juan, "San Pedro Cathedral", 1, "Sakto");
        JFrame f = new JFrame();
        f.setContentPane(bp);
        f.setSize(960, 600);
        f.doLayout();
        bp.doLayout();
        f.validate();

        System.out.println("BattlePanel size: " + bp.getSize());
        dumpComponents(bp, 0);
        f.dispose();
        System.exit(0);
    }

    private static void dumpComponents(Component c, int indent) {
        String name = c.getClass().getSimpleName();
        if (name.isEmpty()) name = c.getClass().getName();
        if (name.contains("JuanAvatarPanel") || name.contains("juanSide") || name.contains("arenaCard") || name.contains("Arena") || name.contains("DizonBox") || name.contains("EnemyBox")) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < indent; i++) sb.append("  ");
            sb.append(name).append(" size=").append(c.getWidth()).append("x").append(c.getHeight())
              .append(" pref=").append(c.getPreferredSize().width).append("x").append(c.getPreferredSize().height);
            System.out.println(sb.toString());
        }
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                dumpComponents(child, indent + 1);
            }
        }
    }
}
