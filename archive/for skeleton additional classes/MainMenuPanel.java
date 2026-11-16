import javax.swing.*;
import java.awt.*;

/** Main menu: New Game, Continue, Bestiary, Settings, Exit. */
public class MainMenuPanel extends JPanel {
    private final JuanWordApp app;
    private final JButton continueBtn = JuanWordApp.btn("Continue");

    public MainMenuPanel(JuanWordApp app) {
        this.app = app;
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.insets = new Insets(6, 0, 6, 0);
        c.fill = GridBagConstraints.HORIZONTAL;

        add(JuanWordApp.label("JUAN WORD", 56, JuanWordApp.GOLD), c);
        add(JuanWordApp.label("A Bisaya Word Adventure", 20, Color.WHITE), c);
        c.insets = new Insets(8, 300, 8, 300);

        JButton newBtn = JuanWordApp.btn("New Game");
        JButton bestBtn = JuanWordApp.btn("Bestiary");
        JButton setBtn = JuanWordApp.btn("Settings");
        JButton exitBtn = JuanWordApp.btn("Exit");
        for (JButton b : new JButton[]{newBtn, continueBtn, bestBtn, setBtn, exitBtn}) {
            b.setPreferredSize(new Dimension(260, 46));
            add(b, c);
        }

        newBtn.addActionListener(e -> app.newGame());
        continueBtn.addActionListener(e -> app.continueGame());
        bestBtn.addActionListener(e -> app.show(JuanWordApp.BESTIARY));
        setBtn.addActionListener(e -> app.show(JuanWordApp.SETTINGS));
        exitBtn.addActionListener(e -> System.exit(0));
    }

    /** Continue is only enabled when a saved game exists. */
    public void refresh() {
        continueBtn.setEnabled(app.dm.loadLatestPlayer() != null);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setPaint(new GradientPaint(0, 0, new Color(0x0F2A1D), 0, getHeight(), new Color(0x1B1B2F)));
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(new Color(255, 255, 255, 40));
        g2.setFont(new Font("SansSerif", Font.ITALIC, 12));
        g2.drawString("[background artwork placeholder - Davao forest / Mt. Apo]", 12, getHeight() - 12);
    }
}
