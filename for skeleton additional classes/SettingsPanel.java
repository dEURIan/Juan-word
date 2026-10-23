import javax.swing.*;
import java.awt.*;

/** Settings screen (placeholder controls; will be saved to settings.csv later). */
public class SettingsPanel extends JPanel {
    public SettingsPanel(JuanWordApp app) {
        setLayout(new GridBagLayout());
        setBackground(JuanWordApp.BG);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.insets = new Insets(10, 0, 10, 0);
        c.fill = GridBagConstraints.HORIZONTAL;

        add(JuanWordApp.label("SETTINGS", 40, JuanWordApp.GOLD), c);

        JCheckBox music = new JCheckBox("Music On", true);
        JCheckBox sfx = new JCheckBox("Sound Effects On", true);
        JSlider volume = new JSlider(0, 100, 70);
        for (JCheckBox b : new JCheckBox[]{music, sfx}) {
            b.setOpaque(false);
            b.setForeground(Color.WHITE);
            b.setFont(new Font("Serif", Font.BOLD, 20));
            add(b, c);
        }
        JLabel vl = JuanWordApp.label("Volume", 20, Color.WHITE);
        add(vl, c);
        volume.setOpaque(false);
        volume.setPaintLabels(true);
        volume.setMajorTickSpacing(25);
        add(volume, c);
        add(JuanWordApp.label("(Settings will be saved to settings.csv in the next phase)", 13, Color.GRAY), c);

        JButton back = JuanWordApp.btn("Back to Main Menu");
        back.setPreferredSize(new Dimension(260, 46));
        back.addActionListener(e -> app.show(JuanWordApp.MENU));
        add(back, c);
    }
}
