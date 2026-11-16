import javax.swing.*;
import java.awt.*;

/** Victory / Defeat screen. */
public class ResultPanel extends JPanel {
    private final JLabel title = JuanWordApp.label("", 56, JuanWordApp.GOLD);
    private final JLabel detail = JuanWordApp.label("", 20, Color.WHITE);
    private final JButton primary = JuanWordApp.btn("Next Level");

    public ResultPanel(JuanWordApp app) {
        setLayout(new GridBagLayout());
        setBackground(JuanWordApp.BG);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.insets = new Insets(10, 0, 10, 0);
        c.fill = GridBagConstraints.HORIZONTAL;

        JButton menu = JuanWordApp.btn("Return to Main Menu");
        JButton map = JuanWordApp.btn("World Map");
        primary.setPreferredSize(new Dimension(280, 46));
        menu.setPreferredSize(new Dimension(280, 46));
        map.setPreferredSize(new Dimension(280, 46));

        add(title, c);
        add(detail, c);
        c.insets = new Insets(8, 300, 8, 300);
        add(primary, c);
        add(map, c);
        add(menu, c);

        primary.addActionListener(e -> app.startBattle());
        map.addActionListener(e -> app.show(JuanWordApp.MAP));
        menu.addActionListener(e -> app.show(JuanWordApp.MENU));
    }

    public void setup(boolean win, String titleText, String detailText, boolean canContinue) {
        title.setText(titleText);
        title.setForeground(win ? JuanWordApp.GOLD : new Color(0xE05555));
        detail.setText(detailText);
        primary.setText(win ? "Next Level" : "Try Again");
        primary.setEnabled(canContinue);
    }
}
