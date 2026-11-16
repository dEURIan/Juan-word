package juanword;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import juanword.data.DataManager;
import juanword.model.BattleSession;
import juanword.model.BattleSessionBuilder;
import juanword.model.Player;
import juanword.ui.BattlePanel;
import juanword.ui.ImageLoader;
import juanword.ui.MapSelectPanel;
import juanword.ui.StartPanel;

public final class JuanWord extends JFrame {
    public static final int WIDTH = 960;
    public static final int HEIGHT = 540;

    private final CardLayout cards;
    private final JPanel root;
    private final StartPanel startPanel;
    private final MapSelectPanel mapPanel;
    private final BattlePanel battlePanel;
    private Player activePlayer;

    public static void main(String[] args) {
        DataManager.getInstance();
        SwingUtilities.invokeLater(() -> {
            JuanWord frame = new JuanWord();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    public JuanWord() {
        super("JuanWord: Lexical Combat RPG");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setResizable(true);
        setMinimumSize(new Dimension(800, 500));
        setSize(WIDTH, HEIGHT);
        getContentPane().setPreferredSize(new Dimension(WIDTH, HEIGHT));
        pack();

        getRootPane().putClientProperty("apple.awt.fullScreenable", true);

        try {
            BufferedImage cursorImg = ImageLoader.load("Cursor.png");
            if (cursorImg != null && !ImageLoader.isPlaceholder(cursorImg)) {
                setCursor(Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(6, 6), "JuanWordCursor"));
            }
        } catch (Exception ignored) {}

        this.cards = new CardLayout();
        this.root = new JPanel(cards);

        this.startPanel = new StartPanel(this);
        this.mapPanel = new MapSelectPanel(this);
        this.battlePanel = new BattlePanel(this);

        root.add(startPanel, Screen.START.cardName());
        root.add(mapPanel, Screen.MAP_SELECT.cardName());
        root.add(battlePanel, Screen.BATTLE.cardName());

        setContentPane(root);
        installClosingHook();

        root.registerKeyboardAction(
            e -> toggleFullScreen(),
            javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F11, 0),
            javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        showScreen(Screen.START);
    }

    public boolean isFullScreen() {
        return (getExtendedState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH;
    }

    public void setFullScreen(boolean fs) {
        if (fs) {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        } else {
            setExtendedState(JFrame.NORMAL);
            setSize(WIDTH, HEIGHT);
            setLocationRelativeTo(null);
        }
    }

    public void toggleFullScreen() {
        setFullScreen(!isFullScreen());
    }

    public void showScreen(Screen screen) {
        if (screen == null) return;
        cards.show(root, screen.cardName());
        switch (screen) {
            case START -> {
                mapPanel.stop();
                startPanel.refreshProfiles();
            }
            case MAP_SELECT -> {
                mapPanel.refresh();
            }
            case BATTLE -> {
                mapPanel.stop();
            }
        }
    }

    public void startBattle(BattleSession session) {
        if (session != null) {
            battlePanel.begin(session);
            showScreen(Screen.BATTLE);
        }
    }

    public void resumeBattle(Player player) {
        if (player != null && player.hasActiveBattle()) {
            BattleSession session = BattleSessionBuilder.fromSavedPlayer(player);
            startBattle(session);
        } else {
            showScreen(Screen.MAP_SELECT);
        }
    }

    public void returnToMap() {
        showScreen(Screen.MAP_SELECT);
    }

    public Player getActivePlayer() {
        return activePlayer;
    }

    public void setActivePlayer(Player p) {
        this.activePlayer = p;
    }

    private void installClosingHook() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (battlePanel.isBattleActive()) {
                    battlePanel.saveMidBattle();
                }
                DataManager.getInstance().savePlayers();
                dispose();
                System.exit(0);
            }
        });
    }
}
