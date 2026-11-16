package juanword.ui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import juanword.JuanWord;
import juanword.Screen;
import juanword.data.DataManager;
import juanword.model.Player;

public final class StartPanel extends JPanel {
    private static final Color GOLD = new Color(255, 200, 50);
    private static final Color SKY_BLUE = new Color(77, 166, 197);
    private static final Color DUSK_NAVY = new Color(31, 32, 88);
    private static final Color DEEP_INDIGO = new Color(14, 15, 42, 230);
    private static final Color SEQUESTA = new Color(211, 205, 86);

    private final JuanWord host;
    private List<Player> profiles = new ArrayList<>();

    // Menu Screen Art Layers
    private final BufferedImage skyBg;
    private final BufferedImage mtApo;
    private final BufferedImage indigFrame;
    private final BufferedImage titleLogo;
    private final BufferedImage avatarImg;

    // Buttons
    private final ImageAssetButton continueBtn;
    private final ImageAssetButton newGameBtn;
    private final ImageAssetButton settingBtn;
    private final ImageAssetButton exitBtn;

    // Player Card Bounds & State
    private final Rectangle playerCardBounds = new Rectangle(36, 24, 230, 48);
    private boolean playerCardHovered = false;

    public StartPanel(JuanWord host) {
        this.host = host;
        setLayout(null);
        setOpaque(true);
        setBackground(new Color(14, 15, 42));

        // Load Authentic Assets
        this.skyBg = ImageLoader.load("bg.png");
        this.mtApo = ImageLoader.load("mtapo.png");
        this.indigFrame = ImageLoader.load("indigframe.png");

        BufferedImage rawTitle = ImageLoader.load("Title.png");
        if (rawTitle != null && rawTitle.getWidth() == 1920 && rawTitle.getHeight() == 1080) {
            this.titleLogo = rawTitle.getSubimage(386, 243, 1162, 585);
        } else {
            this.titleLogo = rawTitle;
        }

        BufferedImage av = ImageLoader.load("maincharacteridle.png");
        if (av == null || ImageLoader.isPlaceholder(av)) {
            av = ImageLoader.load("Main Character Idle_1.png");
        }
        this.avatarImg = av;

        // Button Sprites
        BufferedImage contImg = ImageLoader.load("continuebtn.png");
        BufferedImage newGameImg = ImageLoader.load("newgamebtn.png");
        if (newGameImg == null || ImageLoader.isPlaceholder(newGameImg)) {
            newGameImg = ImageLoader.load("startbtn.png");
        }
        BufferedImage setImg = ImageLoader.load("settingbtn.png");
        BufferedImage exitImg = ImageLoader.load("exitbtn.png");

        continueBtn = new ImageAssetButton(contImg);
        newGameBtn = new ImageAssetButton(newGameImg);
        settingBtn = new ImageAssetButton(setImg);
        exitBtn = new ImageAssetButton(exitImg);

        // Responsive button placement at 960x540
        int btnW = 192;
        int btnH = 48;
        int gap = 6;
        int bottomMargin = 26;
        int totalBtnH = (btnH * 4) + (gap * 3);
        int startY = 540 - bottomMargin - totalBtnH;
        int btnX = (960 - btnW) / 2;

        continueBtn.setBounds(btnX, startY, btnW, btnH);
        newGameBtn.setBounds(btnX, startY + (btnH + gap), btnW, btnH);
        settingBtn.setBounds(btnX, startY + (btnH + gap) * 2, btnW, btnH);
        exitBtn.setBounds(btnX, startY + (btnH + gap) * 3, btnW, btnH);

        continueBtn.addActionListener(e -> onContinue());
        newGameBtn.addActionListener(e -> onNewProfile());
        settingBtn.addActionListener(e -> showProfileSelectionDialog());
        exitBtn.addActionListener(e -> onQuit());

        add(continueBtn);
        add(newGameBtn);
        add(settingBtn);
        add(exitBtn);

        // Mouse interactions for Top-Left Player Profile Card
        MouseAdapter playerCardAdapter = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                boolean wasHovered = playerCardHovered;
                playerCardHovered = playerCardBounds.contains(e.getPoint());
                if (wasHovered != playerCardHovered) {
                    setCursor(playerCardHovered ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
                    repaint(playerCardBounds);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (playerCardHovered) {
                    playerCardHovered = false;
                    setCursor(Cursor.getDefaultCursor());
                    repaint(playerCardBounds);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (playerCardBounds.contains(e.getPoint())) {
                    showProfileSelectionDialog();
                }
            }
        };
        addMouseListener(playerCardAdapter);
        addMouseMotionListener(playerCardAdapter);

        refreshProfiles();
    }

    public void refreshProfiles() {
        profiles = DataManager.getInstance().getAllPlayers();
        if (!profiles.isEmpty() && host.getActivePlayer() == null) {
            host.setActivePlayer(profiles.get(0));
        }
        boolean hasPlayer = (host.getActivePlayer() != null || !profiles.isEmpty());
        continueBtn.setEnabled(hasPlayer);
        repaint();
    }

    private void onContinue() {
        Player active = host.getActivePlayer();
        if (active != null) {
            if (active.hasActiveBattle()) {
                host.resumeBattle(active);
            } else {
                host.showScreen(Screen.MAP_SELECT);
            }
        } else if (!profiles.isEmpty()) {
            Player p = profiles.get(0);
            host.setActivePlayer(p);
            if (p.hasActiveBattle()) {
                host.resumeBattle(p);
            } else {
                host.showScreen(Screen.MAP_SELECT);
            }
        } else {
            onNewProfile();
        }
    }

    private void onNewProfile() {
        String name = JOptionPane.showInputDialog(
            this,
            "Ibutang ang ngalan sa bag-ong magdudula:",
            "Bag-ong Profile",
            JOptionPane.PLAIN_MESSAGE
        );
        if (name != null) {
            name = name.trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Palihug ibutang ang ngalan!", "Pahibalo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (name.length() > 20) {
                name = name.substring(0, 20);
            }
            Player p = DataManager.getInstance().createPlayer(name);
            host.setActivePlayer(p);
            refreshProfiles();
            host.showScreen(Screen.MAP_SELECT);
        }
    }

    public void showProfileSelectionDialog() {
        JDialog dlg = new JDialog(host, "Pilia ang Profile (Select Account)", true);
        dlg.setSize(420, 360);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(8, 8));

        DefaultListModel<Player> model = new DefaultListModel<>();
        for (Player p : profiles) model.addElement(p);

        JList<Player> list = new JList<>(model);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object val, int idx, boolean isSel, boolean focus) {
                JLabel lbl = (JLabel) super.getListCellRendererComponent(l, val, idx, isSel, focus);
                if (val instanceof Player pl) {
                    lbl.setText("  👤 " + pl.getPlayerName() + "  |  Mapa " + pl.getUnlockedMap() + "  |  Score: " + pl.getScore()
                        + (pl.hasActiveBattle() ? " [Gubat Naabli]" : ""));
                }
                return lbl;
            }
        });

        if (host.getActivePlayer() != null) {
            list.setSelectedValue(host.getActivePlayer(), true);
        }

        JPanel btnBar = new JPanel();
        JButton selBtn = new JButton("Pilia (Select)");
        JButton newBtn = new JButton("Bag-ong Profile (New)");
        JButton delBtn = new JButton("Papasa (Delete)");
        JButton closeBtn = new JButton("Isara (Close)");

        selBtn.addActionListener(e -> {
            Player p = list.getSelectedValue();
            if (p != null) {
                host.setActivePlayer(p);
                dlg.dispose();
                refreshProfiles();
            }
        });

        newBtn.addActionListener(e -> {
            dlg.dispose();
            onNewProfile();
        });

        delBtn.addActionListener(e -> {
            Player p = list.getSelectedValue();
            if (p != null) {
                int res = JOptionPane.showConfirmDialog(
                    dlg,
                    "Sigurado ka bang papason si " + p.getPlayerName() + "?",
                    "Kompirmasyon",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );
                if (res == JOptionPane.YES_OPTION) {
                    DataManager.getInstance().deletePlayer(p.getPlayerId());
                    model.removeElement(p);
                    if (host.getActivePlayer() != null && host.getActivePlayer().getPlayerId() == p.getPlayerId()) {
                        host.setActivePlayer(model.isEmpty() ? null : model.get(0));
                    }
                    refreshProfiles();
                }
            }
        });

        closeBtn.addActionListener(e -> dlg.dispose());

        btnBar.add(selBtn);
        btnBar.add(newBtn);
        btnBar.add(delBtn);
        btnBar.add(closeBtn);

        dlg.add(new JScrollPane(list), BorderLayout.CENTER);
        dlg.add(btnBar, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void onQuit() {
        if (host != null) {
            DataManager.getInstance().savePlayers();
        }
        System.exit(0);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth();
        int h = getHeight();

        // 1. Base Layer: Sky background (bg.png)
        if (skyBg != null) {
            g2.drawImage(skyBg, 0, 0, w, h, null);
        }

        // 2. Middle Layer: Mount Apo Foothills (mtapo.png)
        if (mtApo != null) {
            g2.drawImage(mtApo, 0, 0, w, h, null);
        }

        // 3. Border Layer: Authentic Indigenous Decorative Frame (indigframe.png)
        if (indigFrame != null) {
            g2.drawImage(indigFrame, 0, 0, w, h, null);
        }

        // 4. Foreground: Game Title Logo (Title.png)
        if (titleLogo != null) {
            int titleW = (int) (w * 0.52);
            int titleH = titleW * titleLogo.getHeight() / titleLogo.getWidth();
            int titleX = (w - titleW) / 2;
            int titleY = 28;
            g2.drawImage(titleLogo, titleX, titleY, titleW, titleH, null);
        }

        // 5. Top-Left Player Profile Card (Inset within Indigenous Frame)
        Player active = host.getActivePlayer();
        if (active != null) {
            int cx = playerCardBounds.x;
            int cy = playerCardBounds.y;
            int cw = playerCardBounds.width;
            int ch = playerCardBounds.height;

            g2.setColor(playerCardHovered ? DUSK_NAVY : DEEP_INDIGO);
            g2.fillRoundRect(cx, cy, cw, ch, 20, 20);

            g2.setColor(playerCardHovered ? SEQUESTA : DUSK_NAVY);
            g2.setStroke(new BasicStroke(playerCardHovered ? 2.0f : 1.2f));
            g2.drawRoundRect(cx, cy, cw, ch, 20, 20);

            // Avatar on the left
            if (avatarImg != null) {
                g2.drawImage(avatarImg, cx + 8, cy + 7, 34, 34, null);
            } else {
                g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
                g2.setColor(Color.WHITE);
                g2.drawString("👤", cx + 12, cy + 31);
            }

            // Player Name
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
            g2.setColor(GOLD);
            g2.drawString(active.getPlayerName(), cx + 50, cy + 22);

            // Subtitle
            g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
            g2.setColor(SKY_BLUE);
            String info = "Naabli: Mapa " + active.getUnlockedMap() + "  |  Score: " + active.getScore();
            g2.drawString(info, cx + 50, cy + 39);
        }
    }

    /**
     * Inner custom button component rendering artist-crafted PNG button sprites with:
     * - Bilinear interpolation
     * - Tactile 2px press-down animation on click
     * - Subtle white highlight on hover
     * - 38% opacity disabled state
     */
    private final class ImageAssetButton extends JButton {
        private final BufferedImage sprite;
        private boolean isHovered = false;
        private boolean isPressed = false;

        public ImageAssetButton(BufferedImage sprite) {
            this.sprite = sprite;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (isEnabled()) {
                        isHovered = true;
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    isPressed = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (isEnabled() && SwingUtilities.isLeftMouseButton(e)) {
                        isPressed = true;
                        repaint();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (isPressed) {
                        isPressed = false;
                        repaint();
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int w = getWidth();
            int h = getHeight();
            int drawY = 0;
            if (isPressed && isEnabled()) {
                drawY += 2;
            }

            if (!isEnabled()) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.38f));
                if (sprite != null) {
                    g2.drawImage(sprite, 0, drawY, w, h, this);
                }
            } else if (isHovered) {
                if (sprite != null) {
                    g2.drawImage(sprite, 0, drawY, w, h, this);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.14f));
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, drawY, w, h, 24, 24);
                }
            } else {
                if (sprite != null) {
                    g2.drawImage(sprite, 0, drawY, w, h, this);
                }
            }

            g2.dispose();
        }
    }
}
