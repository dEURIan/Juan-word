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
        SAYON("Sayon", GamePalette.FOREST_GREEN, 50, 6, 1.0, 1.0, false, 0),
        SAKTO("Sakto", GamePalette.PUMPKIN_ORANGE, 70, 10, 1.5, 1.5, false, 12),
        LISOD("Lisod", GamePalette.CRIMSON_RED, 110, 15, 2.5, 2.5, true, 25);

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

    // Enemy details (Stage 1: Dizon, Stage 2: Alviola, Stage 3: Rola)
    private Enemy currentEnemy;
    private String enemyName = "DIZON";
    private String enemyTitle = "DIZON (The Box)";
    private String enemySubtitle = "Ang Kahoy nga Kahon";
    private int enemyMaxHp = 70;
    private int enemyCurHp = 70;
    private int enemyAtk = 10;

    public Enemy getCurrentEnemy() {
        return currentEnemy;
    }

    // Compatibility aliases for tests & reflection
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
    private JLayeredPane layeredPane;
    private JPanel battleContentPanel;
    private RoundTitleCardOverlay titleCardOverlay;

    private JLabel locationTitleLabel;
    private JLabel difficultyBadgeLabel;
    private JLabel pacingInfoLabel;

    private PlayerHealthBar juanHpBar;
    private JLabel juanHpText;
    private JLabel juanStatsLabel;
    private JuanAvatarPanel juanAvatarPanel;
    private boolean isEnemyTurnActive = false;

    private JLabel enemyTitleLabel;
    private JLabel enemyIntentLabel;
    private EnemyHealthBar dizonHealthBar;
    private DizonBoxPanel dizonBox;

    private BookwormWordRack wordRack;
    private JPanel poppedTilesContainer;
    private final List<PoppedLetterTile> poppedTiles = new ArrayList<>();

    private JLabel damagePreviewLabel;
    private JLabel battleLogLabel;

    private HeroAttackButton attackButton;
    private FlatGameButton backspaceButton;
    private FlatGameButton clearButton;
    private FlatGameButton shuffleButton;

    // Background images (San Pedro Cathedral)
    private Image sanPedroBgImage;
    private Image sanPedroPhoto;

    public BattlePanel(JuanWord app, DataManager dm) {
        this.app = app;
        this.dm = (dm != null) ? dm : DataManager.getInstance();
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

    // PATTERN 2: FACTORY METHOD - Configure enemy via EnemyFactory
    private void configureEnemyForStage(int stage, DifficultySetting diff) {
        this.currentEnemy = EnemyFactory.createEnemy(stage, diff);
        this.enemyName = currentEnemy.getName();
        this.enemyTitle = currentEnemy.getTitle();
        this.enemySubtitle = currentEnemy.getSubtitle();
        this.enemyMaxHp = currentEnemy.getMaxHp();
        this.enemyCurHp = currentEnemy.getCurHp();
        this.enemyAtk = currentEnemy.getAtk();

        this.dizonMaxHp = enemyMaxHp;
        this.dizonCurHp = enemyCurHp;
        this.dizonAtk = enemyAtk;
    }

    // --- SETUP BATTLE FOR MAP & DIFFICULTY ---
    // PATTERN 3: BUILDER - Primary entry point accepting BattleSession constructed via BattleSessionBuilder
    public void setupBattle(BattleSession session) {
        if (session == null) {
            session = new BattleSessionBuilder().build();
        }
        this.currentPlayer = session.getPlayer();
        this.mapName = session.getMapName();
        this.stageNum = session.getStageNum();
        this.difficulty = session.getDifficulty();

        // Configure active enemy using Factory Method
        configureEnemyForStage(stageNum, difficulty);

        // Restore enemy HP if resuming an in-progress battle
        if (session.isResume() && currentPlayer != null && session.getSavedEnemyHealth() > 0 && session.getSavedEnemyHealth() <= enemyMaxHp) {
            this.enemyCurHp = session.getSavedEnemyHealth();
            this.dizonCurHp = enemyCurHp;
            if (currentEnemy != null) currentEnemy.setCurHp(enemyCurHp);
        }

        // Player HP
        this.juanMaxHp = (currentPlayer != null && currentPlayer.maxHealth > 0) ? currentPlayer.maxHealth : 100;
        if (currentPlayer != null && currentPlayer.currentHealth > 0) {
            this.juanCurHp = Math.min(currentPlayer.currentHealth, juanMaxHp);
        } else {
            this.juanCurHp = juanMaxHp;
        }

        // Dial 3: Pacing / Timers (timer runs only after title card is dismissed)
        if (turnTimer != null) turnTimer.stop();
        if (difficulty.hasTimer) {
            timerSeconds = difficulty.turnLimitOrSeconds; // 25s
            turnTimer = new javax.swing.Timer(1000, e -> onTimerTick());
        } else {
            turnsLeft = difficulty.turnLimitOrSeconds; // 12 turns for Sakto, 0 (infinite) for Sayon
        }

        updateHeaderInfo();
        updateCombatBars();
        updateJuanInfo();
        clearWordSelection();

        // Restore saved letter tiles if resuming, otherwise deal new random grid
        boolean tilesRestored = false;
        if (session.isResume() && session.getSavedTiles() != null && !session.getSavedTiles().isEmpty()) {
            tilesRestored = restoreTiles(session.getSavedTiles());
        }
        if (!tilesRestored) {
            dealNewGrid();
        }

        saveGameState(); // Persist current health & tiles

        if (session.isResume()) {
            battleLogLabel.setText("<html><center>▶ Gipadayon ang dula!<br>Ang imong kinabuhi (" + juanCurHp + " HP) ug mga letra napreserbar.</center></html>");
        } else {
            battleLogLabel.setText("Nagsugod na ang gubat! Pagpili ug mga letra gikan sa ubos para makatake.");
        }

        if (dizonBox != null) {
            dizonBox.setEnemy(currentEnemy);
            dizonBox.setHurt(false);
        }

        // Prominent Round Title Card for every round including Round 1!
        int roundNum = (currentPlayer != null && currentPlayer.currentRound > 0) ? currentPlayer.currentRound : 1;
        showRoundTitleCard(mapName, stageNum, roundNum, enemyName, enemySubtitle);
    }

    public void setupBattle(Player player, String map, int stage, String diffName) {
        setupBattle(player, map, stage, diffName, false);
    }

    public void setupBattle(Player player, String map, int stage, String diffName, boolean isResume) {
        BattleSession session = new BattleSessionBuilder()
                .player(player)
                .mapName(map)
                .stageNum(stage)
                .difficulty(diffName)
                .asResume(isResume)
                .savedTiles((player != null && player.savedTiles != null) ? player.savedTiles : "")
                .savedEnemyHealth((player != null) ? player.savedEnemyHealth : -1)
                .build();
        setupBattle(session);
    }

    public void resumeBattle(Player player) {
        String map = (player != null && player.savedMap != null && !player.savedMap.isEmpty())
                ? player.savedMap : "San Pedro Cathedral";
        int stage = (player != null && player.currentStage > 0) ? player.currentStage : 1;
        String diff = (player != null && player.savedDifficulty != null && !player.savedDifficulty.isEmpty())
                ? player.savedDifficulty : "Sakto";
        setupBattle(player, map, stage, diff, true);
    }

    public void showRoundTitleCard(String map, int stage, int round, String enemy, String subtitle) {
        if (titleCardOverlay != null && layeredPane != null) {
            layeredPane.moveToFront(titleCardOverlay);
            titleCardOverlay.showCard(map, stage, round, enemy, subtitle, () -> {
                // Callback when title card is dismissed:
                if (difficulty.hasTimer && turnTimer != null && !turnTimer.isRunning()) {
                    turnTimer.start();
                }
            });
        }
    }

    private void buildUI() {
        // Load San Pedro Cathedral backgrounds (illustrated game art + authentic photo fallback)
        sanPedroBgImage = app.loadGameImage("san_pedro_cathedral_bg.png");
        sanPedroPhoto = app.loadGameImage("san_pedro_cathedral_photo.jpg");

        // Battle content layout with San Pedro Cathedral background
        battleContentPanel = new JPanel(new BorderLayout(0, 6)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                int w = getWidth();
                int h = getHeight();

                // Solid base: Midnight Navy
                g2.setColor(GamePalette.MIDNIGHT_NAVY);
                g2.fillRect(0, 0, w, h);

                // San Pedro Cathedral background image
                Image bg = (sanPedroBgImage != null) ? sanPedroBgImage : sanPedroPhoto;
                if (bg != null && mapName != null && mapName.toLowerCase().contains("san pedro")) {
                    int imgW = bg.getWidth(null);
                    int imgH = bg.getHeight(null);
                    if (imgW > 0 && imgH > 0) {
                        double scale = Math.max((double) w / imgW, (double) h / imgH);
                        int drawW = (int) Math.round(imgW * scale);
                        int drawH = (int) Math.round(imgH * scale);
                        int drawX = (w - drawW) / 2;
                        int drawY = (h - drawH) / 2;
                        g2.drawImage(bg, drawX, drawY, drawW, drawH, null);
                    }

                    // Scrim overlay: soft translucent Midnight Navy so the cathedral is clearly visible while keeping text sharp
                    g2.setColor(new Color(1, 0, 72, 75));
                    g2.fillRect(0, 0, w, h);
                }

                g2.dispose();
            }
        };
        battleContentPanel.setOpaque(false);
        battleContentPanel.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));

        // 1. TOP HEADER BAR
        JPanel headerBar = new JPanel(new BorderLayout(12, 0));
        headerBar.setOpaque(false);

        FlatGameButton backBtn = new FlatGameButton("← Balik sa Mapa", GamePalette.CERULEAN, GamePalette.MIDNIGHT_NAVY, GamePalette.SKY_BLUE);
        backBtn.setCornerRadius(10);
        backBtn.setPreferredSize(new Dimension(145, 34));
        if (app.customCursor != null) backBtn.setCursor(app.customCursor);
        backBtn.addActionListener(e -> exitBattle());

        JPanel centerHeader = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        centerHeader.setOpaque(false);

        locationTitleLabel = new JLabel("SAN PEDRO CATHEDRAL", SwingConstants.CENTER);
        locationTitleLabel.setFont(new Font("Arial", Font.BOLD, 17));
        locationTitleLabel.setForeground(GamePalette.CREAM);

        difficultyBadgeLabel = new JLabel(" [ SAKTO ] ");
        difficultyBadgeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        difficultyBadgeLabel.setOpaque(true);
        difficultyBadgeLabel.setForeground(GamePalette.CREAM);
        difficultyBadgeLabel.setBackground(DifficultySetting.SAKTO.badgeColor);
        difficultyBadgeLabel.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));

        centerHeader.add(locationTitleLabel);
        centerHeader.add(difficultyBadgeLabel);

        pacingInfoLabel = new JLabel("Turns Left: 12", SwingConstants.RIGHT);
        pacingInfoLabel.setFont(new Font("Arial", Font.BOLD, 13));
        pacingInfoLabel.setForeground(GamePalette.SKY);

        headerBar.add(backBtn, BorderLayout.WEST);
        headerBar.add(centerHeader, BorderLayout.CENTER);
        headerBar.add(pacingInfoLabel, BorderLayout.EAST);
        battleContentPanel.add(headerBar, BorderLayout.NORTH);

        // 2. CENTER: COMBAT ARENA (Juan vs Dizon / Alviola / Rola)
        JPanel arenaPanel = createArenaPanel();
        battleContentPanel.add(arenaPanel, BorderLayout.CENTER);

        // 3. SOUTH: BOTTOM GAMEPLAY PANEL (Word Tray + Attack Button + Letter Tiles)
        JPanel bottomPanel = createBottomPanel();
        battleContentPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Layered Pane to host Battle Content (Layer 0) and Round Title Card Overlay (Layer 1)
        layeredPane = new JLayeredPane() {
            @Override
            public void doLayout() {
                int w = getWidth();
                int h = getHeight();
                if (battleContentPanel != null) {
                    battleContentPanel.setBounds(0, 0, w, h);
                }
                if (titleCardOverlay != null) {
                    titleCardOverlay.setBounds(0, 0, w, h);
                }
            }
        };
        layeredPane.setLayout(null);
        layeredPane.add(battleContentPanel, JLayeredPane.DEFAULT_LAYER);

        titleCardOverlay = new RoundTitleCardOverlay();
        titleCardOverlay.setVisible(false);
        layeredPane.add(titleCardOverlay, JLayeredPane.PALETTE_LAYER);

        setLayout(new BorderLayout());
        add(layeredPane, BorderLayout.CENTER);
    }

    // --- UPPER COMBAT ARENA ---
    private JPanel createArenaPanel() {
        JPanel arenaCard = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Soft translucent Deep Indigo card backdrop so Juan, Bookworm rack, and enemy box are framed while cathedral shines through
                g2.setColor(new Color(16, 16, 86, 115));
                g2.fillRoundRect(0, 0, w, h, 14, 14);

                // Border: Dusk Navy (2px)
                g2.setColor(GamePalette.DUSK_NAVY);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, w - 2, h - 2, 14, 14);

                g2.dispose();
            }
        };
        arenaCard.setOpaque(false);
        arenaCard.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        // LEFT FIGHTER: JUAN
        JPanel juanSide = new JPanel(new BorderLayout(0, 4));
        juanSide.setOpaque(false);
        juanSide.setPreferredSize(new Dimension(185, 160));

        JLabel juanTitle = new JLabel("JUAN (Ikaw)", SwingConstants.CENTER);
        juanTitle.setFont(new Font("Arial", Font.BOLD, 14));
        juanTitle.setForeground(GamePalette.CREAM);

        Image sprite = app.loadGameImage("Main Character Idle_1.png");
        juanAvatarPanel = new JuanAvatarPanel(sprite);

        JPanel juanHpBox = new JPanel(new GridLayout(2, 1, 0, 3));
        juanHpBox.setOpaque(false);
        juanHpBar = new PlayerHealthBar();
        juanHpText = new JLabel("HP: 100 / 100", SwingConstants.CENTER);
        juanHpText.setFont(new Font("Arial", Font.BOLD, 12));
        juanHpText.setForeground(GamePalette.CREAM);
        juanHpBox.add(juanHpBar);
        juanHpBox.add(juanHpText);

        juanStatsLabel = new JLabel("Coins: 0 • Score: 0", SwingConstants.CENTER);
        juanStatsLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        juanStatsLabel.setForeground(GamePalette.SKY);

        JPanel juanBottom = new JPanel(new BorderLayout(0, 2));
        juanBottom.setOpaque(false);
        juanBottom.add(juanHpBox, BorderLayout.NORTH);
        juanBottom.add(juanStatsLabel, BorderLayout.SOUTH);

        juanSide.add(juanTitle, BorderLayout.NORTH);
        juanSide.add(juanAvatarPanel, BorderLayout.CENTER);
        juanSide.add(juanBottom, BorderLayout.SOUTH);

        // CENTER: BOOKWORM COMBAT CENTER (Status Log, Popped Tiles Rack, Attack & Damage Preview)
        JPanel centerSide = new JPanel(new BorderLayout(0, 4));
        centerSide.setOpaque(false);

        // 1. TOP: Status Log Card (Height ~32px)
        JPanel logCard = new JPanel(new BorderLayout());
        logCard.setBackground(GamePalette.DUSK_NAVY);
        logCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GamePalette.SKY_BLUE, 1, true),
            BorderFactory.createEmptyBorder(4, 12, 4, 12)
        ));
        logCard.setPreferredSize(new Dimension(540, 32));

        battleLogLabel = new JLabel("<html><center>Andam na ang dula. Pindota ang mga letra sa ubos aron makaporma ug pulong!</center></html>", SwingConstants.CENTER);
        battleLogLabel.setFont(new Font("Arial", Font.BOLD, 11));
        battleLogLabel.setForeground(GamePalette.CREAM);
        logCard.add(battleLogLabel, BorderLayout.CENTER);

        // 2. MIDDLE: Bookworm Word Rack where Letter Tiles POP UP (Height ~66px)
        wordRack = new BookwormWordRack();
        poppedTilesContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 6));
        poppedTilesContainer.setOpaque(false);
        wordRack.add(poppedTilesContainer, BorderLayout.CENTER);

        // 3. BOTTOM: Damage Preview Card + Prominent Hero Attack Button (Height ~46px)
        JPanel actionRow = new JPanel(new BorderLayout(8, 0));
        actionRow.setOpaque(false);

        JPanel damagePreviewCard = new JPanel(new BorderLayout(0, 0));
        damagePreviewCard.setBackground(GamePalette.DUSK_NAVY);
        damagePreviewCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GamePalette.SKY_BLUE, 1, true),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));

        damagePreviewLabel = new JLabel("Puntos: 0 • Damage: 0 HP (Pili ug mga letra sa ubos)", SwingConstants.CENTER);
        damagePreviewLabel.setFont(new Font("Arial", Font.BOLD, 12));
        damagePreviewLabel.setForeground(GamePalette.SKY);
        damagePreviewCard.add(damagePreviewLabel, BorderLayout.CENTER);

        attackButton = new HeroAttackButton();

        actionRow.add(damagePreviewCard, BorderLayout.CENTER);
        actionRow.add(attackButton, BorderLayout.EAST);

        centerSide.add(logCard, BorderLayout.NORTH);
        centerSide.add(wordRack, BorderLayout.CENTER);
        centerSide.add(actionRow, BorderLayout.SOUTH);

        // RIGHT FIGHTER: DYNAMIC ENEMY (Dizon / Alviola / Rola)
        JPanel dizonSide = new JPanel(new BorderLayout(0, 4));
        dizonSide.setOpaque(false);
        dizonSide.setPreferredSize(new Dimension(185, 160));

        enemyTitleLabel = new JLabel("DIZON (The Box)", SwingConstants.CENTER);
        enemyTitleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        enemyTitleLabel.setForeground(GamePalette.SPICY_SWEETCORN);

        dizonBox = new DizonBoxPanel();

        dizonHealthBar = new EnemyHealthBar();

        enemyIntentLabel = new JLabel("ATK: 10 kada turno", SwingConstants.CENTER);
        enemyIntentLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        enemyIntentLabel.setForeground(GamePalette.SKY);

        JPanel dizonBottom = new JPanel(new BorderLayout(0, 4));
        dizonBottom.setOpaque(false);
        dizonBottom.add(dizonHealthBar, BorderLayout.NORTH);
        dizonBottom.add(enemyIntentLabel, BorderLayout.SOUTH);

        dizonSide.add(enemyTitleLabel, BorderLayout.NORTH);
        dizonSide.add(dizonBox, BorderLayout.CENTER);
        dizonSide.add(dizonBottom, BorderLayout.SOUTH);

        arenaCard.add(juanSide, BorderLayout.WEST);
        arenaCard.add(centerSide, BorderLayout.CENTER);
        arenaCard.add(dizonSide, BorderLayout.EAST);

        return arenaCard;
    }

    // --- BOTTOM GAMEPLAY PANEL (Letter Tiles at Bottom + Utility Controls) ---
    private JPanel createBottomPanel() {
        JPanel bottomContainer = new JPanel(new BorderLayout(0, 0));
        bottomContainer.setOpaque(false);
        bottomContainer.setBorder(BorderFactory.createEmptyBorder(2, 0, 4, 0));

        // Letter Tiles at Bottom flanked by Backspace, Clear, Shuffle
        JPanel tilesRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        tilesRow.setOpaque(false);

        // Left Utility buttons
        JPanel leftUtils = new JPanel(new GridLayout(2, 1, 0, 6));
        leftUtils.setOpaque(false);
        leftUtils.setPreferredSize(new Dimension(110, 92));

        backspaceButton = new FlatGameButton("← Back", GamePalette.SEQUESTA, GamePalette.MIDNIGHT_NAVY, GamePalette.DUSK_NAVY);
        backspaceButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        backspaceButton.setCornerRadius(10);
        if (app.customCursor != null) backspaceButton.setCursor(app.customCursor);
        backspaceButton.addActionListener(e -> backspaceTile());

        clearButton = new FlatGameButton("✕ Clear", GamePalette.SEQUESTA, GamePalette.MIDNIGHT_NAVY, GamePalette.DUSK_NAVY);
        clearButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        clearButton.setCornerRadius(10);
        if (app.customCursor != null) clearButton.setCursor(app.customCursor);
        clearButton.addActionListener(e -> clearWordSelection());

        leftUtils.add(backspaceButton);
        leftUtils.add(clearButton);

        // Center 4x4 Grid of 16 Letter Tiles at the bottom of the screen
        JPanel gridPanel = new JPanel(new GridLayout(4, 4, 6, 5));
        gridPanel.setOpaque(false);
        gridPanel.setPreferredSize(new Dimension(256, 178));

        for (int i = 0; i < 16; i++) {
            LetterTileButton tile = new LetterTileButton(i);
            tiles[i] = tile;
            gridPanel.add(tile);
        }

        // Right Utility buttons & tip
        JPanel rightUtils = new JPanel(new GridLayout(2, 1, 0, 6));
        rightUtils.setOpaque(false);
        rightUtils.setPreferredSize(new Dimension(120, 92));

        shuffleButton = new FlatGameButton("Bag-o", GamePalette.SEQUESTA, GamePalette.MIDNIGHT_NAVY, GamePalette.DUSK_NAVY);
        shuffleButton.setFont(new Font("SansSerif", Font.BOLD, 12));
        shuffleButton.setToolTipText("Ilisi ang tanang letra (Costs 1 Turn)");
        shuffleButton.setCornerRadius(10);
        if (app.customCursor != null) shuffleButton.setCursor(app.customCursor);
        shuffleButton.addActionListener(e -> shuffleGrid());

        JLabel bonusTip = new JLabel("<html><center><font color='#387940'><b>✓ 5+ Letra</b></font><br><font color='#9EE6FE'>Dako'g Bili!</font></center></html>", SwingConstants.CENTER);
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

    // PATTERN 4: DECORATOR - Helper to roll bonus decorated tiles (Gold & Healing)
    private TileComponent createRandomTileComponent(char c, int basePts) {
        TileComponent comp = new StandardTileComponent(c, basePts);
        int roll = rng.nextInt(100);
        if (roll < 12) {
            // 12% chance: Gold Tile Decorator (2x points)
            comp = new GoldTileDecorator(comp);
        } else if (roll < 22) {
            // 10% chance: Healing Tile Decorator (+8 HP to Juan on submit)
            comp = new HealingTileDecorator(comp);
        }
        return comp;
    }

    public String serializeTiles() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            if (tiles[i] != null) {
                if (sb.length() > 0) sb.append(";");
                TileComponent comp = tiles[i].getTileComponent();
                String effect = (comp != null) ? comp.getEffectName() : "STANDARD";
                sb.append(Character.toUpperCase(tiles[i].getLetter()))
                  .append(":").append(tiles[i].getPoints())
                  .append(":").append(effect);
            }
        }
        return sb.toString();
    }

    public boolean restoreTiles(String serialized) {
        if (serialized == null || serialized.trim().isEmpty()) return false;
        String clean = serialized.trim();
        String[] parts = clean.split(";");
        if (parts.length == 16) {
            for (int i = 0; i < 16; i++) {
                String part = parts[i].trim();
                if (part.contains(":")) {
                    String[] lp = part.split(":");
                    char c = lp[0].charAt(0);
                    int pts = 1;
                    try { pts = Integer.parseInt(lp[1].trim()); } catch (Exception ignored) {}
                    String effect = (lp.length >= 3) ? lp[2].trim().toUpperCase() : "STANDARD";

                    TileComponent comp;
                    if ("GOLD".equals(effect)) {
                        int base = (pts > 1 && pts % 2 == 0) ? pts / 2 : letterPoints.getOrDefault(Character.toLowerCase(c), 1);
                        comp = new GoldTileDecorator(new StandardTileComponent(c, base));
                    } else if ("HEAL".equals(effect)) {
                        comp = new HealingTileDecorator(new StandardTileComponent(c, pts));
                    } else {
                        comp = new StandardTileComponent(c, pts);
                    }
                    tiles[i].setTileComponent(comp);
                } else if (!part.isEmpty()) {
                    char c = part.charAt(0);
                    int pts = letterPoints.getOrDefault(Character.toLowerCase(c), 1);
                    tiles[i].setTileComponent(new StandardTileComponent(c, pts));
                }
                tiles[i].setSelectedTile(false);
            }
            return true;
        } else if (clean.length() == 16 && !clean.contains(";")) {
            for (int i = 0; i < 16; i++) {
                char c = clean.charAt(i);
                int pts = letterPoints.getOrDefault(Character.toLowerCase(c), 1);
                tiles[i].setTileComponent(new StandardTileComponent(c, pts));
                tiles[i].setSelectedTile(false);
            }
            return true;
        }
        return false;
    }

    private void dealNewGrid() {
        for (int i = 0; i < 16; i++) {
            char c = getRandomLetter();
            int pts = letterPoints.getOrDefault(c, 1);
            tiles[i].setTileComponent(createRandomTileComponent(c, pts));
            tiles[i].setSelectedTile(false);
        }
        saveGameState();
    }

    private void onTileClicked(LetterTileButton tile) {
        if (isEnemyTurnActive) return;
        if (!tile.isSelectedTile()) {
            tile.setSelectedTile(true);
            selectedTiles.push(tile);

            // Pop up into middle screen Bookworm word rack!
            PoppedLetterTile pTile = new PoppedLetterTile(tile.getTileComponent(), tile);
            poppedTiles.add(pTile);
            if (poppedTilesContainer != null) {
                poppedTilesContainer.add(pTile);
                poppedTilesContainer.revalidate();
                poppedTilesContainer.repaint();
            }
        } else if (!selectedTiles.isEmpty() && selectedTiles.peek() == tile) {
            // Unselect top tile
            selectedTiles.pop();
            tile.setSelectedTile(false);
            if (!poppedTiles.isEmpty()) {
                PoppedLetterTile removed = poppedTiles.remove(poppedTiles.size() - 1);
                if (poppedTilesContainer != null) {
                    poppedTilesContainer.remove(removed);
                    poppedTilesContainer.revalidate();
                    poppedTilesContainer.repaint();
                }
            }
        }
        updateWordTray();
    }

    private void onPoppedTileClicked(PoppedLetterTile clicked) {
        if (isEnemyTurnActive) return;
        int targetIdx = poppedTiles.indexOf(clicked);
        if (targetIdx < 0) return;

        // Deselect and pop all tiles from the end of the word down to targetIdx
        while (poppedTiles.size() > targetIdx && !selectedTiles.isEmpty()) {
            PoppedLetterTile removedTile = poppedTiles.remove(poppedTiles.size() - 1);
            if (poppedTilesContainer != null) {
                poppedTilesContainer.remove(removedTile);
            }
            LetterTileButton topButton = selectedTiles.pop();
            topButton.setSelectedTile(false);
        }

        if (poppedTilesContainer != null) {
            poppedTilesContainer.revalidate();
            poppedTilesContainer.repaint();
        }
        updateWordTray();
    }

    private void backspaceTile() {
        if (isEnemyTurnActive) return;
        if (!selectedTiles.isEmpty()) {
            LetterTileButton top = selectedTiles.pop();
            top.setSelectedTile(false);
            if (!poppedTiles.isEmpty()) {
                PoppedLetterTile removed = poppedTiles.remove(poppedTiles.size() - 1);
                if (poppedTilesContainer != null) {
                    poppedTilesContainer.remove(removed);
                    poppedTilesContainer.revalidate();
                    poppedTilesContainer.repaint();
                }
            }
            updateWordTray();
        }
    }

    private void clearWordSelection() {
        if (isEnemyTurnActive) return;
        while (!selectedTiles.isEmpty()) {
            selectedTiles.pop().setSelectedTile(false);
        }
        poppedTiles.clear();
        if (poppedTilesContainer != null) {
            poppedTilesContainer.removeAll();
            poppedTilesContainer.revalidate();
            poppedTilesContainer.repaint();
        }
        updateWordTray();
    }

    private void shuffleGrid() {
        if (isEnemyTurnActive) return;
        clearWordSelection();
        dealNewGrid();
        battleLogLabel.setText("<html><center>🔄 Gibag-o ang mga letra sa board!<br>Mo-atake si " + enemyName + " samtang nag-shuffle ka!</center></html>");
        isEnemyTurnActive = true;
        if (attackButton != null) attackButton.setEnabled(false);
        javax.swing.Timer turnDelay = new javax.swing.Timer(300, e -> {
            ((javax.swing.Timer) e.getSource()).stop();
            enemyTurn();
        });
        turnDelay.setRepeats(false);
        turnDelay.start();
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
        boolean valid = !word.isEmpty() && dm.isValidWord(word);

        if (wordRack != null) {
            wordRack.setValidWord(valid);
            wordRack.repaint();
        }

        if (word.isEmpty()) {
            damagePreviewLabel.setText("Pili ug mga letra sa ubos para makaporma ug pulong");
            damagePreviewLabel.setForeground(GamePalette.SKY);
            if (dizonHealthBar != null) dizonHealthBar.setPreviewDamage(0, false);
            if (attackButton != null) attackButton.repaint();
            return;
        }

        int basePts = calculateBasePoints(word);
        double mult = difficulty.getLengthMultiplier(word.length());
        int damage = (int) Math.round(basePts * mult);
        if (damage < 1) damage = 1;

        if (valid) {
            damagePreviewLabel.setText("✓ Balido nga pulong! Andam na mo-atake.");
            damagePreviewLabel.setForeground(GamePalette.SPICY_SWEETCORN);
            if (dizonHealthBar != null) dizonHealthBar.setPreviewDamage(damage, true);
        } else {
            damagePreviewLabel.setText("✕ Dili pa balido nga pulong");
            damagePreviewLabel.setForeground(GamePalette.PUMPKIN_ORANGE);
            if (dizonHealthBar != null) dizonHealthBar.setPreviewDamage(0, false);
        }

        if (attackButton != null) attackButton.repaint();
    }

    private int calculateSelectedTilesPoints() {
        if (selectedTiles.isEmpty()) return 0;
        int sum = 0;
        for (LetterTileButton b : selectedTiles) {
            sum += b.getPoints();
        }
        return sum;
    }

    private int calculateBasePoints(String word) {
        if (!selectedTiles.isEmpty()) {
            return calculateSelectedTilesPoints();
        }
        int sum = 0;
        for (char c : word.toLowerCase().toCharArray()) {
            sum += letterPoints.getOrDefault(c, 1);
        }
        return sum;
    }

    // --- ATTACK EXECUTION ---
    private void performAttack() {
        if (isEnemyTurnActive) return;
        String word = getSelectedWord();
        if (word.length() < 2) {
            battleLogLabel.setText("<html><center>⚠️ Mubo ra kaayo ang pulong!<br>Pili ug labing menos 2 o 3 ka letra.</center></html>");
            return;
        }

        boolean valid = dm.isValidWord(word);
        if (!valid) {
            battleLogLabel.setText("<html><center>⚠️ Ang \"" + word.toUpperCase() + "\" dili makit-an sa Cebuano dictionary!</center></html>");
            if (difficulty == DifficultySetting.LISOD) {
                // On Lisod, fumble penalty applies with enemy counterattack animation!
                battleLogLabel.setText("<html><center>⚠️ Sayop nga pulong!<br>Nakahigayon si " + enemyName + " ug niigo!</center></html>");
                if (dizonBox != null) {
                    isEnemyTurnActive = true;
                    if (attackButton != null) attackButton.setEnabled(false);
                    dizonBox.triggerAttack(
                        () -> {
                            if (juanAvatarPanel != null) juanAvatarPanel.triggerHit();
                            applyDamageToJuan(8);
                        },
                        () -> {
                            isEnemyTurnActive = false;
                            if (attackButton != null) attackButton.setEnabled(true);
                        }
                    );
                } else {
                    applyDamageToJuan(8);
                }
            }
            return;
        }

        // PATTERN 4: DECORATOR - Trigger decorator callbacks on submitted tiles (e.g. Healing, Score bonus)
        for (LetterTileButton usedTile : selectedTiles) {
            TileComponent comp = usedTile.getTileComponent();
            if (comp != null) {
                comp.onWordSubmitted(currentPlayer, this);
            }
        }

        // Valid word attack!
        int basePts = calculateBasePoints(word);
        double mult = difficulty.getLengthMultiplier(word.length());
        int damage = (int) Math.round(basePts * mult);
        if (damage < 1) damage = 1;

        String dialNote = "";
        if (word.length() >= 5) {
            dialNote = " (Kusog nga atake!)";
        }

        // Hit active enemy
        enemyCurHp = Math.max(0, enemyCurHp - damage);
        dizonCurHp = enemyCurHp;
        if (currentEnemy != null) {
            currentEnemy.takeDamage(damage);
        }
        updateCombatBars();
        if (dizonBox != null) dizonBox.triggerHit();

        // Refill used tiles (Bookworm falling tile effect) with random decorators
        for (LetterTileButton usedTile : selectedTiles) {
            char newChar = getRandomLetter();
            int newPts = letterPoints.getOrDefault(newChar, 1);
            usedTile.setTileComponent(createRandomTileComponent(newChar, newPts));
            usedTile.setSelectedTile(false);
        }
        selectedTiles.clear();
        poppedTiles.clear();
        if (poppedTilesContainer != null) {
            poppedTilesContainer.removeAll();
            poppedTilesContainer.revalidate();
            poppedTilesContainer.repaint();
        }
        updateWordTray();
        saveGameState(); // Persist refilled letter tiles & enemy HP immediately

        battleLogLabel.setText("<html><center>🎯 Gisumiter ang \"" + word.toUpperCase() + "\"! Naigo si " + enemyName + "!" + dialNote + "</center></html>");

        // Check Victory
        if (enemyCurHp <= 0) {
            handleVictory();
            return;
        }

        // Enemy turn with delayed animation!
        isEnemyTurnActive = true;
        if (attackButton != null) attackButton.setEnabled(false);

        // Pause 450ms so player can register their own hit before enemy counterattacks
        javax.swing.Timer turnDelay = new javax.swing.Timer(450, e -> {
            ((javax.swing.Timer) e.getSource()).stop();
            enemyTurn();
        });
        turnDelay.setRepeats(false);
        turnDelay.start();
    }

    private void enemyTurn() {
        if (enemyCurHp <= 0) {
            isEnemyTurnActive = false;
            if (attackButton != null) attackButton.setEnabled(true);
            return;
        }

        // Reset timer if on Lisod
        if (difficulty.hasTimer) {
            timerSeconds = difficulty.turnLimitOrSeconds;
            pacingInfoLabel.setText("⏱️ Timer: " + timerSeconds + "s");
        } else if (difficulty == DifficultySetting.SAKTO) {
            turnsLeft--;
            pacingInfoLabel.setText("Turns Left: " + turnsLeft);
            if (turnsLeft <= 0) {
                battleLogLabel.setText("<html><center>⌛ Nahurot na ang mga turno!<br>Nasuko si " + enemyName + " ug niigo ug grabeng atake!</center></html>");
                if (dizonBox != null) {
                    dizonBox.triggerAttack(
                        () -> {
                            if (juanAvatarPanel != null) juanAvatarPanel.triggerHit();
                            applyDamageToJuan(999);
                        },
                        () -> {
                            isEnemyTurnActive = false;
                        }
                    );
                } else {
                    applyDamageToJuan(999);
                }
                return;
            }
        }

        // Announce enemy attack
        battleLogLabel.setText("<html><center>⚔️ Mo-atake na si " + enemyName + "...</center></html>");

        // Enemy strikes Juan with full lunge animation!
        if (dizonBox != null) {
            dizonBox.triggerAttack(
                () -> {
                    // On impact frame: Juan recoils and takes damage!
                    if (juanAvatarPanel != null) {
                        juanAvatarPanel.triggerHit();
                    }
                    applyDamageToJuan(enemyAtk);
                },
                () -> {
                    // On animation completion: Unlock player turn
                    isEnemyTurnActive = false;
                    if (attackButton != null) {
                        attackButton.setEnabled(true);
                    }
                }
            );
        } else {
            if (juanAvatarPanel != null) juanAvatarPanel.triggerHit();
            applyDamageToJuan(enemyAtk);
            isEnemyTurnActive = false;
            if (attackButton != null) attackButton.setEnabled(true);
        }
    }

    // PATTERN 4: DECORATOR - Healing Tile effect
    public void healJuan(int amount) {
        if (amount <= 0 || juanCurHp >= juanMaxHp) return;
        int oldHp = juanCurHp;
        juanCurHp = Math.min(juanMaxHp, juanCurHp + amount);
        int restored = juanCurHp - oldHp;
        updateCombatBars();
        saveHealth();
        battleLogLabel.setText("<html><center>💚 Naayo si Juan ug +" + restored + " HP gikan sa Healing Tile!</center></html>");
    }

    private void applyDamageToJuan(int dmg) {
        juanCurHp = Math.max(0, juanCurHp - dmg);
        updateCombatBars();
        saveHealth();
        if (juanCurHp <= 0) {
            // Short delay so player sees the impact before defeat dialog appears
            javax.swing.Timer defeatTimer = new javax.swing.Timer(350, e -> {
                ((javax.swing.Timer) e.getSource()).stop();
                handleDefeat();
            });
            defeatTimer.setRepeats(false);
            defeatTimer.start();
        } else {
            battleLogLabel.setText("<html><center>💥 Niigo si " + enemyName + " ug " + dmg + " damage kang Juan!</center></html>");
        }
    }

    // Active Timer Tick (for Lisod)
    private void onTimerTick() {
        if (enemyCurHp <= 0 || juanCurHp <= 0 || isEnemyTurnActive) return;
        timerSeconds--;
        pacingInfoLabel.setText("⏱️ Timer: " + timerSeconds + "s");
        if (timerSeconds <= 5) {
            pacingInfoLabel.setForeground(GamePalette.PUMPKIN_ORANGE);
        } else {
            pacingInfoLabel.setForeground(GamePalette.SKY);
        }

        if (timerSeconds <= 0) {
            timerSeconds = difficulty.turnLimitOrSeconds;
            battleLogLabel.setText("<html><center>⏰ Nahurot ang oras!<br>Mo-atake na si " + enemyName + "!</center></html>");
            if (dizonBox != null) {
                isEnemyTurnActive = true;
                if (attackButton != null) attackButton.setEnabled(false);
                dizonBox.triggerAttack(
                    () -> {
                        if (juanAvatarPanel != null) juanAvatarPanel.triggerHit();
                        applyDamageToJuan(enemyAtk);
                    },
                    () -> {
                        isEnemyTurnActive = false;
                        if (attackButton != null) attackButton.setEnabled(true);
                    }
                );
            } else {
                applyDamageToJuan(enemyAtk);
            }
        }
    }

    // --- VICTORY & DEFEAT ---
    private void handleVictory() {
        if (turnTimer != null) turnTimer.stop();

        // Calculate rewards according to stage & difficulty
        int coinsEarned = (int) Math.round(20 * stageNum * difficulty.rewardMultiplier);
        int xpEarned = (int) Math.round(40 * stageNum * difficulty.rewardMultiplier);
        int scoreEarned = (int) Math.round((enemyMaxHp * 10) * difficulty.scoreMultiplier);

        if (currentPlayer != null) {
            currentPlayer.coins += coinsEarned;
            currentPlayer.experience += xpEarned;
            currentPlayer.score += scoreEarned;
            currentPlayer.currentRound++;
            currentPlayer.currentHealth = juanCurHp;

            // Unlock next stage / boss if cleared for this specific difficulty
            String diffLabel = (difficulty != null && difficulty.label != null) ? difficulty.label : "Sakto";
            if (stageNum == 1) {
                currentPlayer.recordStageUnlock(diffLabel, 2);
            } else if (stageNum == 2) {
                currentPlayer.recordStageUnlock(diffLabel, 3); // Unlocks Boss Rola for this difficulty!
            } else if (stageNum == 3) {
                currentPlayer.recordStageUnlock(diffLabel, 4); // Cleared Boss Rola for this difficulty!
            }

            currentPlayer.lastSaved = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            dm.savePlayer(currentPlayer);
            dm.addHighScore(currentPlayer.playerName, currentPlayer.score, stageNum);
            dm.recordCreatureDefeated(currentPlayer.playerId, "E00" + stageNum);
        }

        updateJuanInfo();

        if (stageNum == 1) {
            String vicMsg = "🎉 KADAOGAN SA STAGE 1! (VICTORY!)\n\n"
                    + "Gipildi nimo si DIZON sa San Pedro Cathedral!\n"
                    + "Ganti sa " + difficulty.label + " mode:\n"
                    + "  • +" + coinsEarned + " Coins\n"
                    + "  • +" + xpEarned + " XP\n"
                    + "  • +" + scoreEarned + " Score\n\n"
                    + "⭐ Nabuksan na ang STAGE 2: ALVIOLA (The Steel Box)!\n\n"
                    + "Gusto ba nimo mopadayon dayon sa STAGE 2 batok kang ALVIOLA?";

            Object[] options = {"Padayon sa Stage 2 (Alviola) ▶", "Balik sa Mapa"};
            int choice = JOptionPane.showOptionDialog(
                this,
                vicMsg,
                "Stage 1 Kadaogan!",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
            );

            if (choice == 0) {
                // Advance to Stage 2: Alviola
                if (currentPlayer != null) {
                    currentPlayer.currentStage = 2;
                    currentPlayer.currentRound = 1;
                    currentPlayer.savedDifficulty = difficulty.label;
                    dm.savePlayer(currentPlayer);
                }
                setupBattle(currentPlayer, mapName, 2, difficulty.label);
            } else {
                exitBattle();
            }
        } else if (stageNum == 2) {
            String vicMsg = "🎉 KADAOGAN SA STAGE 2! (VICTORY!)\n\n"
                    + "Gipildi nimo si ALVIOLA sa San Pedro Cathedral!\n"
                    + "Ganti sa " + difficulty.label + " mode:\n"
                    + "  • +" + coinsEarned + " Coins\n"
                    + "  • +" + xpEarned + " XP\n"
                    + "  • +" + scoreEarned + " Score\n\n"
                    + "👑 Nabuksan na ang STAGE 3: ROLA (THE BOSS)!\n\n"
                    + "Gusto ba nimo mosukol dayon sa BOSS nga si ROLA?";

            Object[] options = {"Sukla ang Boss (Rola) 👑", "Balik sa Mapa"};
            int choice = JOptionPane.showOptionDialog(
                this,
                vicMsg,
                "Stage 2 Kadaogan!",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
            );

            if (choice == 0) {
                // Advance to Stage 3: Rola (Boss)
                if (currentPlayer != null) {
                    currentPlayer.currentStage = 3;
                    currentPlayer.currentRound = 1;
                    currentPlayer.savedDifficulty = difficulty.label;
                    dm.savePlayer(currentPlayer);
                }
                setupBattle(currentPlayer, mapName, 3, difficulty.label);
            } else {
                exitBattle();
            }
        } else {
            // Stage 3 (Rola) Defeated!
            if (currentPlayer != null) {
                currentPlayer.currentRound = 0; // Run successfully completed
                dm.savePlayer(currentPlayer);
            }
            String vicMsg = "👑 DAKONG KADAOGAN! (SAN PEDRO CATHEDRAL CLEARED!)\n\n"
                    + "Napildi nimo ang Boss nga si ROLA!\n"
                    + "Nahuman nimo ang tanang 3 ka Yugto sa San Pedro Cathedral!\n\n"
                    + "Dakong Ganti:\n"
                    + "  • +" + coinsEarned + " Coins\n"
                    + "  • +" + xpEarned + " XP\n"
                    + "  • +" + scoreEarned + " Score\n\n"
                    + "Pahalipay! Andam na si Juan sa sunod nga mga hagit!";

            JOptionPane.showMessageDialog(this, vicMsg, "Kadaogan sa San Pedro Cathedral!", JOptionPane.INFORMATION_MESSAGE);
            exitBattle();
        }
    }

    private void handleDefeat() {
        if (turnTimer != null) turnTimer.stop();

        String defMsg = "💀 NAPILDI SI JUAN! (DEFEAT)\n\n"
                + "Gipildi ka ni " + enemyName + " sa Stage " + stageNum + " (" + mapName + ").\n"
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

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void saveHealth() {
        saveGameState();
    }

    public void saveGameState() {
        if (currentPlayer != null) {
            currentPlayer.currentHealth = juanCurHp;
            currentPlayer.savedTiles = serializeTiles();
            currentPlayer.savedDifficulty = (difficulty != null) ? difficulty.label : "Sakto";
            currentPlayer.savedEnemyHealth = enemyCurHp;
            currentPlayer.savedMap = (mapName != null) ? mapName : "San Pedro Cathedral";
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

        if (enemyTitleLabel != null) {
            enemyTitleLabel.setText(enemyTitle);
        }
        if (enemyIntentLabel != null) {
            enemyIntentLabel.setText("ATK: " + enemyAtk + " kada turno");
        }
    }

    private void updateCombatBars() {
        if (juanHpBar != null) {
            juanHpBar.setHp(juanCurHp, juanMaxHp);
        }
        juanHpText.setText("HP: " + juanCurHp + " / " + juanMaxHp);

        if (dizonHealthBar != null) {
            dizonHealthBar.setHp(dizonCurHp, dizonMaxHp);
        }
    }

    private void updateJuanInfo() {
        if (currentPlayer != null) {
            juanStatsLabel.setText("Coins: " + currentPlayer.coins + " • Score: " + currentPlayer.score);
        }
    }

    // --- PLAYER HEALTH BAR (Solid Forest Green) ---
    private static class PlayerHealthBar extends JPanel {
        private int curHp = 100;
        private int maxHp = 100;

        PlayerHealthBar() {
            setOpaque(false);
            setPreferredSize(new Dimension(100, 15));
            setMinimumSize(new Dimension(80, 15));
        }

        void setHp(int cur, int max) {
            this.curHp = Math.max(0, cur);
            this.maxHp = Math.max(1, max);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth() - 2;
            int h = getHeight() - 2;
            int x = 1;
            int y = 1;
            int arc = 6;

            // Background / Depleted HP: Midnight Navy
            g2.setColor(GamePalette.MIDNIGHT_NAVY);
            g2.fillRoundRect(x, y, w, h, arc, arc);

            // Fill: Forest Green
            double ratio = Math.max(0.0, Math.min(1.0, (double) curHp / maxHp));
            int fillW = (int) Math.round(w * ratio);
            if (fillW > 0) {
                Shape clip = g2.getClip();
                g2.clip(new java.awt.geom.RoundRectangle2D.Float(x, y, w, h, arc, arc));
                g2.setColor(GamePalette.FOREST_GREEN);
                g2.fillRect(x, y, fillW, h);
                g2.setClip(clip);
            }

            // Outer frame: Dusk Navy
            g2.setColor(GamePalette.DUSK_NAVY);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(x, y, w, h, arc, arc);

            g2.dispose();
        }
    }

    // --- BOOKWORM WORD RACK (Middle of Screen) ---
    private class BookwormWordRack extends JPanel {
        private boolean isValidWord = false;

        BookwormWordRack() {
            setLayout(new BorderLayout());
            setOpaque(false);
            setPreferredSize(new Dimension(540, 66));
            setMinimumSize(new Dimension(440, 66));
        }

        void setValidWord(boolean valid) {
            this.isValidWord = valid;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth() - 4;
            int h = getHeight() - 4;
            int x = 2;
            int y = 2;

            // Drop shadow
            g2.setColor(new Color(0, 0, 0, 40));
            g2.fillRoundRect(x + 1, y + 2, w, h, 14, 14);

            // Wooden Parchment Shelf Body (Solid Dusk Navy)
            g2.setColor(GamePalette.DUSK_NAVY);
            g2.fillRoundRect(x, y, w, h, 14, 14);

            // Border: Glowing Spicy Sweetcorn when word is valid, Sky Blue when building, Deep Indigo when empty
            if (isValidWord) {
                g2.setColor(GamePalette.SPICY_SWEETCORN);
                g2.setStroke(new BasicStroke(2.2f));
            } else if (!selectedTiles.isEmpty()) {
                g2.setColor(GamePalette.SKY_BLUE);
                g2.setStroke(new BasicStroke(1.5f));
            } else {
                g2.setColor(GamePalette.DEEP_INDIGO);
                g2.setStroke(new BasicStroke(1.2f));
            }
            g2.drawRoundRect(x, y, w, h, 14, 14);

            // Shelf bottom bevel highlight
            g2.setColor(GamePalette.DEEP_INDIGO);
            g2.drawLine(x + 10, y + h - 5, x + w - 10, y + h - 5);

            // If empty: draw clean hint prompt
            if (selectedTiles.isEmpty()) {
                g2.setFont(new Font("Arial", Font.BOLD, 13));
                g2.setColor(GamePalette.SKY);
                String hint = "📖 Pindota ang mga letra sa ubos aron makaporma ug pulong";
                FontMetrics fm = g2.getFontMetrics();
                int tx = x + (w - fm.stringWidth(hint)) / 2;
                g2.drawString(hint, tx, y + (h / 2) + 5);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // --- POPPED-UP LETTER TILE (Bookworm Style) ---
    private class PoppedLetterTile extends JComponent {
        private final char letter;
        private final int points;
        private final TileComponent tileComponent;
        private final LetterTileButton sourceTile;
        private float scale = 1.45f;
        private int offsetY = -8;
        private javax.swing.Timer popTimer;
        private boolean isHovered = false;

        PoppedLetterTile(char letter, int points, LetterTileButton sourceTile) {
            this(sourceTile != null ? sourceTile.getTileComponent() : new StandardTileComponent(letter, points), sourceTile);
        }

        PoppedLetterTile(TileComponent component, LetterTileButton sourceTile) {
            this.tileComponent = (component != null) ? component : new StandardTileComponent('a', 1);
            this.letter = Character.toUpperCase(this.tileComponent.getLetter());
            this.points = this.tileComponent.getPoints();
            this.sourceTile = sourceTile;
            setPreferredSize(new Dimension(48, 54));
            setMinimumSize(new Dimension(48, 54));
            setOpaque(false);
            if (app.customCursor != null) setCursor(app.customCursor);
            setToolTipText("I-klik aron tangtangon kini nga letra (Click to remove)");

            // Pop-up animation timer: 60fps, 10 frames (~140ms)
            final long startTime = System.currentTimeMillis();
            final int duration = 140; // ms
            popTimer = new javax.swing.Timer(14, e -> {
                long elapsed = System.currentTimeMillis() - startTime;
                float t = Math.min(1.0f, (float) elapsed / duration);
                // Elastic bounce curve: starts at 1.45, dips to ~0.94, settles at 1.0
                scale = 1.0f + 0.45f * (1.0f - t) * (float) Math.cos(t * Math.PI * 1.5);
                offsetY = (int) (-8 * (1.0f - t));
                repaint();
                if (t >= 1.0f) {
                    scale = 1.0f;
                    offsetY = 0;
                    ((javax.swing.Timer) e.getSource()).stop();
                    repaint();
                }
            });
            popTimer.start();

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

                @Override
                public void mouseClicked(MouseEvent e) {
                    onPoppedTileClicked(PoppedLetterTile.this);
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int compW = getWidth();
            int compH = getHeight();
            int tileW = 44;
            int tileH = 48;

            // Apply scale and bounce offset centered on component
            g2.translate(compW / 2.0, compH / 2.0 + offsetY);
            g2.scale(scale, scale);
            g2.translate(-tileW / 2.0, -tileH / 2.0);

            int x = 0;
            int y = 0;

            // Drop shadow
            g2.setColor(new Color(0, 0, 0, 45));
            g2.fillRoundRect(x + 2, y + 4, tileW, tileH, 10, 10);

            // Tile Background (Solid)
            String currentWord = getSelectedWord();
            boolean isValid = !currentWord.isEmpty() && dm.isValidWord(currentWord);

            if (isValid) {
                // Honey Gold Bookworm Tile (Solid)
                g2.setColor(isHovered ? GamePalette.SPICY_SWEETCORN : GamePalette.HONEY_GOLD);
            } else {
                // Cream Tile (Solid)
                g2.setColor(isHovered ? Color.WHITE : GamePalette.CREAM);
            }
            g2.fillRoundRect(x, y, tileW, tileH, 10, 10);

            // Border
            if (isValid) {
                g2.setColor(isHovered ? GamePalette.SPICY_SWEETCORN : GamePalette.PUMPKIN_ORANGE);
                g2.setStroke(new BasicStroke(isHovered ? 2.2f : 1.8f));
            } else {
                g2.setColor(isHovered ? GamePalette.PUMPKIN_ORANGE : GamePalette.SKY_BLUE);
                g2.setStroke(new BasicStroke(isHovered ? 2.0f : 1.4f));
            }
            g2.drawRoundRect(x, y, tileW, tileH, 10, 10);

            // Main Letter (Centered)
            g2.setFont(new Font("Arial", Font.BOLD, 22));
            String str = String.valueOf(letter);
            FontMetrics fm = g2.getFontMetrics();
            int lx = x + (tileW - fm.stringWidth(str)) / 2;
            int ly = y + (tileH + fm.getAscent() - fm.getDescent()) / 2 - 1;

            // Letter text
            g2.setColor(GamePalette.MIDNIGHT_NAVY);
            g2.drawString(str, lx, ly);

            // Points Value (Bottom-Right Subscript)
            g2.setFont(new Font("Arial", Font.BOLD, 10));
            String ptStr = String.valueOf(points);
            int px = x + tileW - g2.getFontMetrics().stringWidth(ptStr) - 4;
            int py = y + tileH - 4;
            g2.setColor(isValid ? GamePalette.MIDNIGHT_NAVY : GamePalette.PUMPKIN_ORANGE);
            g2.drawString(ptStr, px, py);

            // PATTERN 4: DECORATOR - Render decorator badge on popped tile
            if (tileComponent != null) {
                tileComponent.renderDecoratorBadge(g2, x, y, tileW, tileH);
            }

            // Hover indicator: small "✕" on top right if hovered
            if (isHovered) {
                g2.setFont(new Font("Arial", Font.BOLD, 10));
                g2.setColor(GamePalette.PUMPKIN_ORANGE);
                g2.drawString("✕", x + tileW - 10, y + 11);
            }

            g2.dispose();
        }
    }

    // --- PROMINENT HERO ATTACK BUTTON ---
    private class HeroAttackButton extends JButton {
        private boolean isHovered = false;

        HeroAttackButton() {
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setPreferredSize(new Dimension(175, 46));
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
            g2.setColor(new Color(0, 0, 0, 40));
            g2.fillRoundRect(x + 2, y + 3, w, h, 14, 14);

            // Background (Solid)
            if (isValid) {
                // Cerulean Attack button from palette
                g2.setColor(isHovered ? GamePalette.CERULEAN.brighter() : GamePalette.CERULEAN);
            } else if (hasWord) {
                // Pumpkin Orange when word is in-progress
                g2.setColor(isHovered ? GamePalette.PUMPKIN_ORANGE.brighter() : GamePalette.PUMPKIN_ORANGE);
            } else {
                // Deep Indigo standby
                g2.setColor(isHovered ? GamePalette.DUSK_NAVY : GamePalette.DEEP_INDIGO);
            }
            g2.fillRoundRect(x, y, w, h, 14, 14);

            // Glowing Border
            if (isValid) {
                g2.setColor(GamePalette.SPICY_SWEETCORN); // Glowing Spicy Sweetcorn Border
                g2.setStroke(new BasicStroke(2.8f));
            } else if (hasWord) {
                g2.setColor(GamePalette.HONEY_GOLD);
                g2.setStroke(new BasicStroke(2.0f));
            } else {
                g2.setColor(GamePalette.DUSK_NAVY);
                g2.setStroke(new BasicStroke(1.2f));
            }
            g2.drawRoundRect(x, y, w, h, 14, 14);

            // Main Text: ⚔️ ATAKE!
            g2.setFont(new Font("Arial", Font.BOLD, 15));
            String title = "⚔️ ATAKE!";
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(title);

            // Subtitle or Damage Badge
            String sub;
            if (isValid) {
                sub = "✓ Isumiter";
            } else if (hasWord) {
                sub = "(Dili pa balido)";
            } else {
                sub = "(Pili ug letra)";
            }
            g2.setFont(new Font("Arial", Font.BOLD, 11));
            int sw = g2.getFontMetrics().stringWidth(sub);

            // Draw Title and Subtitle centered
            g2.setFont(new Font("Arial", Font.BOLD, 15));
            g2.setColor(new Color(0, 0, 0, 80));
            g2.drawString(title, x + (w - tw) / 2 + 1, y + 20); // shadow
            g2.setColor(GamePalette.CREAM);
            g2.drawString(title, x + (w - tw) / 2, y + 19);

            g2.setFont(new Font("Arial", Font.BOLD, 11));
            g2.setColor(isValid ? GamePalette.SPICY_SWEETCORN : GamePalette.SKY);
            g2.drawString(sub, x + (w - sw) / 2, y + 34);

            g2.dispose();
        }
    }

    // --- ENEMY HEALTH BAR (No numbers, highlights word damage chunk) ---
    private static class EnemyHealthBar extends JPanel {
        private int maxHp = 70;
        private int curHp = 70;
        private int previewDamage = 0;
        private boolean isValidWord = false;
        private float pulsePhase = 0f;
        private float pulseAlpha = 1.0f;
        private final javax.swing.Timer pulseTimer;

        EnemyHealthBar() {
            setOpaque(false);
            setPreferredSize(new Dimension(150, 22));
            setMinimumSize(new Dimension(130, 22));

            // Pulse timer for highlighted hit chunk
            pulseTimer = new javax.swing.Timer(35, e -> {
                if (isValidWord && previewDamage > 0) {
                    pulsePhase += 0.15f;
                    pulseAlpha = 0.82f + 0.18f * (float) Math.sin(pulsePhase);
                    repaint();
                } else if (pulseAlpha != 1.0f) {
                    pulseAlpha = 1.0f;
                    repaint();
                }
            });
            pulseTimer.start();
        }

        void setHp(int cur, int max) {
            this.curHp = Math.max(0, cur);
            this.maxHp = Math.max(1, max);
            repaint();
        }

        void setPreviewDamage(int dmg, boolean valid) {
            this.previewDamage = valid ? Math.max(0, dmg) : 0;
            this.isValidWord = valid;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth() - 4;
            int h = getHeight() - 4;
            int x = 2;
            int y = 2;
            int arc = 8;

            // 1. Drop shadow
            g2.setColor(new Color(0, 0, 0, 35));
            g2.fillRoundRect(x + 1, y + 2, w, h, arc, arc);

            // 2. Empty/Depleted Bar Background (Solid Midnight Navy)
            g2.setColor(GamePalette.MIDNIGHT_NAVY);
            g2.fillRoundRect(x, y, w, h, arc, arc);

            // Calculate health widths
            double curRatio = Math.max(0.0, Math.min(1.0, (double) curHp / maxHp));
            int curWidth = (int) Math.round(w * curRatio);

            if (curWidth > 0) {
                int dmgWidth = 0;
                int remainingWidth = curWidth;

                if (isValidWord && previewDamage > 0) {
                    double dmgRatio = Math.min(curRatio, (double) previewDamage / maxHp);
                    dmgWidth = (int) Math.round(w * dmgRatio);
                    remainingWidth = Math.max(0, curWidth - dmgWidth);
                }

                Shape clipBackup = g2.getClip();
                // Clip to health bar shape so rounded corners remain crisp
                g2.clip(new java.awt.geom.RoundRectangle2D.Float(x, y, w, h, arc, arc));

                // 3. Remaining Health (Solid Pumpkin Orange)
                if (remainingWidth > 0) {
                    g2.setColor(GamePalette.PUMPKIN_ORANGE);
                    g2.fillRect(x, y, remainingWidth, h);
                }

                // 4. Highlighted Hit Chunk (Solid with pulse alpha)
                if (dmgWidth > 0) {
                    int dmgX = x + remainingWidth;
                    boolean isLethal = previewDamage >= curHp;

                    int alphaInt = Math.max(0, Math.min(255, (int) (255 * pulseAlpha)));

                    if (isLethal) {
                        // Solid lethal Honey Gold with pulse alpha
                        Color lethalCol = new Color(GamePalette.HONEY_GOLD.getRed(),
                                                    GamePalette.HONEY_GOLD.getGreen(),
                                                    GamePalette.HONEY_GOLD.getBlue(),
                                                    alphaInt);
                        g2.setColor(lethalCol);
                        g2.fillRect(dmgX, y, dmgWidth, h);

                        // Subtle KO text in Midnight Navy
                        g2.setFont(new Font("Arial", Font.BOLD, 10));
                        String ko = "☠️ KO";
                        FontMetrics fm = g2.getFontMetrics();
                        int kx = dmgX + (dmgWidth - fm.stringWidth(ko)) / 2;
                        g2.setColor(GamePalette.MIDNIGHT_NAVY);
                        g2.drawString(ko, kx, y + (h / 2) + 3);
                    } else {
                        // Solid highlighted Spicy Sweetcorn hit chunk with pulse alpha
                        Color hitCol = new Color(GamePalette.SPICY_SWEETCORN.getRed(),
                                                 GamePalette.SPICY_SWEETCORN.getGreen(),
                                                 GamePalette.SPICY_SWEETCORN.getBlue(),
                                                 alphaInt);
                        g2.setColor(hitCol);
                        g2.fillRect(dmgX, y, dmgWidth, h);

                        // Vertical divider notch separating remaining health from damage chunk
                        g2.setColor(GamePalette.CREAM);
                        g2.setStroke(new BasicStroke(1.5f));
                        g2.drawLine(dmgX, y, dmgX, y + h);
                    }
                }

                g2.setClip(clipBackup);
            }

            // 5. Outer Metallic Frame (Glows Spicy Sweetcorn if damage chunk is active, else Dusk Navy)
            if (isValidWord && previewDamage > 0) {
                g2.setColor(GamePalette.SPICY_SWEETCORN);
                g2.setStroke(new BasicStroke(1.8f));
            } else {
                g2.setColor(GamePalette.DUSK_NAVY);
                g2.setStroke(new BasicStroke(1.2f));
            }
            g2.drawRoundRect(x, y, w, h, arc, arc);

            g2.dispose();
        }
    }

    // --- CUSTOM ENEMY BOX SPRITE PANEL (Dizon, Alviola, Rola) ---
    private static class EnemyBoxPanel extends JPanel {
        protected boolean isHurt = false;
        protected boolean isAttacking = false;
        protected int shakeOffset = 0;
        protected int lungeOffset = 0; // horizontal lunge towards Juan (negative x)
        protected int stageNum = 1;
        protected String enemyName = "DIZON";
        protected Enemy currentEnemyObj;

        EnemyBoxPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(140, 110));
        }

        void setEnemy(Enemy enemy) {
            this.currentEnemyObj = enemy;
            if (enemy != null) {
                this.stageNum = enemy.getStage();
                this.enemyName = enemy.getName();
            }
            this.isHurt = false;
            this.isAttacking = false;
            this.shakeOffset = 0;
            this.lungeOffset = 0;
            repaint();
        }

        void setEnemy(int stage, String name) {
            this.stageNum = stage;
            this.enemyName = (name != null) ? name.toUpperCase() : "DIZON";
            this.currentEnemyObj = EnemyFactory.createEnemy(stage, BattlePanel.DifficultySetting.SAKTO);
            this.isHurt = false;
            this.isAttacking = false;
            this.shakeOffset = 0;
            this.lungeOffset = 0;
            repaint();
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

        void triggerAttack(Runnable onImpact, Runnable onComplete) {
            this.isAttacking = true;
            this.isHurt = false;
            this.shakeOffset = 0;
            this.lungeOffset = 0;

            final int[] frame = {0};
            // Trajectory: Pull back right (+4, +10, +14), strike left towards Juan (-14, -30, -45 apex impact!), Recoil (-25, -10, 0)
            final int[] steps = {4, 10, 14, -14, -30, -45, -45, -25, -10, -3, 0};

            javax.swing.Timer attackTimer = new javax.swing.Timer(32, null);
            attackTimer.addActionListener(e -> {
                frame[0]++;
                if (frame[0] < steps.length) {
                    lungeOffset = steps[frame[0]];
                    // Frame 5 is peak lunge impact!
                    if (frame[0] == 5) {
                        if (onImpact != null) {
                            onImpact.run();
                        }
                    }
                    repaint();
                } else {
                    lungeOffset = 0;
                    isAttacking = false;
                    repaint();
                    attackTimer.stop();
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            });
            attackTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = 110;
            int h = 85;
            int x = (getWidth() - w) / 2 + shakeOffset + lungeOffset;
            int y = (getHeight() - h) / 2 + (stageNum == 3 ? 10 : 0); // leave room for boss crown if stage 3

            // Box shadow
            g2.setColor(new Color(0, 0, 0, 35));
            g2.fillOval(x + 5, y + h - 6, w - 10, 14);

            // Motion wind trails when lunging forward towards Juan
            if (isAttacking && lungeOffset < -5) {
                g2.setColor(GamePalette.SKY_BLUE);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawLine(x + w + 4, y + 22, x + w + 26, y + 22);
                g2.drawLine(x + w + 6, y + 42, x + w + 32, y + 42);
                g2.drawLine(x + w + 3, y + 62, x + w + 24, y + 62);
            }

            // PATTERN 2: FACTORY METHOD - Polymorphic sprite rendering via Enemy product
            if (currentEnemyObj != null) {
                currentEnemyObj.render(g2, x, y, w, h, isHurt, lungeOffset);
            } else if (stageNum == 2) {
                new AlviolaEnemy().render(g2, x, y, w, h, isHurt, lungeOffset);
            } else if (stageNum == 3) {
                new RolaBossEnemy().render(g2, x, y, w, h, isHurt, lungeOffset);
            } else {
                new DizonEnemy().render(g2, x, y, w, h, isHurt, lungeOffset);
            }

            g2.dispose();
        }
    }

    // Subclass alias for backwards compatibility
    private static class DizonBoxPanel extends EnemyBoxPanel {}

    // --- JUAN HERO AVATAR PANEL WITH HIT REACTION ANIMATION ---
    private static class JuanAvatarPanel extends JPanel {
        private final Image sprite;
        private boolean isHurt = false;
        private int shakeOffset = 0;
        private float hitFlashAlpha = 0f;

        JuanAvatarPanel(Image sprite) {
            this.sprite = sprite;
            setOpaque(false);
            setPreferredSize(new Dimension(100, 90));
        }

        void triggerHit() {
            this.isHurt = true;
            this.hitFlashAlpha = 0.75f;
            this.shakeOffset = -8;
            repaint();

            final int[] frame = {0};
            final int[] offsets = {-8, 8, -6, 5, -3, 0};
            javax.swing.Timer shakeTimer = new javax.swing.Timer(45, null);
            shakeTimer.addActionListener(e -> {
                frame[0]++;
                if (frame[0] < offsets.length) {
                    shakeOffset = offsets[frame[0]];
                    hitFlashAlpha = Math.max(0f, hitFlashAlpha - 0.15f);
                    repaint();
                } else {
                    shakeOffset = 0;
                    isHurt = false;
                    hitFlashAlpha = 0f;
                    repaint();
                    shakeTimer.stop();
                }
            });
            shakeTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int x = (getWidth() - 80) / 2 + shakeOffset;
            int y = (getHeight() - 80) / 2;

            if (sprite != null) {
                g2.drawImage(sprite, x, y, 80, 80, null);
                if (isHurt && hitFlashAlpha > 0f) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, hitFlashAlpha));
                    g2.setColor(GamePalette.CRIMSON_RED);
                    g2.fillRect(x, y, 80, 80);
                }
            } else {
                g2.setFont(new Font("Arial", Font.BOLD, 20));
                g2.setColor(isHurt ? GamePalette.CRIMSON_RED : GamePalette.CREAM);
                g2.drawString("👤 JUAN", x + 5, y + 50);
            }

            if (isHurt) {
                g2.setFont(new Font("Arial", Font.BOLD, 22));
                g2.drawString("💥", x + 40, y + 25);
            }

            g2.dispose();
        }
    }

    // --- ROUND TITLE CARD OVERLAY ---
    private class RoundTitleCardOverlay extends JPanel {
        private final JLabel locationLabel;
        private final JLabel stageRoundLabel;
        private final JLabel vsLabel;
        private final JLabel subtitleLabel;
        private final FlatGameButton dismissBtn;
        private javax.swing.Timer autoDismissTimer;
        private Runnable onDismissCallback;

        RoundTitleCardOverlay() {
            setOpaque(false);
            setLayout(new GridBagLayout());

            // Click anywhere to dismiss early
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mousePressed(java.awt.event.MouseEvent e) {
                    dismiss();
                }
            });

            // Center Modal Card (540 x 240)
            JPanel card = new JPanel(new BorderLayout(0, 10)) {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int w = getWidth();
                    int h = getHeight();

                    // Deep Indigo card background
                    g2.setColor(GamePalette.DEEP_INDIGO);
                    g2.fillRoundRect(0, 0, w, h, 20, 20);

                    // Outer border: Dusk Navy (3px)
                    g2.setColor(GamePalette.DUSK_NAVY);
                    g2.setStroke(new BasicStroke(3f));
                    g2.drawRoundRect(1, 1, w - 2, h - 2, 20, 20);

                    // Inner border: Honey Gold (1.5px)
                    g2.setColor(GamePalette.HONEY_GOLD);
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(5, 5, w - 10, h - 10, 14, 14);

                    g2.dispose();
                }
            };
            card.setOpaque(false);
            card.setPreferredSize(new Dimension(540, 240));
            card.setBorder(BorderFactory.createEmptyBorder(18, 24, 16, 24));

            // Content Panel
            JPanel content = new JPanel(new GridLayout(4, 1, 0, 4));
            content.setOpaque(false);

            locationLabel = new JLabel("SAN PEDRO CATHEDRAL", SwingConstants.CENTER);
            locationLabel.setFont(new Font("Arial", Font.BOLD, 13));
            locationLabel.setForeground(GamePalette.SKY);

            stageRoundLabel = new JLabel("STAGE 1 • ROUND 1", SwingConstants.CENTER);
            stageRoundLabel.setFont(new Font("Arial", Font.BOLD, 26));
            stageRoundLabel.setForeground(GamePalette.HONEY_GOLD);

            vsLabel = new JLabel("JUAN   ⚔️   VS   ⚔️   DIZON", SwingConstants.CENTER);
            vsLabel.setFont(new Font("Arial", Font.BOLD, 20));
            vsLabel.setForeground(GamePalette.CREAM);

            subtitleLabel = new JLabel("Ang Kahoy nga Kahon • Pangandam sa Pag-atake!", SwingConstants.CENTER);
            subtitleLabel.setFont(new Font("Arial", Font.ITALIC, 13));
            subtitleLabel.setForeground(GamePalette.SKY_BLUE);

            content.add(locationLabel);
            content.add(stageRoundLabel);
            content.add(vsLabel);
            content.add(subtitleLabel);

            card.add(content, BorderLayout.CENTER);

            // Bottom action row
            JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            actionPanel.setOpaque(false);

            dismissBtn = new FlatGameButton("Sugod na! ▶", GamePalette.CERULEAN, GamePalette.CREAM, GamePalette.HONEY_GOLD);
            dismissBtn.setFont(new Font("Arial", Font.BOLD, 13));
            dismissBtn.setPreferredSize(new Dimension(160, 36));
            dismissBtn.setCornerRadius(10);
            if (app.customCursor != null) dismissBtn.setCursor(app.customCursor);
            dismissBtn.addActionListener(e -> dismiss());
            actionPanel.add(dismissBtn);

            card.add(actionPanel, BorderLayout.SOUTH);

            add(card);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // Translucent dark Midnight Navy backdrop
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(1, 0, 72, 215));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }

        void showCard(String map, int stage, int round, String enemy, String subtitle, Runnable onDismiss) {
            this.onDismissCallback = onDismiss;
            locationLabel.setText(map.toUpperCase());
            String stageText = "STAGE " + stage + " • ROUND " + round;
            if (stage == 3) {
                stageText += " (BOSS FIGHT)";
            }
            stageRoundLabel.setText(stageText);
            vsLabel.setText("JUAN   ⚔️   VS   ⚔️   " + enemy.toUpperCase());
            subtitleLabel.setText(subtitle + " • Pangandam sa Pag-atake!");

            setVisible(true);
            revalidate();
            repaint();

            if (autoDismissTimer != null) autoDismissTimer.stop();
            // Auto dismiss after 1.8 seconds if player doesn't click
            autoDismissTimer = new javax.swing.Timer(1800, e -> {
                dismiss();
            });
            autoDismissTimer.setRepeats(false);
            autoDismissTimer.start();
        }

        void dismiss() {
            if (autoDismissTimer != null) autoDismissTimer.stop();
            if (!isVisible()) return;
            setVisible(false);
            repaint();
            if (onDismissCallback != null) {
                Runnable cb = onDismissCallback;
                onDismissCallback = null;
                cb.run();
            }
        }
    }

    // --- CUSTOM BOOKWORM LETTER TILE BUTTON ---
    private class LetterTileButton extends JButton {
        private final int index;
        private TileComponent tileComponent;
        private boolean isSelected = false;
        private boolean isHovered = false;

        LetterTileButton(int index) {
            this.index = index;
            this.tileComponent = new StandardTileComponent('a', 1);
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

        void setTileComponent(TileComponent comp) {
            this.tileComponent = (comp != null) ? comp : new StandardTileComponent('a', 1);
            repaint();
        }

        TileComponent getTileComponent() {
            return tileComponent;
        }

        void setLetterAndPoints(char l, int pts) {
            this.tileComponent = new StandardTileComponent(l, pts);
            repaint();
        }

        char getLetter() { return tileComponent != null ? tileComponent.getLetter() : 'a'; }
        int getPoints() { return tileComponent != null ? tileComponent.getPoints() : 1; }
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
                // Honey Gold Selected Tile (Solid)
                g2.setColor(GamePalette.HONEY_GOLD);
                g2.fillRoundRect(x, y, w, h, 10, 10);

                g2.setColor(GamePalette.PUMPKIN_ORANGE);
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawRoundRect(x, y, w, h, 10, 10);
            } else {
                // Cream Tile
                Color bg = isHovered ? Color.WHITE : GamePalette.CREAM;
                g2.setColor(bg);
                g2.fillRoundRect(x, y, w, h, 10, 10);

                g2.setColor(isHovered ? GamePalette.SKY_BLUE : GamePalette.DUSK_NAVY);
                g2.setStroke(new BasicStroke(isHovered ? 2f : 1.2f));
                g2.drawRoundRect(x, y, w, h, 10, 10);
            }

            // Main Letter (Centered)
            g2.setFont(new Font("Arial", Font.BOLD, 19));
            g2.setColor(GamePalette.MIDNIGHT_NAVY);
            String str = String.valueOf(Character.toUpperCase(getLetter()));
            FontMetrics fm = g2.getFontMetrics();
            int lx = x + (w - fm.stringWidth(str)) / 2;
            int ly = y + (h + fm.getAscent() - fm.getDescent()) / 2 - 1;
            g2.drawString(str, lx, ly);

            // Points Value (Bottom Right Subscript)
            g2.setFont(new Font("Arial", Font.BOLD, 9));
            g2.setColor(isSelected ? GamePalette.MIDNIGHT_NAVY : GamePalette.PUMPKIN_ORANGE);
            String ptStr = String.valueOf(getPoints());
            int px = x + w - g2.getFontMetrics().stringWidth(ptStr) - 4;
            int py = y + h - 4;
            g2.drawString(ptStr, px, py);

            // PATTERN 4: DECORATOR - Render decorator badge (Gold star / Healing heart)
            if (tileComponent != null) {
                tileComponent.renderDecoratorBadge(g2, x, y, w, h);
            }

            g2.dispose();
        }
    }
}
