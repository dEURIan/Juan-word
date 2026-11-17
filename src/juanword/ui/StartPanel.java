package juanword.ui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
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

    // Buttons & Sprites
    private final JButton continueBtn;
    private final JButton newGameBtn;
    private final JButton settingBtn;
    private final JButton exitBtn;
    private final BufferedImage contImg;
    private final BufferedImage newGameImg;
    private final BufferedImage setImg;
    private final BufferedImage exitImg;

    // Player Card Bounds & State
    private final Rectangle playerCardBounds = new Rectangle(36, 24, 230, 48);
    private boolean playerCardHovered = false;

    // Title Logo Bounds & Layout Tracking
    private final Rectangle titleBounds = new Rectangle();
    private int lastW = -1;
    private int lastH = -1;

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
        this.contImg = ImageLoader.load("continuebtn.png");
        BufferedImage ng = ImageLoader.load("newgamebtn.png");
        if (ng == null || ImageLoader.isPlaceholder(ng)) {
            ng = ImageLoader.load("startbtn.png");
        }
        this.newGameImg = ng;
        this.setImg = ImageLoader.load("settingbtn.png");
        this.exitImg = ImageLoader.load("exitbtn.png");

        continueBtn = createMenuButton();
        newGameBtn = createMenuButton();
        settingBtn = createMenuButton();
        exitBtn = createMenuButton();

        continueBtn.addActionListener(e -> onContinue());
        newGameBtn.addActionListener(e -> onNewProfile());
        settingBtn.addActionListener(e -> showProfileSelectionDialog());
        exitBtn.addActionListener(e -> onQuit());

        add(continueBtn);
        add(newGameBtn);
        add(settingBtn);
        add(exitBtn);

        recalculateLayout();

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                lastW = -1;
                lastH = -1;
                recalculateLayout();
                repaint();
            }
        });

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
        lastW = -1;
        lastH = -1;
        recalculateLayout();
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
        dlg.setSize(480, 420);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(0, 0));
        dlg.getContentPane().setBackground(new Color(11, 19, 43));

        // 1. Header panel matching deep navy / gold scheme
        JPanel headerPanel = new JPanel(new BorderLayout(4, 4));
        headerPanel.setBackground(new Color(15, 25, 45));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(225, 155, 15)),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        JLabel titleLbl = new JLabel("👤 PILIA ANG PROFILE (Select Account)");
        titleLbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        titleLbl.setForeground(GOLD);
        JLabel subLbl = new JLabel("Pilia ang imong account aron makapadayon sa biyahe sa Davao");
        subLbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        subLbl.setForeground(SKY_BLUE);
        headerPanel.add(titleLbl, BorderLayout.NORTH);
        headerPanel.add(subLbl, BorderLayout.SOUTH);

        // 2. Profile List with themed cards
        DefaultListModel<Player> model = new DefaultListModel<>();
        for (Player p : profiles) model.addElement(p);

        JList<Player> list = new JList<>(model);
        list.setBackground(new Color(15, 25, 45));
        list.setSelectionBackground(new Color(32, 52, 98));
        list.setSelectionForeground(GOLD);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object val, int idx, boolean isSel, boolean focus) {
                JPanel card = new JPanel(new BorderLayout(8, 2));
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(4, 8, 4, 8),
                    BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(isSel ? GOLD : new Color(38, 52, 85), isSel ? 2 : 1, true),
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)
                    )
                ));
                card.setBackground(isSel ? new Color(28, 48, 92) : new Color(18, 28, 52));

                if (val instanceof Player pl) {
                    JLabel nameLbl = new JLabel("👤 " + pl.getPlayerName());
                    nameLbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
                    nameLbl.setForeground(isSel ? GOLD : Color.WHITE);

                    String info = "Naabli: Mapa " + pl.getUnlockedMap() + "  |  Score: " + pl.getScore()
                        + (pl.hasActiveBattle() ? "  •  ⚔ Gubat Naabli" : "");
                    JLabel infoLbl = new JLabel(info);
                    infoLbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
                    infoLbl.setForeground(pl.hasActiveBattle() ? new Color(255, 190, 80) : SKY_BLUE);

                    card.add(nameLbl, BorderLayout.NORTH);
                    card.add(infoLbl, BorderLayout.SOUTH);
                }
                return card;
            }
        });

        if (host.getActivePlayer() != null) {
            list.setSelectedValue(host.getActivePlayer(), true);
        }

        // 3. Styled Action Button Bar
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnBar.setBackground(new Color(15, 25, 45));
        btnBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(35, 50, 80)));

        JButton selBtn = createThemedButton("Pilia (Select)", new Color(245, 185, 30), new Color(15, 20, 40), GOLD);
        JButton newBtn = createThemedButton("Bag-o (New)", new Color(25, 45, 80), GOLD, SKY_BLUE);
        JButton delBtn = createThemedButton("Papasa (Delete)", new Color(65, 20, 30), new Color(255, 140, 150), new Color(180, 50, 60));
        JButton closeBtn = createThemedButton("Isara (Close)", new Color(30, 40, 65), Color.WHITE, new Color(60, 75, 110));

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

        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setBackground(new Color(11, 19, 43));
        scrollPane.getViewport().setBackground(new Color(15, 25, 45));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(35, 50, 85)));

        dlg.add(headerPanel, BorderLayout.NORTH);
        dlg.add(scrollPane, BorderLayout.CENTER);
        dlg.add(btnBar, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private JButton createThemedButton(String text, Color bg, Color fg, Color border) {
        JButton btn = new JButton(text);
        btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(border, 1, true),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(
                    Math.min(255, bg.getRed() + 25),
                    Math.min(255, bg.getGreen() + 25),
                    Math.min(255, bg.getBlue() + 30)
                ));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }

    private void onQuit() {
        if (host != null) {
            DataManager.getInstance().savePlayers();
        }
        System.exit(0);
    }

    public void recalculateLayout() {
        int w = getWidth() > 0 ? getWidth() : 960;
        int h = getHeight() > 0 ? getHeight() : 540;
        if (w == lastW && h == lastH) return;
        lastW = w;
        lastH = h;

        double scaleX = w / 960.0;
        double scaleY = h / 540.0;
        double S = Math.min(scaleX, scaleY);

        // 1. Responsive Player Card Bounds (top-left inset within indigenous frame)
        int cardX = (int) Math.round(36 * scaleX);
        int cardY = (int) Math.max(24 * scaleY, 20 + 10 * scaleY);
        int cardW = (int) Math.round(230 * S);
        int cardH = (int) Math.round(48 * S);
        playerCardBounds.setBounds(cardX, cardY, cardW, cardH);

        // 2. Responsive Menu Buttons
        // Buttons scale with S and are centered horizontally
        int btnW = (int) Math.round(196 * S);
        int btnH = (int) Math.round(48 * S);
        int gap = (int) Math.round(6 * S);
        int totalBtnH = (btnH * 4) + (gap * 3);

        int bottomMargin = (int) Math.round(30 * scaleY);
        int startY = h - bottomMargin - totalBtnH;
        int btnX = (w - btnW) / 2;

        continueBtn.setBounds(btnX, startY, btnW, btnH);
        newGameBtn.setBounds(btnX, startY + (btnH + gap), btnW, btnH);
        settingBtn.setBounds(btnX, startY + (btnH + gap) * 2, btnW, btnH);
        exitBtn.setBounds(btnX, startY + (btnH + gap) * 3, btnW, btnH);

        // 3. Title Logo Bounds
        // Title logo must be placed in the space above startY without overlapping buttons
        if (titleLogo != null && titleLogo.getWidth() > 0) {
            double aspect = (double) titleLogo.getHeight() / titleLogo.getWidth();
            int topMargin = (int) Math.round(26 * scaleY);
            int availableH = startY - topMargin - (int) Math.round(14 * scaleY);

            int maxTitleW = (int) (w * 0.54);
            int titleW = maxTitleW;
            int titleH = (int) Math.round(titleW * aspect);

            if (titleH > availableH && availableH > 50) {
                titleH = availableH;
                titleW = (int) Math.round(titleH / aspect);
            }

            int titleX = (w - titleW) / 2;
            int titleY = topMargin + Math.max(0, (availableH - titleH) / 2);
            titleBounds.setBounds(titleX, titleY, titleW, titleH);
        }
    }

    public Rectangle getTitleBounds() { return new Rectangle(titleBounds); }
    public Rectangle getPlayerCardBounds() { return new Rectangle(playerCardBounds); }
    public Rectangle getContinueBtnBounds() { return continueBtn.getBounds(); }
    public Rectangle getNewGameBtnBounds() { return newGameBtn.getBounds(); }
    public Rectangle getSettingBtnBounds() { return settingBtn.getBounds(); }
    public Rectangle getExitBtnBounds() { return exitBtn.getBounds(); }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        recalculateLayout();

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

        // 4. Foreground: Game Title Logo (Title.png) - Centered and non-overlapping
        if (titleLogo != null && titleBounds.width > 0 && titleBounds.height > 0) {
            g2.drawImage(titleLogo, titleBounds.x, titleBounds.y, titleBounds.width, titleBounds.height, null);
        }

        // 5. Top-Left Player Profile Card (Inset within Indigenous Frame)
        Player active = host.getActivePlayer();
        if (active != null) {
            double scaleX = w / 960.0;
            double scaleY = h / 540.0;
            double S = Math.min(scaleX, scaleY);

            int cx = playerCardBounds.x;
            int cy = playerCardBounds.y;
            int cw = playerCardBounds.width;
            int ch = playerCardBounds.height;
            int corner = Math.max(12, (int) Math.round(20 * S));

            g2.setColor(playerCardHovered ? DUSK_NAVY : DEEP_INDIGO);
            g2.fillRoundRect(cx, cy, cw, ch, corner, corner);

            g2.setColor(playerCardHovered ? SEQUESTA : DUSK_NAVY);
            g2.setStroke(new BasicStroke(playerCardHovered ? 2.0f : 1.2f));
            g2.drawRoundRect(cx, cy, cw, ch, corner, corner);

            // Avatar on the left
            int avSize = Math.max(20, (int) Math.round(34 * S));
            int avPadX = Math.max(4, (int) Math.round(8 * S));
            int avPadY = (ch - avSize) / 2;
            if (avatarImg != null) {
                g2.drawImage(avatarImg, cx + avPadX, cy + avPadY, avSize, avSize, null);
            } else {
                g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, Math.max(12, (int) Math.round(20 * S))));
                g2.setColor(Color.WHITE);
                g2.drawString("👤", cx + avPadX + (int) Math.round(4 * S), cy + ch - (int) Math.round(14 * S));
            }

            int textX = cx + avPadX + avSize + Math.max(4, (int) Math.round(8 * S));

            // Player Name
            int nameFontSize = Math.max(13, (int) Math.round(14 * S));
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, nameFontSize));
            g2.setColor(GOLD);
            g2.drawString(active.getPlayerName(), textX, cy + (int) Math.round(22 * S));

            // Subtitle
            int subFontSize = Math.max(11, (int) Math.round(12 * S));
            g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, subFontSize));
            g2.setColor(SKY_BLUE);
            String info = "Naabli: Mapa " + active.getUnlockedMap() + "  |  Score: " + active.getScore();
            g2.drawString(info, textX, cy + (int) Math.round(39 * S));
        }
    }

    private JButton createMenuButton() {
        JButton btn = new JButton();
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setRolloverEnabled(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    @Override
    protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        drawMenuButton(g2, continueBtn, contImg);
        drawMenuButton(g2, newGameBtn, newGameImg);
        drawMenuButton(g2, settingBtn, setImg);
        drawMenuButton(g2, exitBtn, exitImg);
        g2.dispose();
    }

    private void drawMenuButton(Graphics2D g2, JButton btn, BufferedImage sprite) {
        if (sprite == null || btn == null || !btn.isVisible()) {
            return;
        }
        Rectangle b = btn.getBounds();
        ButtonModel model = btn.getModel();
        boolean pressed = model.isPressed() && model.isArmed() && btn.isEnabled();
        boolean hovered = model.isRollover() && btn.isEnabled();
        boolean enabled = btn.isEnabled();

        int drawX = b.x;
        int drawY = b.y + (pressed ? 2 : 0);
        int w = b.width;
        int h = b.height;

        Graphics2D bg = (Graphics2D) g2.create();
        if (!enabled) {
            bg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.38f));
            bg.drawImage(sprite, drawX, drawY, w, h, null);
        } else if (hovered) {
            bg.drawImage(sprite, drawX, drawY, w, h, null);
            bg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.14f));
            bg.setColor(Color.WHITE);
            int corner = Math.max(12, (int) Math.round(24 * (h / 48.0)));
            bg.fillRoundRect(drawX, drawY, w, h, corner, corner);
        } else {
            bg.drawImage(sprite, drawX, drawY, w, h, null);
        }
        bg.dispose();
    }
}
