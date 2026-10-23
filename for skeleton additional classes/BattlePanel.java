import javax.swing.*;
import java.awt.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Random;

/** Battle screen (placeholder gameplay): letter tiles, word box, health bars, enemy. */
public class BattlePanel extends JPanel {
    private static final String POOL = "AAAAAAIIIIIUUUUBDGGHKKLLLMNNNPRSSTTWY";

    private final JuanWordApp app;
    private final Random rnd = new Random();

    private final JLabel header = JuanWordApp.label("", 24, JuanWordApp.GOLD);
    private final JLabel enemyName = JuanWordApp.label("", 20, Color.WHITE);
    private final JProgressBar playerHp = new JProgressBar();
    private final JProgressBar enemyHp = new JProgressBar();
    private final SpritePanel playerSprite = new SpritePanel("JUAN\n(main_character_idle1.png)", new Color(0x2E6F95));
    private final SpritePanel enemySprite = new SpritePanel("ENEMY\n(enemy.png)", new Color(0x8B2E2E));
    private final JPanel tiles = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
    private final JTextField word = new JTextField();
    private final JLabel message = JuanWordApp.label("Form a Bisaya word using the tiles.", 16, Color.WHITE);
    private final Deque<JButton> used = new ArrayDeque<>();

    private int enemyMax, enemyCur, enemyAtk, playerCur;

    public BattlePanel(JuanWordApp app) {
        this.app = app;
        setLayout(new BorderLayout(8, 8));
        setBackground(JuanWordApp.BG);
        setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        // Top: title + menu button
        JButton menuBtn = JuanWordApp.btn("Main Menu");
        menuBtn.setFont(new Font("Serif", Font.BOLD, 14));
        menuBtn.addActionListener(e -> app.show(JuanWordApp.MENU));
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(header, BorderLayout.CENTER);
        top.add(menuBtn, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // Center: player (left) vs enemy (right)
        JPanel arena = new JPanel(new GridLayout(1, 2, 20, 0));
        arena.setBackground(new Color(0x14281E));
        arena.setBorder(BorderFactory.createLineBorder(JuanWordApp.GOLD, 2));
        arena.add(side(JuanWordApp.label("JUAN", 20, Color.WHITE), playerSprite, playerHp, new Color(0x3CB371)));
        arena.add(side(enemyName, enemySprite, enemyHp, new Color(0xC0392B)));
        add(arena, BorderLayout.CENTER);

        // Bottom: tiles, word box, buttons, message
        word.setEditable(false);
        word.setHorizontalAlignment(JTextField.CENTER);
        word.setFont(new Font("Serif", Font.BOLD, 28));

        JButton submit = JuanWordApp.btn("Attack!");
        JButton back = JuanWordApp.btn("Backspace");
        JButton clear = JuanWordApp.btn("Clear");
        JButton shuffle = JuanWordApp.btn("New Tiles");
        submit.addActionListener(e -> submit());
        back.addActionListener(e -> backspace());
        clear.addActionListener(e -> clearWord());
        shuffle.addActionListener(e -> dealTiles());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 4));
        buttons.setOpaque(false);
        buttons.add(submit); buttons.add(back); buttons.add(clear); buttons.add(shuffle);

        tiles.setOpaque(false);
        JPanel bottom = new JPanel(new GridLayout(4, 1, 0, 4));
        bottom.setOpaque(false);
        bottom.add(tiles);
        bottom.add(word);
        bottom.add(buttons);
        bottom.add(message);
        bottom.setPreferredSize(new Dimension(100, 230));
        add(bottom, BorderLayout.SOUTH);
    }

    private JPanel side(JLabel name, SpritePanel sprite, JProgressBar bar, Color color) {
        bar.setStringPainted(true);
        bar.setForeground(color);
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        p.add(name, BorderLayout.NORTH);
        p.add(sprite, BorderLayout.CENTER);
        p.add(bar, BorderLayout.SOUTH);
        return p;
    }

    /** Sets up a new battle from the player's current stage and round. */
    public void start() {
        Player p = app.player;
        int s = p.currentStage, r = p.currentRound;
        boolean boss = r > JuanWordApp.STAGE_ROUNDS[s - 1];
        String name = boss ? JuanWordApp.BOSSES[s - 1] : JuanWordApp.ENEMIES[s - 1][(r - 1) % 3];

        enemyMax = boss ? 120 + (s - 1) * 80 : 30 + (s - 1) * 25 + r * 3;
        enemyCur = enemyMax;
        enemyAtk = boss ? 10 + (s - 1) * 5 : 4 + (s - 1) * 3;
        playerCur = p.maxHealth;

        header.setText(boss ? "BOSS BATTLE - " + name + " (Stage " + s + ")"
                : "Stage " + s + ": " + JuanWordApp.STAGE_NAMES[s - 1] + " - Round " + r + "/" + JuanWordApp.STAGE_ROUNDS[s - 1]);
        enemyName.setText(boss ? "BOSS: " + name.toUpperCase() : name);
        enemySprite.setLabel((boss ? "BOSS\n" : "ENEMY\n") + name + "\n(enemy.png)");
        updateBars();
        message.setText("Form a Bisaya word using the tiles.");
        dealTiles();
    }

    private void updateBars() {
        enemyHp.setMaximum(enemyMax);
        enemyHp.setValue(Math.max(0, enemyCur));
        enemyHp.setString("Enemy HP " + Math.max(0, enemyCur) + " / " + enemyMax);
        playerHp.setMaximum(app.player.maxHealth);
        playerHp.setValue(Math.max(0, playerCur));
        playerHp.setString("Juan HP " + Math.max(0, playerCur) + " / " + app.player.maxHealth);
    }

    private void dealTiles() {
        tiles.removeAll();
        used.clear();
        word.setText("");
        for (int i = 0; i < 12; i++) {
            JButton b = JuanWordApp.btn(String.valueOf(POOL.charAt(rnd.nextInt(POOL.length()))));
            b.setPreferredSize(new Dimension(54, 54));
            b.addActionListener(e -> {
                word.setText(word.getText() + b.getText());
                b.setEnabled(false);
                used.push(b);
            });
            tiles.add(b);
        }
        tiles.revalidate();
        tiles.repaint();
    }

    private void backspace() {
        if (used.isEmpty()) return;
        used.pop().setEnabled(true);
        String t = word.getText();
        word.setText(t.substring(0, t.length() - 1));
    }

    private void clearWord() {
        while (!used.isEmpty()) used.pop().setEnabled(true);
        word.setText("");
    }

    private void submit() {
        String w = word.getText();
        if (w.length() < 3) {
            message.setText("Word too short! Use at least 3 letters.");
            return;
        }
        // PLACEHOLDER: real version will check w against the Bisaya dictionary CSV.
        int damage = w.length() * 5 + (w.length() >= 6 ? 10 : 0);
        enemyCur -= damage;
        app.player.score += damage;
        updateBars();

        if (enemyCur <= 0) {
            app.onVictory();
            return;
        }
        playerCur -= enemyAtk;
        updateBars();
        if (playerCur <= 0) {
            app.onDefeat();
            return;
        }
        message.setText("\"" + w + "\" dealt " + damage + " damage! The enemy hits back for " + enemyAtk + ".");
        dealTiles();
    }

    /** Draws a colored box with text, standing in for sprite images. */
    static class SpritePanel extends JPanel {
        private String label;
        private final Color color;

        SpritePanel(String label, Color color) {
            this.label = label;
            this.color = color;
            setOpaque(false);
        }

        void setLabel(String l) { label = l; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int w = getWidth() * 2 / 3, h = getHeight() * 4 / 5;
            int x = (getWidth() - w) / 2, y = (getHeight() - h) / 2;
            g.setColor(color);
            g.fillRoundRect(x, y, w, h, 30, 30);
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            String[] lines = label.split("\n");
            int ly = y + h / 2 - (lines.length * 9);
            for (String ln : lines) {
                int tw = g.getFontMetrics().stringWidth(ln);
                g.drawString(ln, x + (w - tw) / 2, ly += 18);
            }
        }
    }
}
