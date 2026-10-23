package juanword;

import javax.swing.*;
import java.awt.*;

public class JuanWord extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainContainer;
    private Image backgroundImage;

    public JuanWord() {
        setTitle("Juan Word");
        setSize(960, 540); // 16:9 aspect ratio
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center window on screen

        // Load background image
        backgroundImage = new ImageIcon("Background draft.png").getImage();

        // CardLayout allows switching between screens (Start Screen, Map Select, etc.)
        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        mainContainer.add(createStartScreen(), "START");
        mainContainer.add(createMapSelectScreen(), "MAP_SELECT");

        add(mainContainer);
    }

    // --- START SCREEN ---
    private JPanel createStartScreen() {
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            }
        };
        backgroundPanel.setLayout(new GridBagLayout());

        // Buttons
        ImageIcon startIcon = new ImageIcon("Start Button.png");
        JButton startButton = new JButton(startIcon);
        styleImageButton(startButton);
        startButton.addActionListener(e -> cardLayout.show(mainContainer, "MAP_SELECT"));

        ImageIcon settingIcon = new ImageIcon("Setting Button.png");
        JButton settingButton = new JButton(settingIcon);
        styleImageButton(settingButton);
        settingButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Opening Settings..."));

        // Stack buttons vertically
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 20));
        buttonPanel.setOpaque(false);
        buttonPanel.add(startButton);
        buttonPanel.add(settingButton);

        // Position buttons towards the bottom
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.insets = new Insets(0, 0, 50, 0); // 50px space from the bottom

        backgroundPanel.add(buttonPanel, gbc);
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
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.addActionListener(e -> cardLayout.show(mainContainer, "START"));

        JLabel titleLabel = new JLabel("MAP SELECT", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new Color(40, 40, 50));

        // Empty label on right to balance the back button and keep title centered
        JLabel placeholder = new JLabel();
        placeholder.setPreferredSize(backButton.getPreferredSize());

        headerPanel.add(backButton, BorderLayout.WEST);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        headerPanel.add(placeholder, BorderLayout.EAST);
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

        for (String[] map : maps) {
            String mapName = map[0];
            String mapDesc = map[1];

            MapTileButton mapBtn = new MapTileButton(mapName, mapDesc);
            mapBtn.addActionListener(e -> showDifficultyDialog(mapName));

            gridPanel.add(mapBtn);
        }

        mapPanel.add(gridPanel, BorderLayout.CENTER);
        return mapPanel;
    }

    // Modal dialog to choose difficulty for the selected map
    private void showDifficultyDialog(String mapName) {
        JDialog dialog = new JDialog(this, "Select Difficulty", true);
        dialog.setSize(360, 290);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(new Color(245, 246, 250));
        dialog.setLayout(new BorderLayout(15, 15));

        // Header info
        JPanel topPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        topPanel.setOpaque(false);
        topPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 0, 15));

        JLabel titleLabel = new JLabel("PILI UG KALISOD (DIFFICULTY)", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 17));
        titleLabel.setForeground(new Color(30, 30, 40));

        JLabel mapLabel = new JLabel("Lugar: " + mapName, SwingConstants.CENTER);
        mapLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        mapLabel.setForeground(new Color(90, 95, 110));

        topPanel.add(titleLabel);
        topPanel.add(mapLabel);
        dialog.add(topPanel, BorderLayout.NORTH);

        // Difficulty options: Easy, Medium, Hard
        JPanel buttonsPanel = new JPanel(new GridLayout(3, 1, 0, 10));
        buttonsPanel.setOpaque(false);
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(5, 40, 15, 40));

        String[][] difficulties = {
            {"Sayon (Easy)", "#388E3C"},     // Green
            {"Sakto (Medium)", "#F57C00"},   // Orange
            {"Lisod (Hard)", "#D32F2F"}      // Red
        };

        for (String[] diff : difficulties) {
            String diffName = diff[0];
            Color btnColor = Color.decode(diff[1]);

            JButton diffBtn = new JButton(diffName);
            diffBtn.setFont(new Font("Arial", Font.BOLD, 14));
            diffBtn.setBackground(btnColor);
            diffBtn.setFocusPainted(false);
            diffBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            diffBtn.addActionListener(e -> {
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Nagsugod na ang Juan Word!\n\nLugar: " + mapName + "\nKalisod: " + diffName);
            });

            buttonsPanel.add(diffBtn);
        }

        dialog.add(buttonsPanel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    // Custom button for map tiles with hover expansion effect
    private static class MapTileButton extends JButton {
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
            setCursor(new Cursor(Cursor.HAND_CURSOR));

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
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new JuanWord().setVisible(true);
        });
    }
}
