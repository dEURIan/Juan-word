package juanword.ui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import juanword.JuanWord;
import juanword.Screen;
import juanword.model.BattleSession;
import juanword.model.BattleSessionBuilder;
import juanword.model.DifficultySetting;
import juanword.model.Player;

public final class MapSelectPanel extends JPanel {
    private static final Color CERULEAN = new Color(64, 180, 200);
    private static final Color GOLD = new Color(255, 220, 100);

    private final JuanWord host;
    private final List<Landmark> landmarks = new ArrayList<>();
    private final BufferedImage skyBg;
    private final BufferedImage mtApoBg;
    private final BufferedImage indigFrame;
    private final BufferedImage settingsIcon;
    private final Timer renderTimer;
    private Point mousePoint = new Point(-1, -1);
    private Landmark hovered = null;
    private Rectangle backButtonBounds = new Rectangle(36, 20, 110, 32);
    private Rectangle settingsButtonBounds = new Rectangle(890, 20, 34, 34);
    private Rectangle fightGudoButtonBounds = new Rectangle(0, 0, 0, 0);

    // Map travel horizontal slide animation
    private boolean isTransitioning = false;
    private float transitionProgress = 0f;
    private int fromMapIndex = 1;
    private int toMapIndex = 2;

    public MapSelectPanel(JuanWord host) {
        this.host = host;
        this.skyBg = ImageLoader.load("bg.png");
        this.mtApoBg = ImageLoader.load("mtapo.png");
        this.indigFrame = ImageLoader.load("indigframe.png");
        this.settingsIcon = ImageLoader.load("Cog Setting.png");

        initLandmarks();
        this.renderTimer = new Timer(16, this::tick);

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                onMouseMoved(e);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                onMouseClicked(e);
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    private void initLandmarks() {
        landmarks.clear();
        landmarks.add(new Landmark(1, "Ateneo Blue Building", "Blue Building.png", new Rectangle(40, 120, 180, 260)));
        landmarks.add(new Landmark(2, "Davao Museum", "durianmuseum.png", new Rectangle(250, 120, 180, 260)));
        landmarks.add(new Landmark(3, "San Pedro Cathedral", "sanpedro.png", new Rectangle(460, 120, 180, 260)));
        landmarks.add(new Landmark(4, "Mount Apo", "mtapo.png", new Rectangle(0, 0, 960, 540)));
    }

    public void refresh() {
        Player p = host.getActivePlayer();
        int unlocked = (p != null) ? p.getUnlockedMap() : 1;
        for (Landmark lm : landmarks) {
            lm.setLocked(lm.getMapIndex() > unlocked);
        }
        recalculateLayout();
        if (!renderTimer.isRunning()) {
            renderTimer.start();
        }
        repaint();
    }

    public void stop() {
        if (renderTimer.isRunning()) {
            renderTimer.stop();
        }
    }

    public List<Landmark> getLandmarks() {
        return Collections.unmodifiableList(landmarks);
    }

    public void triggerTravelTransition(int fromMap, int toMap) {
        this.fromMapIndex = fromMap;
        this.toMapIndex = toMap;
        this.transitionProgress = 0f;
        this.isTransitioning = true;
        if (!renderTimer.isRunning()) {
            renderTimer.start();
        }
    }

    private void recalculateLayout() {
        int w = getWidth() > 0 ? getWidth() : 960;
        int h = getHeight() > 0 ? getHeight() : 540;

        float S = Math.min((float) w / 960f, (float) h / 540f);
        int stageW = Math.min(w, (int) (h * 16.0 / 9.0));
        int stageOffset = (w - stageW) / 2;

        // Map 1: Blue Building (positioned on the right, scaled bigger)
        int bbH = (int) (h * 0.52);
        int bbW = (int) (stageW * 0.35);
        int bbX = stageOffset + stageW - bbW + (int) (15 * S);
        int bbY = h - bbH - (int) (16 * S);
        if (landmarks.size() > 0) landmarks.get(0).setBounds(new Rectangle(bbX, bbY, bbW, bbH));

        // Map 2: Davao Museum (positioned closer to the Blue Building)
        int dmW = (int) (stageW * 0.38);
        int dmH = (int) (h * 0.55);
        int dmX = stageOffset + (stageW - dmW) / 2 + (int) (35 * S);
        int dmY = h - dmH - (int) (16 * S);
        if (landmarks.size() > 1) landmarks.get(1).setBounds(new Rectangle(dmX, dmY, dmW, dmH));

        // Map 3: San Pedro Cathedral (positioned on the left, scaled bigger)
        int spH = (int) (h * 0.52);
        int spW = (int) (stageW * 0.38);
        int spX = stageOffset + (int) (stageW * 0.015);
        int spY = h - spH - (int) (16 * S);
        if (landmarks.size() > 2) landmarks.get(2).setBounds(new Rectangle(spX, spY, spW, spH));

        // Map 4: Mount Apo
        if (landmarks.size() > 3) landmarks.get(3).setBounds(new Rectangle(0, 0, w, h));

        backButtonBounds = new Rectangle((int) (w * 0.035), (int) (h * 0.038), (int) (125 * S), (int) (34 * S));
        settingsButtonBounds = new Rectangle(w - (int) (64 * S), (int) (h * 0.038), (int) (34 * S), (int) (34 * S));

        Player p = host.getActivePlayer();
        boolean gudoUnlocked = (p != null && (p.hasDefeated("BOSS_PORTER") || p.hasDefeated("GUDO")));
        if (gudoUnlocked) {
            int fgW = (int) (160 * S);
            int fgH = (int) (32 * S);
            int fgX = (w - fgW) / 2;
            int fgY = (int) (60 * S);
            fightGudoButtonBounds = new Rectangle(fgX, fgY, fgW, fgH);
        } else {
            fightGudoButtonBounds = new Rectangle(0, 0, 0, 0);
        }
    }

    public Rectangle getFightGudoButtonBounds() {
        return new Rectangle(fightGudoButtonBounds);
    }

    private void onMouseMoved(MouseEvent e) {
        if (isTransitioning) return;
        mousePoint = e.getPoint();
        Landmark previouslyHovered = hovered;
        hovered = null;

        // Foreground buildings (0, 1, 2) have priority over background Mount Apo (3)
        for (int i = 0; i < Math.min(3, landmarks.size()); i++) {
            Landmark lm = landmarks.get(i);
            if (lm.contains(mousePoint)) {
                hovered = lm;
                break;
            }
        }
        if (hovered == null && landmarks.size() > 3) {
            Landmark apo = landmarks.get(3);
            if (apo.contains(mousePoint)) {
                hovered = apo;
            }
        }

        boolean overBack = backButtonBounds.contains(mousePoint);
        boolean overSettings = settingsButtonBounds.contains(mousePoint);
        boolean overFightGudo = fightGudoButtonBounds.width > 0 && fightGudoButtonBounds.contains(mousePoint);
        if (overBack || overSettings || overFightGudo) {
            hovered = null;
        }

        setCursor((hovered != null || overBack || overSettings || overFightGudo) ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());

        if (hovered != previouslyHovered) {
            repaint();
        }
    }

    private void onMouseClicked(MouseEvent e) {
        if (isTransitioning) return;
        if (fightGudoButtonBounds.width > 0 && fightGudoButtonBounds.contains(e.getPoint())) {
            launchBattle(4, 4);
            return;
        }
        if (backButtonBounds.contains(e.getPoint())) {
            stop();
            host.showScreen(Screen.START);
            return;
        }
        if (settingsButtonBounds.contains(e.getPoint())) {
            stop();
            host.showScreen(Screen.START);
            return;
        }

        Landmark clicked = null;
        // Foreground buildings (0, 1, 2) have priority over background Mount Apo (3)
        for (int i = 0; i < Math.min(3, landmarks.size()); i++) {
            Landmark lm = landmarks.get(i);
            if (lm.contains(e.getPoint())) {
                clicked = lm;
                break;
            }
        }
        if (clicked == null && landmarks.size() > 3) {
            Landmark apo = landmarks.get(3);
            if (apo.contains(e.getPoint())) {
                clicked = apo;
            }
        }

        if (clicked != null) {
            Player p = host.getActivePlayer();
            int unlocked = (p != null) ? p.getUnlockedMap() : 1;
            if (clicked.getMapIndex() <= unlocked) {
                launchBattle(clicked.getMapIndex());
            } else {
                String prevLandmark = switch (clicked.getMapIndex()) {
                    case 2 -> "Ateneo Blue Building (Sayon)";
                    case 3 -> "Davao Museum (Sakto)";
                    case 4 -> "San Pedro Cathedral (Lisod)";
                    default -> "naunang mapa";
                };
                String diffName = DifficultySetting.forMap(clicked.getMapIndex()).getDisplayName();
                String msg = "🔒 NAKA-LOCK PA KINI NGA MAPA!\n\n"
                    + "Kinahanglan nimo pildihon una ang " + prevLandmark + "\n"
                    + "aron maablihan ang bag-ong mapa ug ang predefined difficulty niini (" + diffName + ").\n\n"
                    + "Padayon sa pagduwa ug pagkat-on, Juan!";
                JOptionPane.showMessageDialog(this, msg, "Nakasera ang Mapa", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private void launchBattle(int mapIndex) {
        launchBattle(mapIndex, 1);
    }

    private void launchBattle(int mapIndex, int stageIndex) {
        stop();
        Player p = host.getActivePlayer();
        BattleSession session = new BattleSessionBuilder()
            .withPlayer(p)
            .onMap(mapIndex)
            .atStage(stageIndex)
            .build();
        host.startBattle(session);
    }

    private void tick(ActionEvent e) {
        if (isTransitioning) {
            transitionProgress += 0.035f;
            if (transitionProgress >= 1.0f) {
                transitionProgress = 1.0f;
                isTransitioning = false;
            }
        } else {
            for (Landmark lm : landmarks) {
                lm.updateScale(lm == hovered);
            }
        }
        repaint();
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
        recalculateLayout();

        // 1. Sky & Clouds Background (bg.png)
        if (skyBg != null) {
            g2.drawImage(skyBg, 0, 0, w, h, null);
        } else {
            g2.setColor(new Color(25, 35, 55));
            g2.fillRect(0, 0, w, h);
        }

        // 2. Towering Mount Apo in Background (Landmark 4)
        if (landmarks.size() > 3) {
            landmarks.get(3).paint(g2);
        } else if (mtApoBg != null) {
            g2.drawImage(mtApoBg, 0, 0, w, h, null);
        }

        // 3. Subtle Scrim overlay
        g2.setColor(new Color(15, 22, 45, 80));
        g2.fillRect(0, 0, w, h);

        // 4. Landmarks: First 3 are foreground buildings
        if (isTransitioning) {
            int slideOffset = (int) (w * transitionProgress);
            g2.translate(-slideOffset, 0);
            for (int i = 0; i < Math.min(3, landmarks.size()); i++) landmarks.get(i).paint(g2);
            g2.translate(slideOffset, 0);
        } else {
            for (int i = 0; i < Math.min(3, landmarks.size()); i++) landmarks.get(i).paint(g2);
        }

        if (landmarks.size() > 3 && landmarks.get(3).isLocked()) {
            drawSummitLock(g2, w / 2, (int) (h * 0.28));
        }

        // 5. HUD, Banners & Buttons
        drawHud(g2, w, h);

        // 6. Indigenous Border Frame (indigframe.png) strictly on top
        if (indigFrame != null) {
            g2.drawImage(indigFrame, 0, 0, w, h, null);
        }
    }

    private void drawSummitLock(Graphics2D g2, int cx, int cy) {
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRoundRect(cx - 16, cy - 16, 32, 32, 10, 10);

        g2.setColor(new Color(230, 235, 245));
        g2.setStroke(new BasicStroke(2.8f));
        g2.drawArc(cx - 6, cy - 11, 12, 14, 0, 180);

        g2.setColor(new Color(235, 185, 50));
        g2.fillRoundRect(cx - 9, cy - 2, 18, 15, 3, 3);
        g2.setColor(new Color(175, 125, 20));
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRoundRect(cx - 9, cy - 2, 18, 15, 3, 3);

        g2.setColor(new Color(40, 30, 20));
        g2.fillOval(cx - 2, cy + 2, 4, 4);
        g2.fillRect(cx - 1, cy + 4, 2, 4);
    }

    private void drawHud(Graphics2D g2, int w, int h) {
        double scaleX = w / 960.0;
        double scaleY = h / 540.0;
        double S = Math.min(scaleX, scaleY);

        // Back Button in Dark Navy Pill with Gold Border
        g2.setColor(new Color(15, 25, 45, 230));
        g2.fillRoundRect(backButtonBounds.x, backButtonBounds.y, backButtonBounds.width, backButtonBounds.height, 10, 10);
        g2.setColor(GOLD);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(backButtonBounds.x, backButtonBounds.y, backButtonBounds.width, backButtonBounds.height, 10, 10);
        g2.setColor(Color.WHITE);
        int backFontSize = Math.max(12, (int) Math.round(13 * S));
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, backFontSize));
        FontMetrics fmBack = g2.getFontMetrics();
        String backText = "← Balik (Back)";
        int bx = backButtonBounds.x + (backButtonBounds.width - fmBack.stringWidth(backText)) / 2;
        int by = backButtonBounds.y + (backButtonBounds.height + fmBack.getAscent() - fmBack.getDescent()) / 2;
        g2.drawString(backText, bx, by);

        // Screen Title in Dark Navy Pill with Gold Border
        String title = "PILIA ANG MAPA • MAP SELECT";
        int titleFontSize = Math.max(13, (int) Math.round(14 * S));
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, titleFontSize));
        FontMetrics fmTitle = g2.getFontMetrics();
        int titleTextW = fmTitle.stringWidth(title);
        int titleBoxW = titleTextW + (int) Math.round(36 * S);
        int titleBoxH = Math.max(34, (int) Math.round(34 * S));
        int titleBoxX = (w - titleBoxW) / 2;
        int titleBoxY = backButtonBounds.y;

        g2.setColor(new Color(15, 25, 45, 230));
        g2.fillRoundRect(titleBoxX, titleBoxY, titleBoxW, titleBoxH, 10, 10);
        g2.setColor(GOLD);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(titleBoxX, titleBoxY, titleBoxW, titleBoxH, 10, 10);

        g2.setColor(GOLD);
        int tx = titleBoxX + (titleBoxW - titleTextW) / 2;
        int ty = titleBoxY + (titleBoxH + fmTitle.getAscent() - fmTitle.getDescent()) / 2;
        g2.drawString(title, tx, ty);

        // "Fight Gudo" Boss Challenge Button (visible only when unlocked)
        if (fightGudoButtonBounds.width > 0) {
            boolean isHovered = (mousePoint != null && fightGudoButtonBounds.contains(mousePoint));
            Color bgCol = isHovered ? new Color(130, 25, 30, 245) : new Color(65, 15, 20, 235);
            Color borderCol = isHovered ? new Color(255, 240, 150) : GOLD;

            g2.setColor(bgCol);
            g2.fillRoundRect(fightGudoButtonBounds.x, fightGudoButtonBounds.y, fightGudoButtonBounds.width, fightGudoButtonBounds.height, 8, 8);
            g2.setColor(borderCol);
            g2.setStroke(new BasicStroke(isHovered ? 2.2f : 1.6f));
            g2.drawRoundRect(fightGudoButtonBounds.x, fightGudoButtonBounds.y, fightGudoButtonBounds.width, fightGudoButtonBounds.height, 8, 8);

            g2.setColor(Color.WHITE);
            int gudoFontSize = Math.max(13, (int) Math.round(14 * S));
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, gudoFontSize));
            String gudoText = "⚔ Fight Gudo";
            FontMetrics gfm = g2.getFontMetrics();
            int gtw = gfm.stringWidth(gudoText);
            int gtx = fightGudoButtonBounds.x + (fightGudoButtonBounds.width - gtw) / 2;
            int gty = fightGudoButtonBounds.y + (fightGudoButtonBounds.height + gfm.getAscent() - gfm.getDescent()) / 2;
            g2.drawString(gudoText, gtx, gty);
        }

        // Player Info Card in Top Right
        Player p = host.getActivePlayer();
        if (p != null) {
            String info = p.getPlayerName() + "  |  Score: " + p.getScore();
            int infoFontSize = Math.max(12, (int) Math.round(13 * S));
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, infoFontSize));
            FontMetrics fmInfo = g2.getFontMetrics();
            int iw = fmInfo.stringWidth(info);
            int pBoxW = iw + (int) Math.round(28 * S);
            int pBoxX = settingsButtonBounds.x - pBoxW - (int) Math.round(12 * S);
            g2.setColor(new Color(15, 25, 45, 230));
            g2.fillRoundRect(pBoxX, titleBoxY, pBoxW, titleBoxH, 10, 10);
            g2.setColor(GOLD);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(pBoxX, titleBoxY, pBoxW, titleBoxH, 10, 10);
            g2.setColor(Color.WHITE);
            int pix = pBoxX + (pBoxW - iw) / 2;
            int piy = titleBoxY + (titleBoxH + fmInfo.getAscent() - fmInfo.getDescent()) / 2;
            g2.drawString(info, pix, piy);
        }

        // Settings Cog Button
        g2.setColor(new Color(15, 25, 45, 230));
        g2.fillRoundRect(settingsButtonBounds.x, settingsButtonBounds.y, settingsButtonBounds.width, settingsButtonBounds.height, 8, 8);
        g2.setColor(GOLD);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(settingsButtonBounds.x, settingsButtonBounds.y, settingsButtonBounds.width, settingsButtonBounds.height, 8, 8);
        if (settingsIcon != null) {
            int iconPad = Math.max(4, (settingsButtonBounds.width - (int) Math.round(20 * S)) / 2);
            int iconSize = settingsButtonBounds.width - iconPad * 2;
            g2.drawImage(settingsIcon, settingsButtonBounds.x + iconPad, settingsButtonBounds.y + iconPad, iconSize, iconSize, null);
        }

        // Hovered Landmark Info Card at Bottom
        if (hovered != null && !isTransitioning) {
            int mapIdx = hovered.getMapIndex();
            DifficultySetting diff = DifficultySetting.forMap(mapIdx);
            String banner = hovered.getName().toUpperCase() + "  [ " + diff.getDisplayName().toUpperCase() + " ]";
            if (hovered.isLocked()) {
                banner += "  - 🔒 NAKASERA";
            }

            int cardFontSize = Math.max(13, (int) Math.round(14 * S));
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, cardFontSize));
            FontMetrics fmCard = g2.getFontMetrics();
            int cardW = fmCard.stringWidth(banner) + (int) Math.round(36 * S);
            int cardH = Math.max(34, (int) Math.round(34 * S));
            int cardX = (w - cardW) / 2;
            int cardY = h - (int) Math.round(58 * S);

            g2.setColor(new Color(15, 25, 45, 230));
            g2.fillRoundRect(cardX, cardY, cardW, cardH, 10, 10);
            g2.setColor(hovered.isLocked() ? new Color(220, 60, 60) : GOLD);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(cardX, cardY, cardW, cardH, 10, 10);
            g2.setColor(Color.WHITE);
            int cx = cardX + (cardW - fmCard.stringWidth(banner)) / 2;
            int cy = cardY + (cardH + fmCard.getAscent() - fmCard.getDescent()) / 2;
            g2.drawString(banner, cx, cy);
        }
    }
}
