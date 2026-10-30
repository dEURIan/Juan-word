package juanword;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class JuanWord extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainContainer;
    Image backgroundImage;
    Cursor customCursor;
    private final DataManager dataManager = DataManager.getInstance();
    Player currentPlayer;

    // Top-left Player Card components
    private JLabel playerNameLabel;
    private JLabel playerInfoLabel;
    private JPanel playerCardPanel;

    // Menu buttons
    private JButton continueButton;

    // Battle Panel
    private BattlePanel battlePanel;

    public JuanWord() {
        setTitle("Juan Word");
        setSize(960, 540); // 16:9 aspect ratio
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null); // Center window on screen

        // Save game state when closing via window 'X' or JVM shutdown
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveGameOnExit();
                System.exit(0);
            }
        });
        Runtime.getRuntime().addShutdownHook(new Thread(this::saveGameOnExit));

        // Setup custom game cursor
        try {
            Image cursorImage = new ImageIcon("Cursor.png").getImage();
            customCursor = Toolkit.getDefaultToolkit().createCustomCursor(
                cursorImage,
                new Point(6, 6), // Tip hotspot of the cursor
                "JuanWordCursor"
            );
            setCursor(customCursor);
        } catch (Exception e) {
            customCursor = Cursor.getDefaultCursor();
        }

        // Load background image
        backgroundImage = new ImageIcon("Background draft.png").getImage();

        // Load the last used player account on open
        currentPlayer = dataManager.loadLatestPlayer();

        // Periodic check to keep Continue button active state fresh
        new Timer(1500, e -> updateContinueButton()).start();

        // CardLayout allows switching between screens (Start Screen, Map Select, Battle, etc.)
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        battlePanel = new BattlePanel(this, dataManager);

        mainContainer.add(createStartScreen(), "START");
        mainContainer.add(createMapSelectScreen(), "MAP_SELECT");
        mainContainer.add(battlePanel, "BATTLE");

        add(mainContainer);
    }

    // --- START SCREEN ---
    private JPanel createStartScreen() {
        // Load start screen background (TITLE_PAGE.png with fallback)
        String bgPath = new java.io.File("TITLE_PAGE.png").exists() ? "TITLE_PAGE.png" : "TITILE_PAGE.png";
        Image startBg = new ImageIcon(bgPath).getImage();
        Image titleImage = new ImageIcon("Title.png").getImage();

        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(startBg, 0, 0, getWidth(), getHeight(), this);

                if (titleImage != null) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

                    // Resize title card and place it centered at the top (slightly larger)
                    int titleW = (int) (getWidth() * 0.46);
                    int titleH = titleW * 586 / 1163;
                    int titleX = (getWidth() - titleW) / 2;
                    int titleY = 18; // 18px from top

                    g2.drawImage(titleImage, titleX, titleY, titleW, titleH, this);
                    g2.dispose();
                }
            }
        };
        backgroundPanel.setLayout(new BorderLayout());

        // Top bar for Player Card (top-left)
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(16, 20, 0, 0));

        playerCardPanel = createPlayerCardPanel();
        topBar.add(playerCardPanel, BorderLayout.WEST);
        backgroundPanel.add(topBar, BorderLayout.NORTH);

        // Buttons: CONTINUE (on top of START), START, SETTING, EXIT
        continueButton = new FlatGameButton("CONTINUE", GamePalette.HONEY_GOLD, GamePalette.MIDNIGHT_NAVY, GamePalette.SPICY_SWEETCORN);
        stylePlainMenuButton(continueButton);
        continueButton.addActionListener(e -> handleContinueGame());

        ImageIcon startIcon = new ImageIcon("Start Button.png");
        JButton startButton = new JButton(startIcon);
        styleImageButton(startButton);
        startButton.addActionListener(e -> handleStartGame());

        ImageIcon settingIcon = new ImageIcon("Setting Button.png");
        JButton settingButton = new JButton(settingIcon);
        styleImageButton(settingButton);
        settingButton.addActionListener(e -> showSettingsDialog());

        // Exit button (plain button for exiting the game)
        JButton exitButton = new FlatGameButton("EXIT", GamePalette.CRIMSON_RED, GamePalette.CREAM, GamePalette.DARK_RED);
        stylePlainMenuButton(exitButton);
        exitButton.addActionListener(e -> {
            saveGameOnExit();
            System.exit(0);
        });

        // Stack buttons vertically
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setOpaque(false);

        continueButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        settingButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        buttonPanel.add(continueButton);
        buttonPanel.add(Box.createVerticalStrut(8));
        buttonPanel.add(startButton);
        buttonPanel.add(Box.createVerticalStrut(8));
        buttonPanel.add(settingButton);
        buttonPanel.add(Box.createVerticalStrut(8));
        buttonPanel.add(exitButton);

        // Center bottom buttonPanel horizontally
        JPanel bottomContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        bottomContainer.setOpaque(false);
        bottomContainer.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0)); // 16px space from bottom
        bottomContainer.add(buttonPanel);

        backgroundPanel.add(bottomContainer, BorderLayout.SOUTH);
        updateContinueButton();
        return backgroundPanel;
    }

    // --- PLACEHOLDER MAP SELECT SCREEN ---
    private JPanel createMapSelectScreen() {
        JPanel mapPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                // Semi-transparent overlay so map buttons stand out
                g.setColor(new Color(GamePalette.MIDNIGHT_NAVY.getRed(), GamePalette.MIDNIGHT_NAVY.getGreen(), GamePalette.MIDNIGHT_NAVY.getBlue(), 215));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mapPanel.setLayout(new BorderLayout(20, 20));
        mapPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 30, 30));

        // Top Header bar
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        FlatGameButton backButton = new FlatGameButton("← Balik", GamePalette.CERULEAN, GamePalette.MIDNIGHT_NAVY, GamePalette.SKY_BLUE);
        backButton.setPreferredSize(new Dimension(100, 36));
        backButton.setCornerRadius(10);
        if (customCursor != null) {
            backButton.setCursor(customCursor);
        }
        backButton.addActionListener(e -> {
            if (currentPlayer != null) {
                Player fresh = dataManager.loadPlayer(currentPlayer.playerId);
                if (fresh != null) {
                    currentPlayer = fresh;
                }
            }
            updatePlayerCard();
            updateContinueButton();
            cardLayout.show(mainContainer, "START");
        });

        JLabel titleLabel = new JLabel("MAP SELECT", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(GamePalette.CREAM);

        // Cog Settings button on top right
        Image cogImg = new ImageIcon("Cog Setting.png").getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        JButton cogButton = new JButton(new ImageIcon(cogImg));
        styleImageButton(cogButton);
        cogButton.setToolTipText("Settings");
        cogButton.addActionListener(e -> showSettingsDialog());

        headerPanel.add(backButton, BorderLayout.WEST);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(cogButton, BorderLayout.EAST);
        mapPanel.add(headerPanel, BorderLayout.NORTH);

        // Grid of 6 maps (2 rows x 3 columns)
        JPanel gridPanel = new JPanel(new GridLayout(2, 3, 20, 20));
        gridPanel.setOpaque(false);

        String[][] maps = {
            {"San Pedro Cathedral", "Katung daan nga simbahan"},
            {"Davao City Hall", "Sentro sa gobyerno"},
            {"Davao City Clock Tower", "Tungod sa City Hall"},
            {"Ateneo de Davao University", "AdDU Roxas Campus"},
            {"Ramon Magsaysay Park", "Entrance / Waling-waling"},
            {"Davao City Museum", "Durian architectural design"}
        };

        for (int i = 0; i < maps.length; i++) {
            String mapName = maps[i][0];
            String mapDesc = maps[i][1];
            boolean isUnlocked = (i == 0); // San Pedro Cathedral is unlocked; remaining 5 are locked

            MapTileButton mapBtn = new MapTileButton(mapName, mapDesc, isUnlocked);
            if (isUnlocked) {
                mapBtn.addActionListener(e -> showSanPedroStagesDialog());
            } else {
                mapBtn.addActionListener(e -> showLockedMapDialog(mapName));
            }

            gridPanel.add(mapBtn);
        }

        mapPanel.add(gridPanel, BorderLayout.CENTER);
        return mapPanel;
    }

    // Modal dialog explaining a locked map
    private void showLockedMapDialog(String mapName) {
        JOptionPane.showMessageDialog(
            this,
            "<html><center><font size='4' color='#D36F02'><b>🔒 NAKA-LOCK PA KINI NGA MAPA</b></font><br><br>"
            + "<font size='3' color='#FFF8E9'>Naka-lock pa ang <b>" + mapName + "</b>.<br>"
            + "Pildaha una ang tanang 3 ka yugto (<b>Dizon, Alviola, Rola</b>)<br>"
            + "sa <b>San Pedro Cathedral</b> aron maablihan kini!</font></center></html>",
            "Naka-lock nga Mapa",
            JOptionPane.WARNING_MESSAGE
        );
    }

    // Creates the content panel for San Pedro Cathedral stages dialog (backward-compatible overload)
    public JPanel createSanPedroStagesContent(int maxUnlockedStage, Runnable onClose, java.util.function.BiConsumer<Integer, String> onStartStage) {
        Player temp = (currentPlayer != null) ? currentPlayer : new Player("TEMP", "Temp");
        temp.unlockedSayonStage = Math.max(temp.unlockedSayonStage, maxUnlockedStage);
        temp.unlockedSaktoStage = Math.max(temp.unlockedSaktoStage, maxUnlockedStage);
        temp.unlockedLisodStage = Math.max(temp.unlockedLisodStage, maxUnlockedStage);
        return createSanPedroStagesContent(temp, onClose, onStartStage);
    }

    // Creates the content panel for San Pedro Cathedral stages dialog with dynamic per-difficulty boss & stage unlock tracking
    public JPanel createSanPedroStagesContent(Player player, Runnable onClose, java.util.function.BiConsumer<Integer, String> onStartStage) {
        JPanel contentPanel = new JPanel(new BorderLayout(12, 12));
        contentPanel.setBackground(GamePalette.DEEP_INDIGO);

        // Header panel
        JPanel topPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 0, 20));

        JLabel titleLabel = new JLabel("SAN PEDRO CATHEDRAL", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(GamePalette.CREAM);

        JLabel subLabel = new JLabel("Pilia ang Yugto (Stage) ug Kalisod (Difficulty)", SwingConstants.CENTER);
        subLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        subLabel.setForeground(GamePalette.SKY);

        topPanel.add(titleLabel);
        topPanel.add(subLabel);
        contentPanel.add(topPanel, BorderLayout.NORTH);

        // Center: Difficulty selection row + 3 Stage Cards
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 25, 10, 25));

        // Difficulty Selector Row
        JPanel diffRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        diffRow.setOpaque(false);
        JLabel diffLbl = new JLabel("Kalisod:");
        diffLbl.setFont(new Font("Arial", Font.BOLD, 13));
        diffLbl.setForeground(GamePalette.CREAM);
        diffRow.add(diffLbl);

        String[] diffs = {"Sayon", "Sakto", "Lisod"};
        Color[] diffColors = {GamePalette.FOREST_GREEN, GamePalette.PUMPKIN_ORANGE, GamePalette.CRIMSON_RED};

        String initDiff = (player != null && player.savedDifficulty != null && !player.savedDifficulty.trim().isEmpty())
                ? player.savedDifficulty.trim() : "Sakto";
        if ("easy".equalsIgnoreCase(initDiff)) initDiff = "Sayon";
        else if ("hard".equalsIgnoreCase(initDiff)) initDiff = "Lisod";
        else if (!"Sayon".equalsIgnoreCase(initDiff) && !"Lisod".equalsIgnoreCase(initDiff)) initDiff = "Sakto";

        final String[] selectedDiff = {initDiff};

        // 3 Stage Cards container
        JPanel stagesList = new JPanel(new GridLayout(3, 1, 0, 10));
        stagesList.setOpaque(false);

        String[][] stageData = {
            {"1", "DIZON", "The Box (Ang Kahoy nga Kahon)", "#D36F02"},
            {"2", "ALVIOLA", "The Steel Box (Ang Puthaw nga Kahon)", "#4DA6C5"},
            {"3", "ROLA", "The Boss Box (Ang Hari nga Kahon)", "#C62828"}
        };

        // Dynamically rebuild/refresh the 3 stage cards for the currently selected difficulty
        Runnable refreshStageCards = () -> {
            stagesList.removeAll();
            int maxUnlockedForDiff = (player != null) ? player.getMaxUnlockedStage(selectedDiff[0]) : 1;

            for (int s = 0; s < 3; s++) {
                int stageNum = s + 1;
                String enemyName = stageData[s][1];
                String stageDesc = stageData[s][2];
                String accentHex = stageData[s][3];
                boolean isStageUnlocked = (maxUnlockedForDiff >= stageNum);

                JPanel stageCard = new JPanel(new BorderLayout(10, 0));
                stageCard.setBackground(isStageUnlocked ? GamePalette.DUSK_NAVY : new Color(14, 15, 42));
                stageCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(isStageUnlocked ? Color.decode(accentHex) : new Color(28, 30, 60), 2, true),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
                ));

                // Left: Stage info
                JPanel infoPanel = new JPanel(new GridLayout(2, 1, 0, 2));
                infoPanel.setOpaque(false);

                String statusTag = isStageUnlocked ? (maxUnlockedForDiff > stageNum ? "✔ Nahuman" : "⭐ Andam") : "🔒 Naka-lock";
                Color titleCol = isStageUnlocked ? GamePalette.CREAM : new Color(120, 125, 155);

                JLabel sTitle = new JLabel("<html><b>STAGE " + stageNum + ": " + enemyName + "</b> <font size='2' color='" + (isStageUnlocked ? "#F5B820" : "#D36F02") + "'>[" + statusTag + "]</font></html>");
                sTitle.setFont(new Font("Arial", Font.BOLD, 14));
                sTitle.setForeground(titleCol);

                JLabel sDesc = new JLabel(stageDesc);
                sDesc.setFont(new Font("Arial", Font.PLAIN, 12));
                sDesc.setForeground(isStageUnlocked ? GamePalette.SKY : new Color(90, 95, 125));

                infoPanel.add(sTitle);
                infoPanel.add(sDesc);
                stageCard.add(infoPanel, BorderLayout.CENTER);

                // Right: Play button
                FlatGameButton playBtn;
                if (isStageUnlocked) {
                    playBtn = new FlatGameButton("Sukla ▶", Color.decode(accentHex), GamePalette.CREAM, GamePalette.DUSK_NAVY);
                    playBtn.setPreferredSize(new Dimension(100, 34));
                    playBtn.setCornerRadius(8);
                    if (customCursor != null) playBtn.setCursor(customCursor);
                    playBtn.addActionListener(e -> {
                        if (onStartStage != null) {
                            onStartStage.accept(stageNum, selectedDiff[0]);
                        }
                    });
                } else {
                    playBtn = new FlatGameButton("🔒 Lock", new Color(30, 32, 60), new Color(110, 115, 145), new Color(20, 22, 45));
                    playBtn.setPreferredSize(new Dimension(100, 34));
                    playBtn.setCornerRadius(8);
                    playBtn.setEnabled(false);
                }
                stageCard.add(playBtn, BorderLayout.EAST);

                stagesList.add(stageCard);
            }
            stagesList.revalidate();
            stagesList.repaint();
        };

        FlatGameButton[] diffBtns = new FlatGameButton[diffs.length];
        for (int d = 0; d < diffs.length; d++) {
            final String dName = diffs[d];
            final Color dColor = diffColors[d];
            diffBtns[d] = new FlatGameButton(dName, dColor, GamePalette.CREAM, GamePalette.DUSK_NAVY);
            diffBtns[d].setPreferredSize(new Dimension(85, 30));
            diffBtns[d].setCornerRadius(8);
            if (customCursor != null) diffBtns[d].setCursor(customCursor);

            final int idx = d;
            diffBtns[d].addActionListener(e -> {
                selectedDiff[0] = dName;
                for (int j = 0; j < diffBtns.length; j++) {
                    if (j == idx) {
                        diffBtns[j].setText("• " + diffs[j] + " •");
                        diffBtns[j].setBorderColor(GamePalette.CREAM);
                    } else {
                        diffBtns[j].setText(diffs[j]);
                        diffBtns[j].setBorderColor(GamePalette.DUSK_NAVY);
                    }
                    diffBtns[j].repaint();
                }
                refreshStageCards.run(); // Instantly update Stage 1, 2, and 3 cards!
            });
            diffRow.add(diffBtns[d]);
        }

        // Set initial selected visual state
        for (int d = 0; d < diffs.length; d++) {
            if (diffs[d].equalsIgnoreCase(selectedDiff[0])) {
                diffBtns[d].setText("• " + diffs[d] + " •");
                diffBtns[d].setBorderColor(GamePalette.CREAM);
            } else {
                diffBtns[d].setText(diffs[d]);
                diffBtns[d].setBorderColor(GamePalette.DUSK_NAVY);
            }
        }

        centerPanel.add(diffRow, BorderLayout.NORTH);

        // Initial populate of stage cards
        refreshStageCards.run();

        centerPanel.add(stagesList, BorderLayout.CENTER);
        contentPanel.add(centerPanel, BorderLayout.CENTER);

        // Bottom: Cancel / Back button
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomBar.setOpaque(false);
        bottomBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        FlatGameButton closeBtn = new FlatGameButton("Balik sa Mapa", GamePalette.DUSK_NAVY, GamePalette.CREAM, GamePalette.SKY_BLUE);
        closeBtn.setPreferredSize(new Dimension(140, 34));
        closeBtn.setCornerRadius(8);
        if (customCursor != null) closeBtn.setCursor(customCursor);
        closeBtn.addActionListener(e -> {
            if (onClose != null) onClose.run();
        });
        bottomBar.add(closeBtn);

        contentPanel.add(bottomBar, BorderLayout.SOUTH);
        return contentPanel;
    }

    // Modal dialog to choose from the 3 stages of San Pedro Cathedral
    public void showSanPedroStagesDialog() {
        if (currentPlayer == null) {
            currentPlayer = dataManager.loadLatestPlayer();
        }

        JDialog dialog = new JDialog(this, "San Pedro Cathedral - Pilia ang Yugto", true);
        dialog.setSize(520, 500);
        dialog.setLocationRelativeTo(this);
        if (customCursor != null) {
            dialog.setCursor(customCursor);
        }

        JPanel content = createSanPedroStagesContent(
            currentPlayer,
            dialog::dispose,
            (stageNum, diffName) -> {
                dialog.dispose();
                if (currentPlayer != null) {
                    currentPlayer.currentStage = stageNum;
                    currentPlayer.currentRound = 1;
                    currentPlayer.savedDifficulty = diffName;
                    currentPlayer.lastSaved = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                    dataManager.savePlayer(currentPlayer);
                    updatePlayerCard();
                    updateContinueButton();
                }
                startBattle("San Pedro Cathedral", stageNum, diffName);
            }
        );

        dialog.setContentPane(content);
        dialog.setVisible(true);
    }

    // Modal dialog to choose difficulty for a custom stage
    private void showDifficultyDialog(String mapName, int stageNum) {
        JDialog dialog = new JDialog(this, "Select Difficulty", true);
        dialog.setSize(360, 290);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(GamePalette.DEEP_INDIGO);
        dialog.setLayout(new BorderLayout(15, 15));
        if (customCursor != null) {
            dialog.setCursor(customCursor);
        }

        // Header info
        JPanel topPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 0, 15));

        JLabel titleLabel = new JLabel("DIFFICULTY", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 17));
        titleLabel.setForeground(GamePalette.CREAM);

        JLabel mapLabel = new JLabel("Map: " + mapName + " (Stage " + stageNum + ")", SwingConstants.CENTER);
        mapLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        mapLabel.setForeground(GamePalette.SKY);

        topPanel.add(titleLabel);
        topPanel.add(mapLabel);
        dialog.add(topPanel, BorderLayout.NORTH);

        // Difficulty options: Sayon, Sakto, Lisod
        JPanel buttonsPanel = new JPanel(new GridLayout(3, 1, 0, 10));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(5, 40, 15, 40));

        Object[][] difficulties = {
            {"Sayon", GamePalette.FOREST_GREEN},
            {"Sakto", GamePalette.PUMPKIN_ORANGE},
            {"Lisod", GamePalette.CRIMSON_RED}
        };

        for (Object[] diff : difficulties) {
            String diffName = (String) diff[0];
            Color btnColor = (Color) diff[1];

            FlatGameButton diffBtn = new FlatGameButton(diffName, btnColor, GamePalette.CREAM, GamePalette.DUSK_NAVY);
            diffBtn.setPreferredSize(new Dimension(240, 40));
            diffBtn.setCornerRadius(12);
            if (customCursor != null) {
                diffBtn.setCursor(customCursor);
            }

            diffBtn.addActionListener(e -> {
                dialog.dispose();
                if (currentPlayer == null) {
                    currentPlayer = dataManager.loadLatestPlayer();
                }
                if (currentPlayer != null) {
                    currentPlayer.currentStage = stageNum;
                    currentPlayer.currentRound = 1; // Mark active game run in progress
                    currentPlayer.lastSaved = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                    dataManager.savePlayer(currentPlayer);
                    updatePlayerCard();
                    updateContinueButton();
                }
                startBattle(mapName, stageNum, diffName);
            });

            buttonsPanel.add(diffBtn);
        }

        dialog.add(buttonsPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    // Modal Settings dialog that reads from and saves to data/settings.csv
    private void showSettingsDialog() {
        JDialog dialog = new JDialog(this, "Settings", true);
        dialog.setSize(380, 310);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(GamePalette.DEEP_INDIGO);
        dialog.setLayout(new BorderLayout(15, 15));
        if (customCursor != null) {
            dialog.setCursor(customCursor);
        }

        // Load current settings from data/settings.csv for active player
        String pid = (currentPlayer != null) ? currentPlayer.playerId : "P001";
        DataManager.GameSettings current = dataManager.loadSettings(pid);

        // Header
        JPanel top = new JPanel(new GridLayout(2, 1, 0, 4));
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(15, 15, 0, 15));
        JLabel title = new JLabel("SETTINGS", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(GamePalette.CREAM);
        JLabel sub = new JLabel("Configured in data/settings.csv (" + pid + ")", SwingConstants.CENTER);
        sub.setFont(new Font("Arial", Font.PLAIN, 12));
        sub.setForeground(GamePalette.SKY);
        top.add(title);
        top.add(sub);
        dialog.add(top, BorderLayout.NORTH);

        // Controls
        JPanel center = new JPanel(new GridLayout(3, 1, 0, 10));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(5, 40, 10, 40));

        JCheckBox musicCheck = new JCheckBox("Music", current.musicOn);
        musicCheck.setFont(new Font("Arial", Font.BOLD, 14));
        musicCheck.setForeground(GamePalette.CREAM);
        musicCheck.setOpaque(false);
        if (customCursor != null) musicCheck.setCursor(customCursor);

        JCheckBox sfxCheck = new JCheckBox("Sound Effects", current.sfxOn);
        sfxCheck.setFont(new Font("Arial", Font.BOLD, 14));
        sfxCheck.setForeground(GamePalette.CREAM);
        sfxCheck.setOpaque(false);
        if (customCursor != null) sfxCheck.setCursor(customCursor);

        JPanel volPanel = new JPanel(new BorderLayout(5, 5));
        volPanel.setOpaque(false);
        JLabel volLabel = new JLabel("Volume: " + current.volume + "%");
        volLabel.setFont(new Font("Arial", Font.BOLD, 13));
        volLabel.setForeground(GamePalette.SKY);
        JSlider volSlider = new JSlider(0, 100, current.volume);
        volSlider.setOpaque(false);
        if (customCursor != null) volSlider.setCursor(customCursor);
        volSlider.addChangeListener(e -> volLabel.setText("Volume: " + volSlider.getValue() + "%"));
        volPanel.add(volLabel, BorderLayout.NORTH);
        volPanel.add(volSlider, BorderLayout.CENTER);

        center.add(musicCheck);
        center.add(sfxCheck);
        center.add(volPanel);
        dialog.add(center, BorderLayout.CENTER);

        // Save Button
        FlatGameButton saveBtn = new FlatGameButton("Save & Close", GamePalette.SEQUESTA, GamePalette.MIDNIGHT_NAVY, GamePalette.DUSK_NAVY);
        saveBtn.setPreferredSize(new Dimension(150, 38));
        saveBtn.setCornerRadius(12);
        if (customCursor != null) saveBtn.setCursor(customCursor);
        saveBtn.addActionListener(e -> {
            current.playerId = pid;
            current.musicOn = musicCheck.isSelected();
            current.sfxOn = sfxCheck.isSelected();
            current.volume = volSlider.getValue();
            dataManager.saveSettings(current);
            dialog.dispose();
            JOptionPane.showMessageDialog(this, "Settings saved successfully to data/settings.csv!");
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        bottom.add(saveBtn);
        dialog.add(bottom, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    // --- TOP-LEFT PLAYER PROFILE CARD ---
    private JPanel createPlayerCardPanel() {
        JPanel card = new JPanel(new BorderLayout(8, 0)) {
            private boolean isHovered = false;
            {
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
                        showPlayerSelectDialog(false);
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Deep Indigo solid card background
                g2.setColor(isHovered ? GamePalette.DUSK_NAVY : GamePalette.DEEP_INDIGO);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);

                // Border: Spicy Sweetcorn on hover, Dusk Navy normal
                g2.setColor(isHovered ? GamePalette.SPICY_SWEETCORN : GamePalette.DUSK_NAVY);
                g2.setStroke(new BasicStroke(isHovered ? 2.0f : 1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 14));
        card.setPreferredSize(new Dimension(230, 48));
        card.setMaximumSize(new Dimension(240, 50));
        if (customCursor != null) {
            card.setCursor(customCursor);
        }
        card.setToolTipText("Click to switch or create player profiles");

        // Avatar icon on the left
        Image sprite = loadGameImage("Main Character Idle_1.png");
        JLabel avatarLabel;
        if (sprite != null) {
            Image scaled = sprite.getScaledInstance(34, 34, Image.SCALE_SMOOTH);
            avatarLabel = new JLabel(new ImageIcon(scaled));
        } else {
            avatarLabel = new JLabel("👤");
            avatarLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
            avatarLabel.setForeground(GamePalette.CREAM);
        }
        if (customCursor != null) avatarLabel.setCursor(customCursor);
        card.add(avatarLabel, BorderLayout.WEST);

        // Player Text info in center
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 1));
        textPanel.setOpaque(false);
        if (customCursor != null) textPanel.setCursor(customCursor);

        playerNameLabel = new JLabel("Player");
        playerNameLabel.setFont(new Font("Arial", Font.BOLD, 13));
        playerNameLabel.setForeground(GamePalette.HONEY_GOLD);

        playerInfoLabel = new JLabel("Stage 1 • Switch ▾");
        playerInfoLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        playerInfoLabel.setForeground(GamePalette.SKY);

        textPanel.add(playerNameLabel);
        textPanel.add(playerInfoLabel);
        card.add(textPanel, BorderLayout.CENTER);

        // Forward clicks on inner labels to dialog
        MouseAdapter forwardClick = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showPlayerSelectDialog(false);
            }
        };
        avatarLabel.addMouseListener(forwardClick);
        textPanel.addMouseListener(forwardClick);
        playerNameLabel.addMouseListener(forwardClick);
        playerInfoLabel.addMouseListener(forwardClick);

        updatePlayerCard();
        return card;
    }

    private void updatePlayerCard() {
        if (playerNameLabel == null || playerInfoLabel == null) return;
        if (currentPlayer != null) {
            playerNameLabel.setText(currentPlayer.playerName);
            playerInfoLabel.setText("Stage " + currentPlayer.currentStage + " • HP " + currentPlayer.currentHealth + "/" + currentPlayer.maxHealth + " • " + currentPlayer.coins + "c ▾");
        } else {
            playerNameLabel.setText("Walay Player");
            playerInfoLabel.setText("Pilia o Paghimo ▾");
        }
        updateContinueButton();
    }

    // Check if an active game run exists for the player
    public boolean hasActiveGame(Player p) {
        if (p == null) return false;
        // An active game is any run where a stage/round was started (currentRound >= 1), progress was made, or saved battle exists
        return p.currentRound > 0 || p.score > 0 || p.currentStage > 1 || (p.savedTiles != null && !p.savedTiles.isEmpty());
    }

    // Refresh Continue button state (enabled vs grayed-out)
    private void updateContinueButton() {
        if (continueButton == null) return;
        boolean hasAnyActive = false;
        List<Player> all = dataManager.getAllPlayers();
        for (Player p : all) {
            if (hasActiveGame(p)) {
                hasAnyActive = true;
                break;
            }
        }
        continueButton.setEnabled(hasAnyActive);
        if (hasAnyActive) {
            if (currentPlayer != null && hasActiveGame(currentPlayer)) {
                continueButton.setToolTipText("Ipadayon ang dula alang kang " + currentPlayer.playerName + " (Stage " + currentPlayer.currentStage + ", HP " + currentPlayer.currentHealth + ")");
            } else {
                continueButton.setToolTipText("Ipadayon ang aktibo nga dula (Continue active game run)");
            }
            if (customCursor != null) {
                continueButton.setCursor(customCursor);
            }
        } else {
            continueButton.setToolTipText("Walay aktibo nga dula (No active game in progress)");
            continueButton.setCursor(Cursor.getDefaultCursor());
        }
        continueButton.repaint();
    }

    // Handler for START button: if active game exists, ask confirmation before resetting
    private void handleStartGame() {
        if (hasActiveGame(currentPlayer)) {
            String msg = "Naa kay kasamtangang aktibo nga dula alang kang " + currentPlayer.playerName + ":\n"
                    + "  • Stage " + currentPlayer.currentStage + ", Round " + currentPlayer.currentRound + "\n"
                    + "  • HP: " + currentPlayer.currentHealth + "/" + currentPlayer.maxHealth + "\n"
                    + "  • Score: " + currentPlayer.score + ", Coins: " + currentPlayer.coins + "\n\n"
                    + "Kung magsugod ka ug bag-ong dula, MA-RESET kini balik sa Stage 1 Round 1!\n"
                    + "Sigurado ka nga gusto nimo i-reset ug magsugod ug bag-o?\n\n"
                    + "(Starting a new game will reset your active run back to Stage 1, Round 1.)";

            int choice = JOptionPane.showConfirmDialog(
                this,
                msg,
                "Kumpirmaha ang Pag-reset (Reset Active Game?)",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (choice != JOptionPane.YES_OPTION) {
                return; // User cancelled, do not reset and do not switch screen
            }

            // User confirmed: reset run back to fresh inactive state (currentRound = 0)
            currentPlayer.currentStage = 1;
            currentPlayer.currentRound = 0;
            currentPlayer.score = 0;
            currentPlayer.experience = 0;
            currentPlayer.coins = 0;
            currentPlayer.currentHealth = currentPlayer.maxHealth;
            currentPlayer.savedTiles = "";
            currentPlayer.savedEnemyHealth = -1;
            currentPlayer.lastSaved = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            dataManager.savePlayer(currentPlayer);
            updatePlayerCard();
            updateContinueButton();
        }

        // Proceed to map select
        cardLayout.show(mainContainer, "MAP_SELECT");
    }

    // Handler for CONTINUE button: resume active game directly or choose between multiple active save files
    private void handleContinueGame() {
        List<Player> allPlayers = dataManager.getAllPlayers();
        List<Player> activePlayers = new ArrayList<>();
        for (Player p : allPlayers) {
            if (hasActiveGame(p)) {
                activePlayers.add(p);
            }
        }

        if (activePlayers.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Walay aktibo nga dula sa bisan unsang save file.\nI-klik ang START aron makasugod ug bag-ong dula!", 
                "Walay Aktibo nga Dula", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // If multiple save files have active runs, show modal to let the user select which one to continue!
        if (activePlayers.size() > 1) {
            showContinueSaveFileDialog(activePlayers);
            return;
        }

        // Exactly one save file has an active run: resume directly!
        resumeSelectedPlayerGame(activePlayers.get(0));
    }

    private void resumeSelectedPlayerGame(Player player) {
        if (player == null) return;
        this.currentPlayer = player;
        updatePlayerCard();
        updateContinueButton();
        battlePanel.resumeBattle(currentPlayer);
        cardLayout.show(mainContainer, "BATTLE");
    }

    // Modal dialog allowing the player to select which active save file to resume
    private void showContinueSaveFileDialog(List<Player> activePlayers) {
        JDialog dialog = new JDialog(this, "Ipadayon ang Dula (Choose Save File)", true);
        dialog.setSize(560, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(0, 10));
        dialog.getContentPane().setBackground(GamePalette.MIDNIGHT_NAVY);

        // Header Panel
        JPanel header = new JPanel(new GridLayout(2, 1, 0, 4));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(16, 20, 8, 20));

        JLabel titleLbl = new JLabel("IPADAYON ANG DULA (CONTINUE GAME)", SwingConstants.CENTER);
        titleLbl.setFont(new Font("Arial", Font.BOLD, 18));
        titleLbl.setForeground(GamePalette.HONEY_GOLD);

        JLabel subLbl = new JLabel("Pilia ang save file nga gusto nimong dulaon:", SwingConstants.CENTER);
        subLbl.setFont(new Font("Arial", Font.PLAIN, 13));
        subLbl.setForeground(GamePalette.SKY);

        header.add(titleLbl);
        header.add(subLbl);
        dialog.add(header, BorderLayout.NORTH);

        // Center: List of active save files
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);
        listPanel.setBorder(BorderFactory.createEmptyBorder(6, 20, 10, 20));

        for (Player p : activePlayers) {
            JPanel card = new JPanel(new BorderLayout(12, 6));
            card.setOpaque(true);
            boolean isCur = (currentPlayer != null && p.playerId.equals(currentPlayer.playerId));
            card.setBackground(isCur ? GamePalette.DUSK_NAVY : GamePalette.DEEP_INDIGO);
            card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(isCur ? GamePalette.SPICY_SWEETCORN : GamePalette.SKY_BLUE, isCur ? 2 : 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
            ));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 86));

            // Left icon
            JLabel icon = new JLabel("💾");
            icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
            card.add(icon, BorderLayout.WEST);

            // Center details
            JPanel infoPanel = new JPanel(new GridLayout(3, 1, 0, 2));
            infoPanel.setOpaque(false);

            String enemyForStage = (p.currentStage == 2) ? "Alviola (Steel Box)" : (p.currentStage == 3) ? "Rola (The Boss)" : "Dizon (The Box)";
            String map = (p.savedMap != null && !p.savedMap.isEmpty()) ? p.savedMap : "San Pedro Cathedral";
            String diff = (p.savedDifficulty != null && !p.savedDifficulty.isEmpty()) ? p.savedDifficulty : "Sakto";

            JLabel nameLbl = new JLabel(p.playerName + "  [ " + p.playerId + " ]" + (isCur ? "  ★ AKTIBO" : ""));
            nameLbl.setFont(new Font("Arial", Font.BOLD, 15));
            nameLbl.setForeground(isCur ? GamePalette.SPICY_SWEETCORN : GamePalette.CREAM);

            JLabel stageLbl = new JLabel("📍 " + map + " • Stage " + p.currentStage + ": " + enemyForStage);
            stageLbl.setFont(new Font("Arial", Font.BOLD, 12));
            stageLbl.setForeground(GamePalette.SKY_BLUE);

            JLabel statLbl = new JLabel("❤️ HP: " + p.currentHealth + "/" + p.maxHealth + " • Mode: " + diff + " • Coins: " + p.coins + " • " + (p.lastSaved != null ? p.lastSaved : ""));
            statLbl.setFont(new Font("Arial", Font.PLAIN, 11));
            statLbl.setForeground(GamePalette.SKY);

            infoPanel.add(nameLbl);
            infoPanel.add(stageLbl);
            infoPanel.add(statLbl);
            card.add(infoPanel, BorderLayout.CENTER);

            // Right: Continue Button
            FlatGameButton playBtn = new FlatGameButton("Ipadayon ▶", GamePalette.HONEY_GOLD, GamePalette.MIDNIGHT_NAVY, GamePalette.SPICY_SWEETCORN);
            playBtn.setFont(new Font("Arial", Font.BOLD, 13));
            playBtn.setPreferredSize(new Dimension(116, 36));
            playBtn.setCornerRadius(8);
            if (customCursor != null) playBtn.setCursor(customCursor);
            playBtn.addActionListener(e -> {
                dialog.dispose();
                resumeSelectedPlayerGame(p);
            });
            card.add(playBtn, BorderLayout.EAST);

            listPanel.add(card);
            listPanel.add(Box.createVerticalStrut(10));
        }

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        dialog.add(scroll, BorderLayout.CENTER);

        // Bottom: Close button
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomBar.setOpaque(false);
        bottomBar.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        FlatGameButton cancelBtn = new FlatGameButton("Balik (Close)", GamePalette.DUSK_NAVY, GamePalette.CREAM, GamePalette.SKY_BLUE);
        cancelBtn.setFont(new Font("Arial", Font.PLAIN, 13));
        cancelBtn.setPreferredSize(new Dimension(130, 34));
        cancelBtn.setCornerRadius(8);
        if (customCursor != null) cancelBtn.setCursor(customCursor);
        cancelBtn.addActionListener(e -> dialog.dispose());
        bottomBar.add(cancelBtn);

        dialog.add(bottomBar, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    public void startBattle(String mapName, int stageNum, String diffName) {
        if (currentPlayer != null) {
            Player fresh = dataManager.loadPlayer(currentPlayer.playerId);
            if (fresh != null) {
                currentPlayer = fresh;
            }
        }
        battlePanel.setupBattle(currentPlayer, mapName, stageNum, diffName);
        cardLayout.show(mainContainer, "BATTLE");
    }

    public void returnToMapSelect() {
        if (battlePanel != null) {
            battlePanel.saveGameState();
            Player bp = battlePanel.getCurrentPlayer();
            if (bp != null) {
                currentPlayer = bp;
            }
        }
        if (currentPlayer != null) {
            Player fresh = dataManager.loadPlayer(currentPlayer.playerId);
            if (fresh != null) {
                currentPlayer = fresh;
            }
        }
        cardLayout.show(mainContainer, "MAP_SELECT");
        updatePlayerCard();
        updateContinueButton();
    }

    public void saveGameOnExit() {
        if (battlePanel != null) {
            battlePanel.saveGameState();
            Player bp = battlePanel.getCurrentPlayer();
            if (bp != null) {
                currentPlayer = bp;
            }
        }
        if (currentPlayer != null) {
            currentPlayer.lastSaved = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            dataManager.savePlayer(currentPlayer);
        }
    }

    // Helper method to style plain text buttons consistently
    private void stylePlainMenuButton(JButton btn) {
        btn.setFont(new Font("Arial", Font.BOLD, 15));
        btn.setFocusPainted(false);
        btn.setBackground(GamePalette.DUSK_NAVY);
        btn.setForeground(GamePalette.CREAM);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(GamePalette.SKY_BLUE, 1, true),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        btn.setPreferredSize(new Dimension(192, 40));
        btn.setMaximumSize(new Dimension(192, 40));
        btn.setMinimumSize(new Dimension(192, 40));
        if (customCursor != null) {
            btn.setCursor(customCursor);
        }
    }

    // --- PLAYER SELECT MODAL DIALOG ---
    public void showPlayerSelectDialog(boolean isInitialRequired) {
        JDialog dialog = new JDialog(this, "Player Select", true);
        dialog.setSize(520, 480);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(GamePalette.DEEP_INDIGO);
        dialog.setLayout(new BorderLayout(12, 12));
        if (customCursor != null) {
            dialog.setCursor(customCursor);
        }

        // Top Header
        JPanel topPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 0, 20));

        JLabel titleLabel = new JLabel("PLAYER PROFILES", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(GamePalette.CREAM);

        JLabel subLabel = new JLabel("Pilia ang imong profile o paghimo ug bag-o para makadula", SwingConstants.CENTER);
        subLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subLabel.setForeground(GamePalette.SKY);

        topPanel.add(titleLabel);
        topPanel.add(subLabel);
        dialog.add(topPanel, BorderLayout.NORTH);

        // Center: Scrollable list of players
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);
        listPanel.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(GamePalette.DEEP_INDIGO);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        dialog.add(scrollPane, BorderLayout.CENTER);

        // Refresh helper
        Runnable refreshList = new Runnable() {
            @Override
            public void run() {
                listPanel.removeAll();
                List<Player> players = dataManager.getAllPlayers();
                if (players.isEmpty()) {
                    JPanel emptyBox = new JPanel(new GridLayout(2, 1, 0, 8));
                    emptyBox.setOpaque(false);
                    emptyBox.setBorder(BorderFactory.createEmptyBorder(60, 20, 40, 20));

                    JLabel emptyTitle = new JLabel("Walay nakit-an nga player profile.", SwingConstants.CENTER);
                    emptyTitle.setFont(new Font("Arial", Font.BOLD, 15));
                    emptyTitle.setForeground(GamePalette.CREAM);

                    JLabel emptyHint = new JLabel("I-klik ang '+ Bag-ong Player' sa ubos para makasugod!", SwingConstants.CENTER);
                    emptyHint.setFont(new Font("Arial", Font.PLAIN, 13));
                    emptyHint.setForeground(GamePalette.SKY);

                    emptyBox.add(emptyTitle);
                    emptyBox.add(emptyHint);
                    listPanel.add(emptyBox);
                } else {
                    for (Player p : players) {
                        boolean isActive = (currentPlayer != null && p.playerId.equals(currentPlayer.playerId));
                        listPanel.add(createPlayerRow(p, isActive, dialog, this));
                        listPanel.add(Box.createVerticalStrut(10));
                    }
                }
                listPanel.revalidate();
                listPanel.repaint();
            }
        };

        refreshList.run();

        // Bottom Controls: "+ Bag-ong Player" and "Close"
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 12));
        bottomPanel.setOpaque(false);

        FlatGameButton newPlayerBtn = new FlatGameButton("+ Bag-ong Player (New Player)", GamePalette.CERULEAN, GamePalette.MIDNIGHT_NAVY, GamePalette.SKY_BLUE);
        newPlayerBtn.setFont(new Font("Arial", Font.BOLD, 13));
        newPlayerBtn.setPreferredSize(new Dimension(240, 36));
        newPlayerBtn.setCornerRadius(10);
        if (customCursor != null) newPlayerBtn.setCursor(customCursor);

        newPlayerBtn.addActionListener(e -> {
            String name = (String) JOptionPane.showInputDialog(
                dialog,
                "Ibutang ang ngalan sa bag-ong player (Enter player name):",
                "Bag-ong Player",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                ""
            );
            if (name != null) {
                name = name.trim();
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Dili pwede blangko ang ngalan.", "Pahibalo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                name = name.replace(",", " ");
                String newId = dataManager.nextPlayerId();
                Player newP = new Player(newId, name);
                newP.lastSaved = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                dataManager.savePlayer(newP);
                dataManager.saveSettings(new DataManager.GameSettings(newId, true, true, 70));
                dataManager.saveUnlocks(new DataManager.Unlocks(newId));

                currentPlayer = newP;
                updatePlayerCard();
                dialog.dispose();
            }
        });

        FlatGameButton closeBtn = new FlatGameButton("Isara (Close)", GamePalette.DUSK_NAVY, GamePalette.CREAM, GamePalette.SKY_BLUE);
        closeBtn.setFont(new Font("Arial", Font.PLAIN, 13));
        closeBtn.setPreferredSize(new Dimension(110, 36));
        closeBtn.setCornerRadius(10);
        if (customCursor != null) closeBtn.setCursor(customCursor);
        closeBtn.addActionListener(e -> {
            if (currentPlayer == null && !dataManager.getAllPlayers().isEmpty()) {
                currentPlayer = dataManager.loadLatestPlayer();
                updatePlayerCard();
            }
            if (currentPlayer == null) {
                JOptionPane.showMessageDialog(dialog, "Gikinahanglan ang player profile para makadula.", "Pahibalo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            dialog.dispose();
        });

        bottomPanel.add(newPlayerBtn);
        bottomPanel.add(closeBtn);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        // Window closing safety (if closed without creating when empty)
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (currentPlayer == null) {
                    List<Player> all = dataManager.getAllPlayers();
                    if (!all.isEmpty()) {
                        currentPlayer = dataManager.loadLatestPlayer();
                    } else {
                        // Create default Juan profile if closed without creating
                        String defId = dataManager.nextPlayerId();
                        Player def = new Player(defId, "Juan");
                        def.lastSaved = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                        dataManager.savePlayer(def);
                        dataManager.saveSettings(new DataManager.GameSettings(defId, true, true, 70));
                        dataManager.saveUnlocks(new DataManager.Unlocks(defId));
                        currentPlayer = def;
                    }
                    updatePlayerCard();
                }
            }
        });

        dialog.setVisible(true);
    }

    private JPanel createPlayerRow(Player p, boolean isActive, JDialog dialog, Runnable onRefresh) {
        JPanel row = new JPanel(new BorderLayout(12, 6));
        row.setOpaque(true);
        row.setBackground(isActive ? GamePalette.DUSK_NAVY : GamePalette.DEEP_INDIGO);
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(isActive ? GamePalette.SPICY_SWEETCORN : GamePalette.DUSK_NAVY, isActive ? 2 : 1, true),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));

        // Left Avatar
        Image sprite = loadGameImage("Main Character Idle_1.png");
        JLabel avatar;
        if (sprite != null) {
            avatar = new JLabel(new ImageIcon(sprite.getScaledInstance(36, 36, Image.SCALE_SMOOTH)));
        } else {
            avatar = new JLabel("👤");
            avatar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
            avatar.setForeground(GamePalette.CREAM);
        }
        row.add(avatar, BorderLayout.WEST);

        // Center Info
        JPanel info = new JPanel(new GridLayout(2, 1, 0, 2));
        info.setOpaque(false);

        JPanel nameLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        nameLine.setOpaque(false);
        JLabel nameLbl = new JLabel(p.playerName);
        nameLbl.setFont(new Font("Arial", Font.BOLD, 15));
        nameLbl.setForeground(GamePalette.CREAM);

        JLabel idLbl = new JLabel("(" + p.playerId + ")");
        idLbl.setFont(new Font("Arial", Font.PLAIN, 12));
        idLbl.setForeground(GamePalette.SKY);

        nameLine.add(nameLbl);
        nameLine.add(idLbl);

        if (isActive) {
            JLabel activeBadge = new JLabel("[ACTIVE]");
            activeBadge.setFont(new Font("Arial", Font.BOLD, 11));
            activeBadge.setForeground(GamePalette.SPICY_SWEETCORN);
            nameLine.add(activeBadge);
        }

        String lastTime = (p.lastSaved != null && !p.lastSaved.isEmpty()) ? p.lastSaved : "Bag-o";
        JLabel statsLbl = new JLabel("Stage " + p.currentStage + " (Round " + p.currentRound + ") • HP: " + p.currentHealth + "/" + p.maxHealth + " • Coins: " + p.coins + " • " + lastTime);
        statsLbl.setFont(new Font("Arial", Font.PLAIN, 11));
        statsLbl.setForeground(GamePalette.SKY);

        info.add(nameLine);
        info.add(statsLbl);
        row.add(info, BorderLayout.CENTER);

        // Right Actions (Select & Delete)
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        actionPanel.setOpaque(false);

        if (!isActive) {
            FlatGameButton selectBtn = new FlatGameButton("Pilia (Select)", GamePalette.CERULEAN, GamePalette.MIDNIGHT_NAVY, GamePalette.SKY_BLUE);
            selectBtn.setFont(new Font("Arial", Font.BOLD, 12));
            selectBtn.setPreferredSize(new Dimension(108, 30));
            selectBtn.setCornerRadius(8);
            if (customCursor != null) selectBtn.setCursor(customCursor);
            selectBtn.addActionListener(e -> {
                currentPlayer = p;
                p.lastSaved = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                dataManager.savePlayer(p);
                updatePlayerCard();
                dialog.dispose();
            });
            actionPanel.add(selectBtn);
        }

        FlatGameButton deleteBtn = new FlatGameButton("✕", GamePalette.PUMPKIN_ORANGE, GamePalette.CREAM, GamePalette.DUSK_NAVY);
        deleteBtn.setFont(new Font("Arial", Font.BOLD, 12));
        deleteBtn.setPreferredSize(new Dimension(36, 30));
        deleteBtn.setCornerRadius(8);
        deleteBtn.setToolTipText("Papasa kini nga profile");
        if (customCursor != null) deleteBtn.setCursor(customCursor);
        deleteBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                dialog,
                "Sigurado ka nga papason si \"" + p.playerName + "\" (" + p.playerId + ")?",
                "Kumpirmaha ang Pagpapas",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            if (confirm == JOptionPane.YES_OPTION) {
                dataManager.deletePlayer(p.playerId);
                if (currentPlayer != null && currentPlayer.playerId.equals(p.playerId)) {
                    currentPlayer = dataManager.loadLatestPlayer();
                    updatePlayerCard();
                }
                onRefresh.run();
            }
        });
        actionPanel.add(deleteBtn);

        row.add(actionPanel, BorderLayout.EAST);
        return row;
    }

    // Helper method to load images safely from root or classpath
    public Image loadGameImage(String filename) {
        try {
            File f = new File(filename);
            if (f.exists()) {
                return ImageIO.read(f);
            }
            java.net.URL url = getClass().getResource("/juanword/" + filename);
            if (url != null) {
                return ImageIO.read(url);
            }
            url = getClass().getResource("/" + filename);
            if (url != null) {
                return ImageIO.read(url);
            }
        } catch (Exception ignored) {}
        return null;
    }

    // Custom button for map tiles with hover expansion effect and locked state
    private class MapTileButton extends JButton {
        private boolean isHovered = false;
        private final String title;
        private final String subtitle;
        private final boolean isUnlocked;

        public MapTileButton(String title, String subtitle, boolean isUnlocked) {
            this.title = title;
            this.subtitle = subtitle;
            this.isUnlocked = isUnlocked;
            updateContent();
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            if (isUnlocked && customCursor != null) {
                setCursor(customCursor);
            }

            if (isUnlocked) {
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        isHovered = true;
                        updateContent();
                        repaint();
                    }

                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        isHovered = false;
                        updateContent();
                        repaint();
                    }
                });
            }
        }

        // Backward compatibility constructor
        public MapTileButton(String title, String subtitle) {
            this(title, subtitle, true);
        }

        private void updateContent() {
            if (!isUnlocked) {
                setText("<html><center><font size='4' color='#6C7293'><b>" + title + "</b></font><br>"
                        + "<font size='2' color='#4A4E6B'>" + subtitle + "</font><br>"
                        + "<font size='2' color='#D36F02'>🔒 <b>Naka-lock</b></font></center></html>");
            } else if (isHovered) {
                setText("<html><center><font size='5' color='#FFF8E9'><b>" + title + "</b></font><br>"
                        + "<font size='3' color='#9EE6FE'>" + subtitle + "</font><br>"
                        + "<font size='2' color='#F5B820'>⭐ <b>3 ka Yugto (Dizon, Alviola, Rola)</b></font></center></html>");
            } else {
                setText("<html><center><font size='4' color='#FFF8E9'><b>" + title + "</b></font><br>"
                        + "<font size='2' color='#9EE6FE'>" + subtitle + "</font><br>"
                        + "<font size='2' color='#387940'>✔ <b>Nabuksan (3 ka Yugto)</b></font></center></html>");
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (!isUnlocked) {
                // Grayed-out locked map card: dark desaturated navy
                int pad = 10;
                int x = pad;
                int y = pad;
                int w = getWidth() - (pad * 2);
                int h = getHeight() - (pad * 2);

                g2.setColor(new Color(11, 11, 32));
                g2.fillRoundRect(x, y, w, h, 16, 16);

                g2.setColor(new Color(25, 27, 56));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(x, y, w, h, 16, 16);
            } else {
                // Normal: 10px margin. Hovered: expands outward to 2px margin.
                int pad = isHovered ? 2 : 10;
                int x = pad;
                int y = pad;
                int w = getWidth() - (pad * 2);
                int h = getHeight() - (pad * 2);

                // Background card: Deep Indigo normal, Dusk Navy hovered
                g2.setColor(isHovered ? GamePalette.DUSK_NAVY : GamePalette.DEEP_INDIGO);
                g2.fillRoundRect(x, y, w, h, 16, 16);

                // Border: Cerulean hovered, Dusk Navy normal
                if (isHovered) {
                    g2.setColor(GamePalette.CERULEAN);
                    g2.setStroke(new BasicStroke(2.5f));
                } else {
                    g2.setColor(GamePalette.DUSK_NAVY);
                    g2.setStroke(new BasicStroke(1.5f));
                }
                g2.drawRoundRect(x, y, w, h, 16, 16);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Helper method to remove default button borders and backgrounds for pixel art
    private void styleImageButton(JButton button) {
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        if (customCursor != null) {
            button.setCursor(customCursor);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JuanWord game = new JuanWord();
            game.setVisible(true);
            if (game.currentPlayer == null) {
                game.showPlayerSelectDialog(true);
            }
        });
    }
}
