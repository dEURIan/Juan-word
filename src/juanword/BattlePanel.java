package juanword;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * San Pedro Cathedral Demo Battle: Juan vs. Dizon (The Box).
 * Replicates Bookworm gameplay with letter tiles positioned at the bottom of the screen,
 * a prominent Attack Button with dynamic damage feedback, Cebuano dictionary validation,
 * and the 4 Parametric Dials of Difficulty.
 */
public class BattlePanel extends JPanel {

    // --- DIFFICULTY DIAL ENUM ---
    public enum DifficultySetting {
        SAYON("Sayon", new Color(46, 125, 50), 50, 6, 1.0, 1.0, false, 0),
        SAKTO("Sakto", new Color(230, 108, 0), 70, 10, 1.5, 1.5, false, 12),
        LISOD("Lisod", new Color(198, 40, 40), 110, 15, 2.5, 2.5, true, 25);

        public final String label;
        public final Color badgeColor;
        public final int enemyHp;
        public final int enemyAtk;
        public final double rewardMultiplier;
        public final double scoreMultiplier;
        public final boolean hasTimer;
        public final int turnLimitOrSeconds;

        DifficultySetting(String label, Color badgeColor, int enemyHp, int enemyAtk,
                          double rewardMultiplier, double scoreMultiplier,
                          boolean hasTimer, int turnLimitOrSeconds) {
            this.label = label;
            this.badgeColor = badgeColor;
            this.enemyHp = enemyHp;
            this.enemyAtk = enemyAtk;
            this.rewardMultiplier = rewardMultiplier;
            this.scoreMultiplier = scoreMultiplier;
            this.hasTimer = hasTimer;
            this.turnLimitOrSeconds = turnLimitOrSeconds;
        }

        public static DifficultySetting fromString(String name) {
            if ("Lisod".equalsIgnoreCase(name)) return LISOD;
            if ("Sakto".equalsIgnoreCase(name)) return SAKTO;
            return SAYON;
        }

        /**
         * DIAL 1: Word length damage multiplier.
         * Harder difficulties make shorter letter words worth less!
         */
        public double getLengthMultiplier(int length) {
            switch (this) {
                case LISOD:
                    // Hard: Shorter words are heavily penalized!
                    if (length <= 2) return 0.20;
                    if (length == 3) return 0.35; // 3-letter words only 35% damage!
                    if (length == 4) return 0.85; // 4-letter words 85% damage
                    if (length == 5) return 1.50; // 5-letter words 150%
                    if (length == 6) return 2.20; // 6-letter words 220%
                    return 3.00;                 // 7+ letter words 300%!

                case SAKTO:
                    // Medium: Shorter words are moderately weakened
                    if (length <= 2) return 0.40;
                    if (length == 3) return 0.65; // 3-letter words 65% damage
                    if (length == 4) return 1.00; // Baseline
                    if (length == 5) return 1.45;
                    if (length == 6) return 1.90;
                    return 2.40;

                case SAYON:
                default:
                    // Easy: Friendly scaling, 3-letter words deal full 100% damage
                    if (length <= 2) return 0.80;
                    if (length == 3) return 1.00;
                    if (length == 4) return 1.20;
                    if (length == 5) return 1.45;
                    return 1.80;
            }
        }
    }

    private final JuanWord app;
    private final DataManager dm;
    private final Random rng = new Random();

    // Game state
    private Player currentPlayer;
    private String mapName = "San Pedro Cathedral";
    private int stageNum = 1;
    private DifficultySetting difficulty = DifficultySetting.SAKTO;

    private int juanMaxHp = 100;
    private int juanCurHp = 100;
    private int dizonMaxHp = 70;
    private int dizonCurHp = 70;
    private int dizonAtk = 10;
    private int turnsLeft = 12;
    private int timerSeconds = 25;
    private javax.swing.Timer turnTimer;

    // Letter pool & values
    private Map<Character, Integer> letterPoints = new HashMap<>();
    private List<Character> weightedPool = new ArrayList<>();

    // Tiles & selection stack
    private final LetterTileButton[] tiles = new LetterTileButton[16]; // 4x4 grid
    private final Deque<LetterTileButton> selectedTiles = new ArrayDeque<>();

    // UI components
    private JLabel locationTitleLabel;
    private JLabel difficultyBadgeLabel;
    private JLabel pacingInfoLabel;

    private JProgressBar juanHpBar;
    private JLabel juanHpText;
    private JLabel juanStatsLabel;

    private JProgressBar dizonHpBar;
    private JLabel dizonHpText;
    private DizonBoxPanel dizonBox;

    private JTextField wordDisplayField;
    private JLabel damagePreviewLabel;
    private JLabel battleLogLabel;

    private HeroAttackButton attackButton;
    private JButton backspaceButton;
    private JButton clearButton;
    private JButton shuffleButton;

    public BattlePanel(JuanWord app, DataManager dm) {
        this.app = app;
        this.dm = dm;
        loadLetterData();
        buildUI();
    }

    private void loadLetterData() {
        letterPoints = dm.loadLetterPoints();
        Map<Character, Integer> weights = dm.loadLetterWeights();
        weightedPool.clear();
        for (Map.Entry<Character, Integer> entry : weights.entrySet()) {
            char ch = entry.getKey();
            int w = entry.getValue();
            for (int i = 0; i < w; i++) {
                weightedPool.add(ch);
            }
        }
        if (weightedPool.isEmpty()) {
            String fallback = "AAAAAAIIIIIUUUUBDGGHKKLLLMNNNPRSSTTWY";
            for (char c : fallback.toCharArray()) {
                weightedPool.add(Character.toLowerCase(c));
            }
        }
    }

    private char getRandomLetter() {
        return weightedPool.get(rng.nextInt(weightedPool.size()));
    }

    // --- SETUP BATTLE FOR MAP & DIFFICULTY ---
    public void setupBattle(Player player, String map, int stage, String diffName) {
        this.currentPlayer = player;
        this.mapName = map;
        this.stageNum = stage;
        this.difficulty = DifficultySetting.fromString(diffName);

        // Dial 2: Enemy stats scaling
        this.dizonMaxHp = difficulty.enemyHp;
        this.dizonCurHp = dizonMaxHp;
        this.dizonAtk = difficulty.enemyAtk;

        // Player HP
        this.juanMaxHp = (player != null && player.maxHealth > 0) ? player.maxHealth : 100;
        if (player != null && player.currentHealth > 0) {
            this.juanCurHp = Math.min(player.currentHealth, juanMaxHp);
        } else {
            this.juanCurHp = juanMaxHp;
        }
        saveHealth();

        // Dial 3: Pacing / Timers
        if (turnTimer != null) turnTimer.stop();
        if (difficulty.hasTimer) {
            timerSeconds = difficulty.turnLimitOrSeconds; // 25s
            turnTimer = new javax.swing.Timer(1000, e -> onTimerTick());
            turnTimer.start();
        } else {
            turnsLeft = difficulty.turnLimitOrSeconds; // 12 turns for Sakto, 0 (infinite) for Sayon
        }

        updateHeaderInfo();
        updateCombatBars();
        updateJuanInfo();
        clearWordSelection();
        dealNewGrid();

        battleLogLabel.setText("Nagsugod na ang gubat! Pagpili ug mga letra gikan sa ubos para makatake.");
        if (dizonBox != null) dizonBox.setHurt(false);
    }

    private void buildUI() {
        setLayout(new BorderLayout(0, 6));
        setBackground(new Color(244, 246, 250));
        setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));

        // 1. TOP HEADER BAR
        JPanel headerBar = new JPanel(new BorderLayout(12, 0));
        headerBar.setOpaque(false);

        JButton backBtn = new JButton("← Balik sa Mapa");
        backBtn.setFont(new Font("Arial", Font.BOLD, 13));
        backBtn.setFocusPainted(false);
        if (app.customCursor != null) backBtn.setCursor(app.customCursor);
        backBtn.addActionListener(e -> exitBattle());

        JPanel centerHeader = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        centerHeader.setOpaque(false);

        locationTitleLabel = new JLabel("SAN PEDRO CATHEDRAL", SwingConstants.CENTER);
        locationTitleLabel.setFont(new Font("Arial", Font.BOLD, 17));
        locationTitleLabel.setForeground(new Color(30, 35, 50));

        difficultyBadgeLabel = new JLabel(" [ SAKTO ] ");
        difficultyBadgeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        difficultyBadgeLabel.setOpaque(true);
        difficultyBadgeLabel.setForeground(Color.WHITE);
        difficultyBadgeLabel.setBackground(DifficultySetting.SAKTO.badgeColor);
        difficultyBadgeLabel.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));

        centerHeader.add(locationTitleLabel);
        centerHeader.add(difficultyBadgeLabel);

        pacingInfoLabel = new JLabel("Turns Left: 12", SwingConstants.RIGHT);
        pacingInfoLabel.setFont(new Font("Arial", Font.BOLD, 13));
        pacingInfoLabel.setForeground(new Color(70, 75, 90));

        headerBar.add(backBtn, BorderLayout.WEST);
        headerBar.add(centerHeader, BorderLayout.CENTER);
        headerBar.add(pacingInfoLabel, BorderLayout.EAST);
        add(headerBar, BorderLayout.NORTH);

        // 2. CENTER: COMBAT ARENA (Juan vs Dizon + Battle Dialogue)
        JPanel arenaPanel = createArenaPanel();
        add(arenaPanel, BorderLayout.CENTER);

        // 3. SOUTH: BOTTOM GAMEPLAY PANEL (Word Tray + Attack Button + Letter Tiles)
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // --- UPPER COMBAT ARENA ---
    private JPanel createArenaPanel() {
        JPanel arenaCard = new JPanel(new BorderLayout(12, 0));
        arenaCard.setBackground(Color.WHITE);
        arenaCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 215, 225), 1, true),
            BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));

        // LEFT FIGHTER: JUAN
        JPanel juanSide = new JPanel(new BorderLayout(0, 4));
        juanSide.setOpaque(false);
        juanSide.setPreferredSize(new Dimension(170, 160));

        JLabel juanTitle = new JLabel("JUAN (Ikaw)", SwingConstants.CENTER);
        juanTitle.setFont(new Font("Arial", Font.BOLD, 14));
        juanTitle.setForeground(new Color(35, 40, 55));

        Image sprite = app.loadGameImage("Main Character Idle_1.png");
        JLabel juanAvatar = new JLabel();
        juanAvatar.setHorizontalAlignment(SwingConstants.CENTER);
        if (sprite != null) {
            juanAvatar.setIcon(new ImageIcon(sprite.getScaledInstance(80, 80, Image.SCALE_SMOOTH)));
        } else {
            juanAvatar.setText("👤 JUAN");
            juanAvatar.setFont(new Font("Arial", Font.BOLD, 20));
        }

        JPanel juanHpBox = new JPanel(new GridLayout(2, 1, 0, 2));
        juanHpBox.setOpaque(false);
        juanHpBar = new JProgressBar(0, 100);
        juanHpBar.setValue(100);
        juanHpBar.setForeground(new Color(46, 125, 50));
        juanHpBar.setPreferredSize(new Dimension(100, 14));
        juanHpText = new JLabel("HP: 100 / 100", SwingConstants.CENTER);
        juanHpText.setFont(new Font("Arial", Font.BOLD, 12));
        juanHpText.setForeground(new Color(46, 125, 50));
        juanHpBox.add(juanHpBar);
        juanHpBox.add(juanHpText);

        juanStatsLabel = new JLabel("Coins: 0 • Score: 0", SwingConstants.CENTER);
        juanStatsLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        juanStatsLabel.setForeground(new Color(110, 115, 130));

        JPanel juanBottom = new JPanel(new BorderLayout(0, 2));
        juanBottom.setOpaque(false);
        juanBottom.add(juanHpBox, BorderLayout.NORTH);
        juanBottom.add(juanStatsLabel, BorderLayout.SOUTH);

        juanSide.add(juanTitle, BorderLayout.NORTH);
        juanSide.add(juanAvatar, BorderLayout.CENTER);
        juanSide.add(juanBottom, BorderLayout.SOUTH);

        // CENTER: BATTLE ACTION LOG & VS BADGE
        JPanel centerSide = new JPanel(new BorderLayout(0, 6));
        centerSide.setOpaque(false);

        JLabel vsLabel = new JLabel("⚔️  V E R S U S  ⚔️", SwingConstants.CENTER);
        vsLabel.setFont(new Font("Arial", Font.BOLD, 13));
        vsLabel.setForeground(new Color(120, 125, 140));

        JPanel logCard = new JPanel(new BorderLayout());
        logCard.setBackground(new Color(32, 36, 48));
        logCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50, 56, 72), 1, true),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        battleLogLabel = new JLabel("<html><center>Andam na ang dula.<br>Pindota ang mga letra sa ubos aron makaporma ug Bisaya nga pulong!</center></html>", SwingConstants.CENTER);
        battleLogLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        battleLogLabel.setForeground(new Color(230, 235, 245));
        logCard.add(battleLogLabel, BorderLayout.CENTER);

        JLabel tipLabel = new JLabel("Lisod dial: 3-letter words deal reduced damage • 5+ letters earn huge multipliers!", SwingConstants.CENTER);
        tipLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        tipLabel.setForeground(new Color(130, 135, 150));

        centerSide.add(vsLabel, BorderLayout.NORTH);
        centerSide.add(logCard, BorderLayout.CENTER);
        centerSide.add(tipLabel, BorderLayout.SOUTH);

        // RIGHT FIGHTER: DIZON (The Box)
        JPanel dizonSide = new JPanel(new BorderLayout(0, 4));
        dizonSide.setOpaque(false);
        dizonSide.setPreferredSize(new Dimension(170, 160));

        JLabel dizonTitle = new JLabel("DIZON (The Box)", SwingConstants.CENTER);
        dizonTitle.setFont(new Font("Arial", Font.BOLD, 14));
        dizonTitle.setForeground(new Color(180, 40, 40));

        dizonBox = new DizonBoxPanel();

        JPanel dizonHpBox = new JPanel(new GridLayout(2, 1, 0, 2));
        dizonHpBox.setOpaque(false);
        dizonHpBar = new JProgressBar(0, 70);
        dizonHpBar.setValue(70);
        dizonHpBar.setForeground(new Color(198, 40, 40));
        dizonHpBar.setPreferredSize(new Dimension(100, 14));
        dizonHpText = new JLabel("HP: 70 / 70", SwingConstants.CENTER);
        dizonHpText.setFont(new Font("Arial", Font.BOLD, 12));
        dizonHpText.setForeground(new Color(198, 40, 40));
        dizonHpBox.add(dizonHpBar);
        dizonHpBox.add(dizonHpText);

        JLabel dizonIntent = new JLabel("ATK: 10 kada turno", SwingConstants.CENTER);
        dizonIntent.setFont(new Font("Arial", Font.PLAIN, 11));
        dizonIntent.setForeground(new Color(140, 60, 60));

        JPanel dizonBottom = new JPanel(new BorderLayout(0, 2));
        dizonBottom.setOpaque(false);
        dizonBottom.add(dizonHpBox, BorderLayout.NORTH);
        dizonBottom.add(dizonIntent, BorderLayout.SOUTH);

        dizonSide.add(dizonTitle, BorderLayout.NORTH);
        dizonSide.add(dizonBox, BorderLayout.CENTER);
        dizonSide.add(dizonBottom, BorderLayout.SOUTH);

        arenaCard.add(juanSide, BorderLayout.WEST);
        arenaCard.add(centerSide, BorderLayout.CENTER);
        arenaCard.add(dizonSide, BorderLayout.EAST);

        return arenaCard;
    }

    // --- BOTTOM GAMEPLAY PANEL (Word Tray + Attack Button + Tiles at Bottom) ---
    private JPanel createBottomPanel() {
        JPanel bottomContainer = new JPanel(new BorderLayout(0, 6));
        bottomContainer.setOpaque(false);

        // 1. TOP ROW: Word Tray (Left/Center) + Prominent Attack Button (Right)
        JPanel wordAndAttackRow = new JPanel(new BorderLayout(12, 0));
        wordAndAttackRow.setOpaque(false);

        JPanel wordTrayCard = new JPanel(new BorderLayout(0, 2));
        wordTrayCard.setBackground(Color.WHITE);
        wordTrayCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(205, 210, 220), 1, true),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        wordDisplayField = new JTextField();
        wordDisplayField.setEditable(false);
        wordDisplayField.setHorizontalAlignment(JTextField.CENTER);
        wordDisplayField.setFont(new Font("Arial", Font.BOLD, 22));
        wordDisplayField.setForeground(new Color(30, 35, 50));
        wordDisplayField.setBackground(new Color(248, 250, 253));
        wordDisplayField.setBorder(BorderFactory.createLineBorder(new Color(220, 225, 235), 1));
        wordDisplayField.setPreferredSize(new Dimension(100, 36));

        damagePreviewLabel = new JLabel("Puntos: 0 • Damage: 0 HP (Pili ug mga letra sa ubos)", SwingConstants.CENTER);
        damagePreviewLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        damagePreviewLabel.setForeground(new Color(110, 115, 130));

        wordTrayCard.add(wordDisplayField, BorderLayout.CENTER);
        wordTrayCard.add(damagePreviewLabel, BorderLayout.SOUTH);

        // PROMINENT HERO ATTACK BUTTON
        attackButton = new HeroAttackButton();

        wordAndAttackRow.add(wordTrayCard, BorderLayout.CENTER);
        wordAndAttackRow.add(attackButton, BorderLayout.EAST);
        bottomContainer.add(wordAndAttackRow, BorderLayout.NORTH);

        // 2. BOTTOM ROW: Letter Tiles at Bottom flanked by Backspace, Clear, Shuffle
        JPanel tilesRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        tilesRow.setOpaque(false);

        // Left Utility buttons
        JPanel leftUtils = new JPanel(new GridLayout(2, 1, 0, 6));
        leftUtils.setOpaque(false);
        leftUtils.setPreferredSize(new Dimension(110, 100));

        backspaceButton = new JButton("⌫ Back");
        backspaceButton.setFont(new Font("Arial", Font.BOLD, 12));
        backspaceButton.setFocusPainted(false);
        if (app.customCursor != null) backspaceButton.setCursor(app.customCursor);
        backspaceButton.addActionListener(e -> backspaceTile());

        clearButton = new JButton("✕ Clear");
        clearButton.setFont(new Font("Arial", Font.BOLD, 12));
        clearButton.setFocusPainted(false);
        if (app.customCursor != null) clearButton.setCursor(app.customCursor);
        clearButton.addActionListener(e -> clearWordSelection());

        leftUtils.add(backspaceButton);
        leftUtils.add(clearButton);

        // Center 4x4 Grid of 16 Letter Tiles at the bottom of the screen
        JPanel gridPanel = new JPanel(new GridLayout(4, 4, 6, 5));
        gridPanel.setOpaque(false);
        gridPanel.setPreferredSize(new Dimension(240, 175));

        for (int i = 0; i < 16; i++) {
            LetterTileButton tile = new LetterTileButton(i);
            tiles[i] = tile;
            gridPanel.add(tile);
        }

        // Right Utility buttons & tip
        JPanel rightUtils = new JPanel(new GridLayout(2, 1, 0, 6));
        rightUtils.setOpaque(false);
        rightUtils.setPreferredSize(new Dimension(120, 100));

        shuffleButton = new JButton("↻ Bag-o");
        shuffleButton.setFont(new Font("Arial", Font.BOLD, 12));
        shuffleButton.setToolTipText("Ilisi ang tanang letra (Costs 1 Turn)");
        shuffleButton.setFocusPainted(false);
        if (app.customCursor != null) shuffleButton.setCursor(app.customCursor);
        shuffleButton.addActionListener(e -> shuffleGrid());

        JLabel bonusTip = new JLabel("<html><center><font color='#2E7D32'><b>✓ 5+ Letra</b></font><br><font color='#777777'>Dako'g Bili!</font></center></html>", SwingConstants.CENTER);
        bonusTip.setFont(new Font("Arial", Font.PLAIN, 11));

        rightUtils.add(shuffleButton);
        rightUtils.add(bonusTip);

        tilesRow.add(leftUtils);
        tilesRow.add(gridPanel);
        tilesRow.add(rightUtils);

        bottomContainer.add(tilesRow, BorderLayout.CENTER);
        return bottomContainer;
    }

    // --- GAMEPLAY ACTIONS ---

    private void dealNewGrid() {
        for (int i = 0; i < 16; i++) {
            char c = getRandomLetter();
            int pts = letterPoints.getOrDefault(c, 1);
            tiles[i].setLetterAndPoints(c, pts);
            tiles[i].setSelectedTile(false);
        }
    }

    private void onTileClicked(LetterTileButton tile) {
        if (!tile.isSelectedTile()) {
            tile.setSelectedTile(true);
            selectedTiles.push(tile);
        } else if (!selectedTiles.isEmpty() && selectedTiles.peek() == tile) {
            // Unselect top tile
            selectedTiles.pop();
            tile.setSelectedTile(false);
        }
        updateWordTray();
    }

    private void backspaceTile() {
        if (!selectedTiles.isEmpty()) {
            LetterTileButton top = selectedTiles.pop();
            top.setSelectedTile(false);
            updateWordTray();
        }
    }

    private void clearWordSelection() {
        while (!selectedTiles.isEmpty()) {
            selectedTiles.pop().setSelectedTile(false);
        }
        updateWordTray();
    }

    private void shuffleGrid() {
        clearWordSelection();
        dealNewGrid();
        battleLogLabel.setText("<html><center>🔄 Gibag-o ang mga letra sa board!<br>Niigo si Dizon samtang nag-shuffle ka!</center></html>");
        enemyTurn();
    }

    private String getSelectedWord() {
        StringBuilder sb = new StringBuilder();
        // Stack holds in reverse order of selection, so iterate backwards
        List<LetterTileButton> list = new ArrayList<>(selectedTiles);
        Collections.reverse(list);
        for (LetterTileButton b : list) {
            sb.append(b.getLetter());
        }
        return sb.toString().toLowerCase();
    }

    private void updateWordTray() {
        String word = getSelectedWord();
        if (word.isEmpty()) {
            wordDisplayField.setText("");
            damagePreviewLabel.setText("Puntos: 0 • Damage: 0 HP (Pili ug mga letra sa ubos)");
            damagePreviewLabel.setForeground(new Color(110, 115, 130));
            if (attackButton != null) attackButton.repaint();
            return;
        }

        // Format spaced letters e.g. "B  A  L  A  Y"
        StringBuilder spaced = new StringBuilder();
        for (char c : word.toUpperCase().toCharArray()) {
            spaced.append(c).append(" ");
        }
        wordDisplayField.setText(spaced.toString().trim());

        int basePts = calculateBasePoints(word);
        double mult = difficulty.getLengthMultiplier(word.length());
        int damage = (int) Math.round(basePts * mult);
        if (damage < 1) damage = 1;

        boolean valid = dm.isValidWord(word);
        String validTag = valid ? "✓ Balido nga pulong" : "✕ Dili pa balido";
        damagePreviewLabel.setText(String.format("Base: %d pts • %s (%.2fx) • Damage: %d HP • %s",
                basePts, difficulty.label, mult, damage, validTag));
        damagePreviewLabel.setForeground(valid ? new Color(46, 125, 50) : new Color(180, 50, 50));

        if (attackButton != null) attackButton.repaint();
    }

    private int calculateBasePoints(String word) {
        int sum = 0;
        for (char c : word.toLowerCase().toCharArray()) {
            sum += letterPoints.getOrDefault(c, 1);
        }
        return sum;
    }

    // --- ATTACK EXECUTION ---
    private void performAttack() {
        String word = getSelectedWord();
        if (word.length() < 2) {
            battleLogLabel.setText("<html><center>⚠️ Mubo ra kaayo ang pulong!<br>Pili ug labing menos 2 o 3 ka letra.</center></html>");
            return;
        }

        boolean valid = dm.isValidWord(word);
        if (!valid) {
            battleLogLabel.setText("<html><center>⚠️ Ang \"" + word.toUpperCase() + "\" dili makit-an sa Cebuano dictionary!</center></html>");
            if (difficulty == DifficultySetting.LISOD) {
                // On Lisod, fumble penalty applies!
                battleLogLabel.setText("<html><center>⚠️ Sayop nga pulong!<br>Sa Lisod, nakahigayon si Dizon ug niigo (-8 HP)!</center></html>");
                applyDamageToJuan(8);
            }
            return;
        }

        // Valid word attack!
        int basePts = calculateBasePoints(word);
        double mult = difficulty.getLengthMultiplier(word.length());
        int damage = (int) Math.round(basePts * mult);
        if (damage < 1) damage = 1;

        // Dial 1 demonstration message if short word on Lisod/Sakto
        String dialNote = "";
        if (difficulty == DifficultySetting.LISOD && word.length() <= 3) {
            dialNote = " (Short word penalty: 35% damage!)";
        } else if (difficulty == DifficultySetting.SAKTO && word.length() <= 3) {
            dialNote = " (Short word: 65% damage)";
        } else if (word.length() >= 5) {
            dialNote = " (Bonus long word: " + String.format("%.2f", mult) + "x!)";
        }

        // Hit Dizon
        dizonCurHp = Math.max(0, dizonCurHp - damage);
        updateCombatBars();
        if (dizonBox != null) dizonBox.triggerHit();

        // Refill used tiles (Bookworm falling tile effect)
        for (LetterTileButton usedTile : selectedTiles) {
            char newChar = getRandomLetter();
            int newPts = letterPoints.getOrDefault(newChar, 1);
            usedTile.setLetterAndPoints(newChar, newPts);
            usedTile.setSelectedTile(false);
        }
        selectedTiles.clear();
        updateWordTray();

        battleLogLabel.setText("<html><center>🎯 Gisumiter ang \"" + word.toUpperCase() + "\"! Naigo si Dizon sa " + damage + " damage!" + dialNote + "</center></html>");

        // Check Victory
        if (dizonCurHp <= 0) {
            handleVictory();
            return;
        }

        // Enemy turn
        enemyTurn();
    }

    private void enemyTurn() {
        if (dizonCurHp <= 0) return;

        // Reset timer if on Lisod
        if (difficulty.hasTimer) {
            timerSeconds = difficulty.turnLimitOrSeconds;
            pacingInfoLabel.setText("⏱️ Timer: " + timerSeconds + "s");
        } else if (difficulty == DifficultySetting.SAKTO) {
            turnsLeft--;
            pacingInfoLabel.setText("Turns Left: " + turnsLeft);
            if (turnsLeft <= 0) {
                battleLogLabel.setText("<html><center>⌛ Nahurot na ang mga turno!<br>Nasuko si Dizon ug niigo ug grabeng atake!</center></html>");
                applyDamageToJuan(999);
                return;
            }
        }

        // Dizon attacks Juan
        applyDamageToJuan(dizonAtk);
    }

    private void applyDamageToJuan(int dmg) {
        juanCurHp = Math.max(0, juanCurHp - dmg);
        updateCombatBars();
        saveHealth();
        if (juanCurHp <= 0) {
            handleDefeat();
        } else {
            String currentText = battleLogLabel.getText().replace("<html><center>", "").replace("</center></html>", "");
            battleLogLabel.setText("<html><center>" + currentText + "<br><font color='#FF6B6B'>💥 Niigo si Dizon ug " + dmg + " damage kang Juan!</font></center></html>");
        }
    }

    // Active Timer Tick (for Lisod)
    private void onTimerTick() {
        if (dizonCurHp <= 0 || juanCurHp <= 0) return;
        timerSeconds--;
        pacingInfoLabel.setText("⏱️ Timer: " + timerSeconds + "s");
        if (timerSeconds <= 5) {
            pacingInfoLabel.setForeground(new Color(210, 30, 30));
        } else {
            pacingInfoLabel.setForeground(new Color(70, 75, 90));
        }

        if (timerSeconds <= 0) {
            timerSeconds = difficulty.turnLimitOrSeconds;
            battleLogLabel.setText("<html><center>⏰ Nahurot ang oras!<br>Niigo si Dizon samtang naghunahuna ka (-" + dizonAtk + " HP)!</center></html>");
            applyDamageToJuan(dizonAtk);
        }
    }

    // --- VICTORY & DEFEAT ---
    private void handleVictory() {
        if (turnTimer != null) turnTimer.stop();

        // Calculate rewards according to Dial 4
        int coinsEarned = (int) Math.round(20 * difficulty.rewardMultiplier);
        int xpEarned = (int) Math.round(40 * difficulty.rewardMultiplier);
        int scoreEarned = (int) Math.round((dizonMaxHp * 10) * difficulty.scoreMultiplier);

        if (currentPlayer != null) {
            currentPlayer.coins += coinsEarned;
            currentPlayer.experience += xpEarned;
            currentPlayer.score += scoreEarned;
            currentPlayer.currentRound++;
            currentPlayer.currentHealth = juanCurHp;
            currentPlayer.lastSaved = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            dm.savePlayer(currentPlayer);
            dm.addHighScore(currentPlayer.playerName, currentPlayer.score, stageNum);
            dm.recordCreatureDefeated(currentPlayer.playerId, "E001");
        }

        updateJuanInfo();

        String vicMsg = "🎉 DAOG KA! (VICTORY!)\n\n"
                + "Gipildi nimo si Dizon sa " + mapName + "!\n"
                + "Kalampusan sa " + difficulty.label + " mode:\n"
                + "  • +" + coinsEarned + " Coins\n"
                + "  • +" + xpEarned + " XP\n"
                + "  • +" + scoreEarned + " Score\n\n"
                + "I-click ang OK aron makabalik sa Mapa.";

        JOptionPane.showMessageDialog(this, vicMsg, "Kadaogan!", JOptionPane.INFORMATION_MESSAGE);
        exitBattle();
    }

    private void handleDefeat() {
        if (turnTimer != null) turnTimer.stop();

        String defMsg = "💀 NAPILDI SI JUAN! (DEFEAT)\n\n"
                + "Gipildi ka ni Dizon sa " + mapName + ".\n"
                + "Gusto ba nimo sulayan pag-usab (Retry)?";

        int opt = JOptionPane.showConfirmDialog(
            this,
            defMsg,
            "Napildi",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.ERROR_MESSAGE
        );

        if (opt == JOptionPane.YES_OPTION) {
            // Restore full health on retry
            if (currentPlayer != null) {
                currentPlayer.currentHealth = juanMaxHp;
                dm.savePlayer(currentPlayer);
            }
            setupBattle(currentPlayer, mapName, stageNum, difficulty.label);
        } else {
            // Revive with full health when returning after defeat
            if (currentPlayer != null) {
                currentPlayer.currentHealth = juanMaxHp;
                dm.savePlayer(currentPlayer);
            }
            exitBattle();
        }
    }

    public void saveHealth() {
        if (currentPlayer != null) {
            currentPlayer.currentHealth = juanCurHp;
            currentPlayer.lastSaved = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            dm.savePlayer(currentPlayer);
        }
    }

    private void exitBattle() {
        if (turnTimer != null) turnTimer.stop();
        saveHealth(); // Persist health when leaving battle!
        app.returnToMapSelect();
    }

    private void updateHeaderInfo() {
        locationTitleLabel.setText(mapName.toUpperCase() + " - STAGE " + stageNum);
        difficultyBadgeLabel.setText(" [ " + difficulty.label.toUpperCase() + " ] ");
        difficultyBadgeLabel.setBackground(difficulty.badgeColor);

        if (difficulty.hasTimer) {
            pacingInfoLabel.setText("⏱️ Timer: " + timerSeconds + "s");
        } else if (difficulty == DifficultySetting.SAKTO) {
            pacingInfoLabel.setText("Turns Left: " + turnsLeft);
        } else {
            pacingInfoLabel.setText("Pacing: Relaxed (No Timer)");
        }
    }

    private void updateCombatBars() {
        juanHpBar.setMaximum(juanMaxHp);
        juanHpBar.setValue(juanCurHp);
        juanHpText.setText("HP: " + juanCurHp + " / " + juanMaxHp);

        dizonHpBar.setMaximum(dizonMaxHp);
        dizonHpBar.setValue(dizonCurHp);
        dizonHpText.setText("HP: " + dizonCurHp + " / " + dizonMaxHp);
    }

    private void updateJuanInfo() {
        if (currentPlayer != null) {
            juanStatsLabel.setText("Coins: " + currentPlayer.coins + " • Score: " + currentPlayer.score);
        }
    }

    // --- PROMINENT HERO ATTACK BUTTON ---
    private class HeroAttackButton extends JButton {
        private boolean isHovered = false;

        HeroAttackButton() {
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setPreferredSize(new Dimension(200, 52));
            if (app.customCursor != null) setCursor(app.customCursor);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });

            addActionListener(e -> performAttack());
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth() - 6;
            int h = getHeight() - 6;
            int x = 3;
            int y = 3;

            String word = getSelectedWord();
            boolean hasWord = !word.isEmpty();
            boolean isValid = hasWord && dm.isValidWord(word);
            int damage = 0;
            if (hasWord) {
                int basePts = calculateBasePoints(word);
                double mult = difficulty.getLengthMultiplier(word.length());
                damage = Math.max(1, (int) Math.round(basePts * mult));
            }

            // Drop shadow
            g2.setColor(new Color(0, 0, 0, 35));
            g2.fillRoundRect(x + 2, y + 3, w, h, 14, 14);

            // Background gradient
            if (isValid) {
                // Vibrant Hero Green when valid
                Color topC = isHovered ? new Color(55, 195, 85) : new Color(42, 165, 70);
                Color botC = isHovered ? new Color(30, 145, 55) : new Color(22, 125, 45);
                g2.setPaint(new GradientPaint(x, y, topC, x, y + h, botC));
            } else if (hasWord) {
                // Amber Orange when word in progress but not yet recognized
                Color topC = isHovered ? new Color(245, 145, 50) : new Color(230, 125, 30);
                Color botC = isHovered ? new Color(200, 105, 20) : new Color(185, 90, 15);
                g2.setPaint(new GradientPaint(x, y, topC, x, y + h, botC));
            } else {
                // Standby slate green
                Color topC = isHovered ? new Color(90, 120, 105) : new Color(78, 105, 92);
                Color botC = isHovered ? new Color(68, 92, 80) : new Color(58, 80, 70);
                g2.setPaint(new GradientPaint(x, y, topC, x, y + h, botC));
            }
            g2.fillRoundRect(x, y, w, h, 14, 14);

            // Glowing Border
            if (isValid) {
                g2.setColor(new Color(255, 230, 100)); // Glowing Gold Border
                g2.setStroke(new BasicStroke(2.8f));
            } else if (hasWord) {
                g2.setColor(new Color(255, 190, 100));
                g2.setStroke(new BasicStroke(2.0f));
            } else {
                g2.setColor(new Color(130, 155, 140));
                g2.setStroke(new BasicStroke(1.2f));
            }
            g2.drawRoundRect(x, y, w, h, 14, 14);

            // Main Text: ⚔️ ATAKE!
            g2.setFont(new Font("Arial", Font.BOLD, 17));
            String title = "⚔️ ATAKE!";
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(title);

            // Subtitle or Damage Badge
            String sub;
            if (isValid) {
                sub = "⚡ " + damage + " DMG (Isumiter)";
            } else if (hasWord) {
                sub = "(Dili pa balido)";
            } else {
                sub = "(Pili ug letra)";
            }
            g2.setFont(new Font("Arial", Font.PLAIN, 11));
            int sw = g2.getFontMetrics().stringWidth(sub);

            // Draw Title and Subtitle centered
            g2.setFont(new Font("Arial", Font.BOLD, 17));
            g2.setColor(new Color(0, 0, 0, 70));
            g2.drawString(title, x + (w - tw) / 2 + 1, y + 23); // shadow
            g2.setColor(Color.WHITE);
            g2.drawString(title, x + (w - tw) / 2, y + 22);

            g2.setFont(new Font("Arial", Font.BOLD, 11));
            g2.setColor(isValid ? new Color(255, 240, 130) : new Color(220, 230, 225));
            g2.drawString(sub, x + (w - sw) / 2, y + 40);

            g2.dispose();
        }
    }

    // --- CUSTOM DIZON BOX SPRITE PANEL ---
    private static class DizonBoxPanel extends JPanel {
        private boolean isHurt = false;
        private int shakeOffset = 0;

        DizonBoxPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(140, 100));
        }

        void setHurt(boolean hurt) {
            this.isHurt = hurt;
            repaint();
        }

        void triggerHit() {
            isHurt = true;
            shakeOffset = 6;
            repaint();
            new javax.swing.Timer(180, e -> {
                isHurt = false;
                shakeOffset = 0;
                repaint();
                ((javax.swing.Timer) e.getSource()).stop();
            }).start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = 110;
            int h = 85;
            int x = (getWidth() - w) / 2 + shakeOffset;
            int y = (getHeight() - h) / 2;

            // Box shadow
            g2.setColor(new Color(0, 0, 0, 35));
            g2.fillOval(x + 5, y + h - 6, w - 10, 14);

            // Wooden Crate Body
            Color topWood = isHurt ? new Color(225, 90, 80) : new Color(190, 130, 80);
            Color botWood = isHurt ? new Color(180, 50, 45) : new Color(135, 85, 45);
            GradientPaint gp = new GradientPaint(x, y, topWood, x, y + h, botWood);
            g2.setPaint(gp);
            g2.fillRoundRect(x, y, w, h, 14, 14);

            // Wood planks horizontal seams
            g2.setColor(new Color(90, 50, 25, 120));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(x + 4, y + (h / 3), x + w - 4, y + (h / 3));
            g2.drawLine(x + 4, y + (2 * h / 3), x + w - 4, y + (2 * h / 3));

            // Steel Corner Reinforcements
            g2.setColor(new Color(85, 90, 100));
            g2.setStroke(new BasicStroke(3f));
            g2.drawRoundRect(x, y, w, h, 14, 14);

            // Brass Rivets
            g2.setColor(new Color(230, 195, 80));
            g2.fillOval(x + 4, y + 4, 5, 5);
            g2.fillOval(x + w - 9, y + 4, 5, 5);
            g2.fillOval(x + 4, y + h - 9, 5, 5);
            g2.fillOval(x + w - 9, y + h - 9, 5, 5);

            // Expressive Eyes
            if (isHurt) {
                // Hurt eyes: > <
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Arial", Font.BOLD, 16));
                g2.drawString(">  <", x + (w / 2) - 14, y + 24);
            } else {
                // Determined eyes
                g2.setColor(Color.WHITE);
                g2.fillOval(x + 28, y + 14, 14, 14);
                g2.fillOval(x + 68, y + 14, 14, 14);
                g2.setColor(new Color(20, 20, 30));
                g2.fillOval(x + 32, y + 17, 7, 7);
                g2.fillOval(x + 72, y + 17, 7, 7);
                // Eyebrows
                g2.setStroke(new BasicStroke(2f));
                g2.drawLine(x + 28, y + 12, x + 42, y + 14);
                g2.drawLine(x + 68, y + 14, x + 82, y + 12);
            }

            // Nameplate Plaque: DIZON
            int plateW = 76;
            int plateH = 22;
            int plateX = x + (w - plateW) / 2;
            int plateY = y + 44;

            g2.setColor(new Color(235, 195, 95));
            g2.fillRoundRect(plateX, plateY, plateW, plateH, 6, 6);
            g2.setColor(new Color(130, 95, 30));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(plateX, plateY, plateW, plateH, 6, 6);

            g2.setColor(new Color(40, 25, 10));
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            String nameStr = "DIZON";
            int tw = g2.getFontMetrics().stringWidth(nameStr);
            g2.drawString(nameStr, plateX + (plateW - tw) / 2, plateY + 16);

            g2.dispose();
        }
    }

    // --- CUSTOM BOOKWORM LETTER TILE BUTTON ---
    private class LetterTileButton extends JButton {
        private final int index;
        private char letter = 'a';
        private int points = 1;
        private boolean isSelected = false;
        private boolean isHovered = false;

        LetterTileButton(int index) {
            this.index = index;
            setFont(new Font("Arial", Font.BOLD, 20));
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setPreferredSize(new Dimension(54, 38));
            if (app.customCursor != null) setCursor(app.customCursor);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }
            });

            addActionListener(e -> onTileClicked(this));
        }

        void setLetterAndPoints(char l, int pts) {
            this.letter = Character.toLowerCase(l);
            this.points = pts;
            repaint();
        }

        char getLetter() { return letter; }
        boolean isSelectedTile() { return isSelected; }
        void setSelectedTile(boolean sel) {
            this.isSelected = sel;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth() - 4;
            int h = getHeight() - 4;
            int x = 2;
            int y = 2;

            if (isSelected) {
                // Bookworm Golden Amber Selected Tile
                GradientPaint gp = new GradientPaint(x, y, new Color(255, 218, 90), x, y + h, new Color(255, 170, 40));
                g2.setPaint(gp);
                g2.fillRoundRect(x, y, w, h, 10, 10);

                g2.setColor(new Color(200, 115, 10));
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawRoundRect(x, y, w, h, 10, 10);
            } else {
                // Ivory Scrabble/Bookworm Tile
                Color bg = isHovered ? new Color(255, 255, 255) : new Color(250, 248, 242);
                g2.setColor(bg);
                g2.fillRoundRect(x, y, w, h, 10, 10);

                g2.setColor(isHovered ? new Color(70, 130, 230) : new Color(210, 205, 195));
                g2.setStroke(new BasicStroke(isHovered ? 2f : 1.2f));
                g2.drawRoundRect(x, y, w, h, 10, 10);
            }

            // Main Letter (Centered)
            g2.setFont(new Font("Arial", Font.BOLD, 19));
            g2.setColor(isSelected ? new Color(55, 30, 5) : new Color(35, 40, 55));
            String str = String.valueOf(Character.toUpperCase(letter));
            FontMetrics fm = g2.getFontMetrics();
            int lx = x + (w - fm.stringWidth(str)) / 2;
            int ly = y + (h + fm.getAscent() - fm.getDescent()) / 2 - 1;
            g2.drawString(str, lx, ly);

            // Points Value (Bottom Right Subscript)
            g2.setFont(new Font("Arial", Font.BOLD, 9));
            g2.setColor(isSelected ? new Color(90, 50, 10) : new Color(130, 135, 145));
            String ptStr = String.valueOf(points);
            int px = x + w - g2.getFontMetrics().stringWidth(ptStr) - 4;
            int py = y + h - 4;
            g2.drawString(ptStr, px, py);

            g2.dispose();
        }
    }
}
