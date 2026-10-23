package juanword;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.imageio.ImageIO;

public class JuanWord extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainContainer;
    private Image backgroundImage;
    private Cursor customCursor;
    private final DataManager dataManager = new DataManager();
    private Player currentPlayer;

    // Top-left Player Card components
    private JLabel playerNameLabel;
    private JLabel playerInfoLabel;
    private JPanel playerCardPanel;

    // Menu buttons
    private JButton continueButton;

    public JuanWord() {
        setTitle("Juan Word");
        setSize(960, 540); // 16:9 aspect ratio
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center window on screen

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

        // CardLayout allows switching between screens (Start Screen, Map Select, etc.)
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        mainContainer.add(createStartScreen(), "START");
        mainContainer.add(createMapSelectScreen(), "MAP_SELECT");

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
        continueButton = new JButton("CONTINUE");
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
        JButton exitButton = new JButton("EXIT");
        stylePlainMenuButton(exitButton);
        exitButton.addActionListener(e -> System.exit(0));

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
                g.setColor(new Color(255, 255, 255, 210));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mapPanel.setLayout(new BorderLayout(20, 20));
        mapPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 30, 30));

        // Top Header bar
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JButton backButton = new JButton("← Balik");
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setFocusPainted(false);
        if (customCursor != null) {
            backButton.setCursor(customCursor);
        }
        backButton.addActionListener(e -> {
            updateContinueButton();
            cardLayout.show(mainContainer, "START");
        });

        JLabel titleLabel = new JLabel("MAP SELECT", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(40, 40, 50));

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
            int stageNum = i + 1;

            MapTileButton mapBtn = new MapTileButton(mapName, mapDesc);
            mapBtn.addActionListener(e -> showDifficultyDialog(mapName, stageNum));

            gridPanel.add(mapBtn);
        }

        mapPanel.add(gridPanel, BorderLayout.CENTER);
        return mapPanel;
    }

    // Modal dialog to choose difficulty for the selected map
    private void showDifficultyDialog(String mapName, int stageNum) {
        JDialog dialog = new JDialog(this, "Select Difficulty", true);
        dialog.setSize(360, 290);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(new Color(245, 246, 250));
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
        titleLabel.setForeground(new Color(30, 30, 40));

        JLabel mapLabel = new JLabel("Map: " + mapName + " (Stage " + stageNum + ")", SwingConstants.CENTER);
        mapLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        mapLabel.setForeground(new Color(90, 95, 110));

        topPanel.add(titleLabel);
        topPanel.add(mapLabel);
        dialog.add(topPanel, BorderLayout.NORTH);

        // Difficulty options: Sayon, Sakto, Lisod
        JPanel buttonsPanel = new JPanel(new GridLayout(3, 1, 0, 10));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(5, 40, 15, 40));

        String[][] difficulties = {
            {"Sayon", "#388E3C"},     // Green
            {"Sakto", "#F57C00"},     // Orange
            {"Lisod", "#D32F2F"}      // Red
        };

        for (String[] diff : difficulties) {
            String diffName = diff[0];
            Color btnColor = Color.decode(diff[1]);

            JButton diffBtn = new JButton(diffName);
            diffBtn.setFont(new Font("Arial", Font.BOLD, 14));
            diffBtn.setBackground(btnColor);
            diffBtn.setFocusPainted(false);
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
                Player p = currentPlayer;
                String pInfo = (p != null) ? "\nPlayer: " + p.playerName + " (Stage " + p.currentStage + ", Coins: " + p.coins + ")" : "";
                JOptionPane.showMessageDialog(this, "Nagsugod na ang Juan Word!\n\nMap: " + mapName + " (Stage " + stageNum + ")\nDifficulty: " + diffName + pInfo);
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
        dialog.getContentPane().setBackground(new Color(245, 246, 250));
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
        title.setForeground(new Color(30, 30, 40));
        JLabel sub = new JLabel("Configured in data/settings.csv (" + pid + ")", SwingConstants.CENTER);
        sub.setFont(new Font("Arial", Font.PLAIN, 12));
        sub.setForeground(Color.GRAY);
        top.add(title);
        top.add(sub);
        dialog.add(top, BorderLayout.NORTH);

        // Controls
        JPanel center = new JPanel(new GridLayout(3, 1, 0, 10));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(5, 40, 10, 40));

        JCheckBox musicCheck = new JCheckBox("Music", current.musicOn);
        musicCheck.setFont(new Font("Arial", Font.BOLD, 14));
        musicCheck.setOpaque(false);
        if (customCursor != null) musicCheck.setCursor(customCursor);

        JCheckBox sfxCheck = new JCheckBox("Sound Effects", current.sfxOn);
        sfxCheck.setFont(new Font("Arial", Font.BOLD, 14));
        sfxCheck.setOpaque(false);
        if (customCursor != null) sfxCheck.setCursor(customCursor);

        JPanel volPanel = new JPanel(new BorderLayout(5, 5));
        volPanel.setOpaque(false);
        JLabel volLabel = new JLabel("Volume: " + current.volume + "%");
        volLabel.setFont(new Font("Arial", Font.BOLD, 13));
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
        JButton saveBtn = new JButton("Save & Close");
        saveBtn.setFont(new Font("Arial", Font.BOLD, 14));
        saveBtn.setBackground(new Color(60, 110, 220));
        saveBtn.setFocusPainted(false);
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

                // Translucent pill card background
                g2.setColor(isHovered ? new Color(30, 36, 55, 235) : new Color(18, 22, 35, 205));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);

                // Subtle gold or soft-white border
                g2.setColor(isHovered ? new Color(240, 192, 64, 230) : new Color(255, 255, 255, 75));
                g2.setStroke(new BasicStroke(isHovered ? 2.0f : 1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 22, 22);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 14));
        card.setPreferredSize(new Dimension(210, 48));
        card.setMaximumSize(new Dimension(220, 50));
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
            avatarLabel.setForeground(Color.WHITE);
        }
        if (customCursor != null) avatarLabel.setCursor(customCursor);
        card.add(avatarLabel, BorderLayout.WEST);

        // Player Text info in center
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 1));
        textPanel.setOpaque(false);
        if (customCursor != null) textPanel.setCursor(customCursor);

        playerNameLabel = new JLabel("Player");
        playerNameLabel.setFont(new Font("Arial", Font.BOLD, 13));
        playerNameLabel.setForeground(new Color(240, 192, 64)); // JuanWord Gold

        playerInfoLabel = new JLabel("Stage 1 • Switch ▾");
        playerInfoLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        playerInfoLabel.setForeground(new Color(210, 215, 225));

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
            playerInfoLabel.setText("Stage " + currentPlayer.currentStage + " • " + currentPlayer.coins + " Coins ▾");
        } else {
            playerNameLabel.setText("Walay Player");
            playerInfoLabel.setText("Pilia o Paghimo ▾");
        }
        updateContinueButton();
    }

    // Check if an active game run exists for the player
    public boolean hasActiveGame(Player p) {
        if (p == null) return false;
        // An active game is any run where a stage/round was started (currentRound >= 1) or progress was made
        return p.currentRound > 0 || p.score > 0 || p.currentStage > 1;
    }

    // Refresh Continue button state (enabled vs grayed-out)
    private void updateContinueButton() {
        if (continueButton == null) return;
        boolean hasActive = hasActiveGame(currentPlayer);
        continueButton.setEnabled(hasActive);
        if (hasActive) {
            continueButton.setToolTipText("Ipadayon ang dula (Stage " + currentPlayer.currentStage + ", Round " + currentPlayer.currentRound + ", Score " + currentPlayer.score + ")");
            if (customCursor != null) {
                continueButton.setCursor(customCursor);
            }
        } else {
            continueButton.setToolTipText("Walay aktibo nga dula (No active game in progress)");
            continueButton.setCursor(Cursor.getDefaultCursor());
        }
    }

    // Handler for START button: if active game exists, ask confirmation before resetting
    private void handleStartGame() {
        if (hasActiveGame(currentPlayer)) {
            String msg = "Naa kay kasamtangang aktibo nga dula alang kang " + currentPlayer.playerName + ":\n"
                    + "  • Stage " + currentPlayer.currentStage + ", Round " + currentPlayer.currentRound + "\n"
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
            currentPlayer.lastSaved = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            dataManager.savePlayer(currentPlayer);
            updatePlayerCard();
            updateContinueButton();
        }

        // Proceed to map select
        cardLayout.show(mainContainer, "MAP_SELECT");
    }

    // Handler for CONTINUE button: resume active game directly
    private void handleContinueGame() {
        if (currentPlayer != null) {
            Player fresh = dataManager.loadPlayer(currentPlayer.playerId);
            if (fresh != null) {
                currentPlayer = fresh;
                updatePlayerCard();
            }
        }
        if (!hasActiveGame(currentPlayer)) return;
        cardLayout.show(mainContainer, "MAP_SELECT");
    }

    // Helper method to style plain text buttons consistently
    private void stylePlainMenuButton(JButton btn) {
        btn.setFont(new Font("Arial", Font.BOLD, 15));
        btn.setFocusPainted(false);
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
        dialog.getContentPane().setBackground(new Color(245, 246, 250));
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
        titleLabel.setForeground(new Color(30, 30, 40));

        JLabel subLabel = new JLabel("Pilia ang imong profile o paghimo ug bag-o para makadula", SwingConstants.CENTER);
        subLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subLabel.setForeground(new Color(100, 105, 120));

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
        scrollPane.getViewport().setBackground(new Color(245, 246, 250));
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
                    emptyTitle.setForeground(new Color(120, 125, 140));

                    JLabel emptyHint = new JLabel("I-klik ang '+ Bag-ong Player' sa ubos para makasugod!", SwingConstants.CENTER);
                    emptyHint.setFont(new Font("Arial", Font.PLAIN, 13));
                    emptyHint.setForeground(new Color(140, 145, 160));

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

        JButton newPlayerBtn = new JButton("+ Bag-ong Player (New Player)");
        newPlayerBtn.setFont(new Font("Arial", Font.BOLD, 13));
        newPlayerBtn.setForeground(new Color(25, 120, 50));
        newPlayerBtn.setFocusPainted(false);
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

        JButton closeBtn = new JButton("Isara (Close)");
        closeBtn.setFont(new Font("Arial", Font.PLAIN, 13));
        closeBtn.setFocusPainted(false);
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
        row.setBackground(isActive ? new Color(236, 248, 238) : Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(isActive ? new Color(56, 142, 60) : new Color(215, 220, 230), isActive ? 2 : 1, true),
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
        }
        row.add(avatar, BorderLayout.WEST);

        // Center Info
        JPanel info = new JPanel(new GridLayout(2, 1, 0, 2));
        info.setOpaque(false);

        JPanel nameLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        nameLine.setOpaque(false);
        JLabel nameLbl = new JLabel(p.playerName);
        nameLbl.setFont(new Font("Arial", Font.BOLD, 15));
        nameLbl.setForeground(new Color(30, 30, 45));

        JLabel idLbl = new JLabel("(" + p.playerId + ")");
        idLbl.setFont(new Font("Arial", Font.PLAIN, 12));
        idLbl.setForeground(Color.GRAY);

        nameLine.add(nameLbl);
        nameLine.add(idLbl);

        if (isActive) {
            JLabel activeBadge = new JLabel("[ACTIVE]");
            activeBadge.setFont(new Font("Arial", Font.BOLD, 11));
            activeBadge.setForeground(new Color(46, 125, 50));
            nameLine.add(activeBadge);
        }

        String lastTime = (p.lastSaved != null && !p.lastSaved.isEmpty()) ? p.lastSaved : "Bag-o";
        JLabel statsLbl = new JLabel("Stage " + p.currentStage + " (Round " + p.currentRound + ") • Score: " + p.score + " • Coins: " + p.coins + " • " + lastTime);
        statsLbl.setFont(new Font("Arial", Font.PLAIN, 11));
        statsLbl.setForeground(new Color(90, 95, 110));

        info.add(nameLine);
        info.add(statsLbl);
        row.add(info, BorderLayout.CENTER);

        // Right Actions (Select & Delete)
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        actionPanel.setOpaque(false);

        if (!isActive) {
            JButton selectBtn = new JButton("Pilia (Select)");
            selectBtn.setFont(new Font("Arial", Font.BOLD, 12));
            selectBtn.setForeground(new Color(30, 90, 200));
            selectBtn.setFocusPainted(false);
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

        JButton deleteBtn = new JButton("✕");
        deleteBtn.setFont(new Font("Arial", Font.BOLD, 12));
        deleteBtn.setToolTipText("Papasa kini nga profile");
        deleteBtn.setForeground(new Color(180, 50, 50));
        deleteBtn.setFocusPainted(false);
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
    private Image loadGameImage(String filename) {
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

    // Custom button for map tiles with hover expansion effect
    private class MapTileButton extends JButton {
        private boolean isHovered = false;
        private final String title;
        private final String subtitle;

        public MapTileButton(String title, String subtitle) {
            this.title = title;
            this.subtitle = subtitle;
            updateContent();
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            if (customCursor != null) {
                setCursor(customCursor);
            }

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

        private void updateContent() {
            if (isHovered) {
                setText("<html><center><font size='5'><b>" + title + "</b></font><br><font size='3' color='#222222'>" + subtitle + "</font></center></html>");
            } else {
                setText("<html><center><font size='4'><b>" + title + "</b></font><br><font size='2' color='#666666'>" + subtitle + "</font></center></html>");
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Normal: 10px margin. Hovered: expands outward to 2px margin.
            int pad = isHovered ? 2 : 10;
            int x = pad;
            int y = pad;
            int w = getWidth() - (pad * 2);
            int h = getHeight() - (pad * 2);

            // Background card (pure white on hover, soft light gray normally)
            g2.setColor(isHovered ? Color.WHITE : new Color(240, 242, 247));
            g2.fillRoundRect(x, y, w, h, 16, 16);

            // Border (accent color on hover)
            if (isHovered) {
                g2.setColor(new Color(60, 110, 220));
                g2.setStroke(new BasicStroke(2.5f));
            } else {
                g2.setColor(new Color(205, 210, 220));
                g2.setStroke(new BasicStroke(1.5f));
            }
            g2.drawRoundRect(x, y, w, h, 16, 16);

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
