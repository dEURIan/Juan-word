import javax.swing.*;
import java.awt.*;

/** World map: shows the 4 stages and their status (cleared / current / locked). */
public class WorldMapPanel extends JPanel {
    private final JuanWordApp app;
    private final JLabel info = JuanWordApp.label("", 16, Color.WHITE);
    private final JPanel stagesPanel = new JPanel(new GridLayout(1, 4, 14, 0));

    public WorldMapPanel(JuanWordApp app) {
        this.app = app;
        setLayout(new BorderLayout(10, 10));
        setBackground(JuanWordApp.BG);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel top = new JPanel(new GridLayout(2, 1));
        top.setOpaque(false);
        top.add(JuanWordApp.label("WORLD MAP - Davao Region", 32, JuanWordApp.GOLD));
        top.add(info);
        add(top, BorderLayout.NORTH);

        stagesPanel.setOpaque(false);
        stagesPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));
        add(stagesPanel, BorderLayout.CENTER);

        JButton back = JuanWordApp.btn("Main Menu");
        back.addActionListener(e -> app.show(JuanWordApp.MENU));
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.setOpaque(false);
        bottom.add(back);
        add(bottom, BorderLayout.SOUTH);
    }

    public void refresh() {
        Player p = app.player;
        info.setText(p.playerName + "  |  Stage " + p.currentStage + ", Round " + p.currentRound
                + "  |  Score " + p.score + "  |  XP " + p.experience + "  |  Coins " + p.coins);
        stagesPanel.removeAll();
        for (int i = 0; i < 4; i++) {
            int stage = i + 1;
            JPanel card = new JPanel(new BorderLayout(6, 6));
            card.setBackground(JuanWordApp.PANEL);
            card.setBorder(BorderFactory.createLineBorder(JuanWordApp.GOLD, 2));

            JLabel pic = JuanWordApp.label("<html><center>[location art]<br>" + JuanWordApp.STAGE_PLACES[i] + "</center></html>", 14, Color.LIGHT_GRAY);
            JLabel name = JuanWordApp.label("<html><center>Stage " + stage + "<br>" + JuanWordApp.STAGE_NAMES[i]
                    + "<br><small>" + JuanWordApp.STAGE_DIFFICULTY[i] + " | Boss: " + JuanWordApp.BOSSES[i] + "</small></center></html>", 16, JuanWordApp.GOLD);
            JButton action = JuanWordApp.btn(stage < p.currentStage ? "Cleared" : stage == p.currentStage ? "Enter" : "Locked");
            action.setEnabled(stage == p.currentStage);
            action.addActionListener(e -> app.startBattle());

            card.add(name, BorderLayout.NORTH);
            card.add(pic, BorderLayout.CENTER);
            card.add(action, BorderLayout.SOUTH);
            stagesPanel.add(card);
        }
        stagesPanel.revalidate();
        stagesPanel.repaint();
    }
}
