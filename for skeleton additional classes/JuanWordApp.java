import javax.swing.*;
import java.awt.*;

/** Main window. Uses CardLayout to switch between all game screens. */
public class JuanWordApp extends JFrame {

    // Screen names (CardLayout keys)
    public static final String MENU = "menu", MAP = "map", BATTLE = "battle",
            RESULT = "result", BESTIARY = "bestiary", SETTINGS = "settings";

    // Game content (matches stages.csv / enemies.csv)
    public static final String[] STAGE_NAMES = {"The Beginning", "The Trial", "The Darkness", "The Final Trial"};
    public static final String[] STAGE_PLACES = {"Forest Trail", "Riverside Community", "Mountain Path", "Abandoned Settlement"};
    public static final String[] STAGE_DIFFICULTY = {"Easy", "Average", "Difficult", "Very Difficult"};
    public static final int[] STAGE_ROUNDS = {15, 10, 10, 12};
    public static final String[] BOSSES = {"Tikbalang", "Kapre", "Manananggal", "Aswang"};
    public static final String[][] ENEMIES = {
        {"Duwende", "Sigbin", "Tiyanak"},
        {"Wakwak", "Santelmo", "Sigbin"},
        {"Mangkukulam", "Berbalang", "Wakwak"},
        {"Aswang Foot Soldier", "Engkanto", "Berbalang"}};

    // Theme colors
    public static final Color BG = new Color(0x1B1B2F);
    public static final Color PANEL = new Color(0x2B2545);
    public static final Color GOLD = new Color(0xF0C040);

    public final DataManager dm = new DataManager();
    public Player player;                       // current player (null until New Game / Continue)

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private final MainMenuPanel menu = new MainMenuPanel(this);
    private final WorldMapPanel map = new WorldMapPanel(this);
    private final BattlePanel battle = new BattlePanel(this);
    private final ResultPanel result = new ResultPanel(this);

    public JuanWordApp() {
        super("JUAN Word");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        root.add(menu, MENU);
        root.add(map, MAP);
        root.add(battle, BATTLE);
        root.add(result, RESULT);
        root.add(new BestiaryPanel(this), BESTIARY);
        root.add(new SettingsPanel(this), SETTINGS);
        add(root);
        setSize(980, 660);
        setMinimumSize(new Dimension(900, 620));
        setLocationRelativeTo(null);
        show(MENU);
    }

    /** Switch screen. */
    public void show(String name) {
        if (name.equals(MENU)) menu.refresh();
        if (name.equals(MAP)) map.refresh();
        cards.show(root, name);
    }

    public void newGame() {
        String name = JOptionPane.showInputDialog(this, "Enter your name:", "New Game", JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.trim().isEmpty()) return;
        player = new Player(dm.nextPlayerId(), name.trim());
        dm.savePlayer(player);
        show(MAP);
    }

    public void continueGame() {
        Player p = dm.loadLatestPlayer();
        if (p == null) return;
        player = p;
        show(MAP);
    }

    public void startBattle() {
        battle.start();
        show(BATTLE);
    }

    /** Called by BattlePanel when the enemy reaches 0 HP. */
    public void onVictory() {
        boolean wasBoss = player.currentRound > STAGE_ROUNDS[player.currentStage - 1];
        player.experience += wasBoss ? 50 : 10;
        player.coins += wasBoss ? 25 : 5;
        String detail;
        boolean canNext = true;
        if (wasBoss) {
            if (player.currentStage < 4) {
                player.currentStage++;
                player.currentRound = 1;
                detail = "Boss defeated! Stage " + player.currentStage + " unlocked. +50 XP, +25 coins.";
            } else {
                detail = "You defeated the final boss. The adventure is complete!";
                canNext = false;
                dm.addHighScore(player.playerName, player.score, 4);
            }
        } else {
            player.currentRound++;
            detail = "Round cleared! +10 XP, +5 coins. Progress saved.";
        }
        dm.savePlayer(player);
        result.setup(true, "VICTORY!", detail, canNext);
        show(RESULT);
    }

    /** Called by BattlePanel when the player reaches 0 HP. */
    public void onDefeat() {
        result.setup(false, "DEFEATED", "Juan has fallen. Try again or return to the main menu.", true);
        show(RESULT);
    }

    // ---------- shared styling helpers ----------
    public static JButton btn(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Serif", Font.BOLD, 18));
        b.setBackground(new Color(0x3A2E5A));
        b.setForeground(GOLD);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorder(BorderFactory.createLineBorder(GOLD, 2));
        return b;
    }

    public static JLabel label(String text, int size, Color color) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Serif", Font.BOLD, size));
        l.setForeground(color);
        return l;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) { }
        SwingUtilities.invokeLater(() -> new JuanWordApp().setVisible(true));
    }
}
