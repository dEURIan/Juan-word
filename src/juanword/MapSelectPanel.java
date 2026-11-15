package juanword;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Interactive Map Selection Screen for JuanWord.
 *
 * Game Architecture & Staging:
 * 1. Back Layer: Sky background (bg.png) + Towering Mount Apo (mtapo.png - Map 4, Extreme)
 * 2. Foreground Landmarks (Left to Right):
 *    - Map 1 (Left): Blue Building (bluebuilding.png - Predefined Difficulty: Easy / Sayon)
 *    - Map 2 (Center): Davao Museum (durianmuseum.png - Predefined Difficulty: Medium / Sakto)
 *    - Map 3 (Right): San Pedro Cathedral (sanpedro.png - Predefined Difficulty: Hard / Lisod)
 * 3. Silhouette Rendering: Locked maps are rendered as dark obsidian silhouettes with a lock emblem.
 * 4. Predefined Difficulty: Clicking an unlocked map launches directly into battle with that map's
 *    fixed difficulty (Easy, Medium, Hard, Extreme) without requiring manual difficulty selection.
 * 5. Interactive Animation & Hover:
 *    - Entrance: Landmarks rise smoothly from the bottom of the screen.
 *    - Hover: Expands hovered map (+7% anchored at base) with a custom luminous HUD badge.
 */
public class MapSelectPanel extends JPanel {

    private final JuanWord app;

    // Background Layers
    private BufferedImage skyBg;
    private BufferedImage mtApo;
    private BufferedImage indigFrame;

    // Foreground Landmark Sprites (Cropped tight to visual artwork)
    private BufferedImage blueBuildingImg;
    private BufferedImage durianMuseumImg;
    private BufferedImage sanPedroImg;

    // Silhouettes for locked maps
    private BufferedImage silBlueBuilding;
    private BufferedImage silDurianMuseum;
    private BufferedImage silSanPedro;
    private BufferedImage silMtApo;

    // Landmarks Configuration
    // 0: Blue Building (Easy), 1: Davao Museum (Medium), 2: San Pedro (Hard), 3: Mt Apo (Extreme)
    public static final int LANDMARK_BLUE_BUILDING = 0;
    public static final int LANDMARK_DURIAN_MUSEUM = 1;
    public static final int LANDMARK_SAN_PEDRO = 2;
    public static final int LANDMARK_MT_APO = 3;
    public static final int LANDMARK_COUNT = 4;

    // Animation & Hover States
    private float animProgress = 1.0f; // 0.0 -> 1.0
    private Timer entranceTimer;
    private Timer hoverTimer;

    // Level Selection Expand Transition
    private boolean isExpanding = false;
    private int expandingLandmark = -1;
    private float expandProgress = 0.0f; // 0.0 -> 1.0
    private Timer expandTimer;

    private int hoveredLandmark = -1;
    private final float[] currentScale = new float[]{1.0f, 1.0f, 1.0f, 1.0f};
    private final float[] targetScale = new float[]{1.0f, 1.0f, 1.0f, 1.0f};

    // Landmark Layout Rectangles (Computed during paint)
    private final Rectangle[] landmarkBounds = new Rectangle[LANDMARK_COUNT];

    // Navigation Bounds
    private final Rectangle backButtonBounds = new Rectangle(28, 22, 120, 36);
    private final Rectangle settingsButtonBounds = new Rectangle(0, 22, 38, 38);
    private final Rectangle fullscreenButtonBounds = new Rectangle(0, 22, 38, 38);
    private BufferedImage settingsIcon;

    public MapSelectPanel(JuanWord app) {
        this.app = app;
        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(GamePalette.DEEP_INDIGO);

        loadAssets();
        initInteractions();

        for (int i = 0; i < LANDMARK_COUNT; i++) {
            landmarkBounds[i] = new Rectangle();
        }
    }

    private void loadAssets() {
        skyBg = toBufferedImage(app.loadGameImage("bg.png"));
        mtApo = toBufferedImage(app.loadGameImage("mtapo.png"));
        indigFrame = toBufferedImage(app.loadGameImage("indigframe.png"));
        settingsIcon = toBufferedImage(app.loadGameImage("Cog Setting.png"));

        // Blue Building (Map 1: 1024x512 raw, cropped [0,0 to 648,512])
        BufferedImage bbRaw = toBufferedImage(app.loadGameImage("bluebuilding.png"));
        if (bbRaw == null) {
            bbRaw = toBufferedImage(app.loadGameImage("Blue Building.png"));
        }
        if (bbRaw != null) {
            int cropW = Math.min(648, bbRaw.getWidth());
            int cropH = Math.min(512, bbRaw.getHeight());
            blueBuildingImg = bbRaw.getSubimage(0, 0, cropW, cropH);
        }

        // Davao Museum (Map 2: raw 1080x1920, cropped [0, 300, 1080, 1620])
        BufferedImage dmRaw = toBufferedImage(app.loadGameImage("durianmuseum.png"));
        if (dmRaw != null) {
            int subY = Math.min(300, dmRaw.getHeight() - 1);
            int subH = Math.min(1620, dmRaw.getHeight() - subY);
            durianMuseumImg = dmRaw.getSubimage(0, subY, dmRaw.getWidth(), subH);
        }

        // San Pedro Cathedral (Map 3: raw 1612x1066, cropped [0, 201, 1612, 865])
        BufferedImage spRaw = toBufferedImage(app.loadGameImage("sanpedro.png"));
        if (spRaw != null) {
            int subY = Math.min(201, spRaw.getHeight() - 1);
            int subH = Math.min(865, spRaw.getHeight() - subY);
            sanPedroImg = spRaw.getSubimage(0, subY, spRaw.getWidth(), subH);
        }

        // Generate high-quality silhouettes for locked representation
        Color silColor = new Color(14, 18, 28, 252);
        Color apoSilColor = new Color(18, 24, 36, 245);
        if (blueBuildingImg != null) silBlueBuilding = createSilhouette(blueBuildingImg, silColor);
        if (durianMuseumImg != null) silDurianMuseum = createSilhouette(durianMuseumImg, silColor);
        if (sanPedroImg != null) silSanPedro = createSilhouette(sanPedroImg, silColor);
        if (mtApo != null) silMtApo = createSilhouette(mtApo, apoSilColor);
    }

    private BufferedImage toBufferedImage(Image img) {
        if (img == null) return null;
        if (img instanceof BufferedImage) return (BufferedImage) img;
        int w = img.getWidth(null);
        int h = img.getHeight(null);
        if (w <= 0 || h <= 0) return null;
        BufferedImage bImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bImg.createGraphics();
        g2.drawImage(img, 0, 0, null);
        g2.dispose();
        return bImg;
    }

    private BufferedImage createSilhouette(BufferedImage src, Color color) {
        if (src == null) return null;
        BufferedImage sil = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = sil.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.setComposite(AlphaComposite.SrcIn);
        g.setColor(color);
        g.fillRect(0, 0, src.getWidth(), src.getHeight());
        g.dispose();
        return sil;
    }

    public void startEntranceAnimation() {
        if (entranceTimer != null && entranceTimer.isRunning()) {
            entranceTimer.stop();
        }
        animProgress = 0.0f;
        // 60 FPS animation timer (approx 600ms duration)
        final long startTime = System.currentTimeMillis();
        final long duration = 650;

        entranceTimer = new Timer(16, e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed >= duration) {
                animProgress = 1.0f;
                entranceTimer.stop();
            } else {
                animProgress = (float) elapsed / duration;
            }
            repaint();
        });
        entranceTimer.start();
    }

    private void initInteractions() {
        // Continuous smooth hover interpolation timer (60 FPS)
        hoverTimer = new Timer(16, e -> {
            boolean needRepaint = false;
            for (int i = 0; i < LANDMARK_COUNT; i++) {
                float target = (i == hoveredLandmark) ? 1.07f : 1.0f;
                targetScale[i] = target;
                if (Math.abs(currentScale[i] - target) > 0.002f) {
                    currentScale[i] += (target - currentScale[i]) * 0.25f;
                    needRepaint = true;
                } else {
                    currentScale[i] = target;
                }
            }
            if (needRepaint) {
                repaint();
            }
        });
        hoverTimer.start();

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (isExpanding) return;
                Point p = e.getPoint();

                // Check Navigation controls first
                if (backButtonBounds.contains(p) || settingsButtonBounds.contains(p) || fullscreenButtonBounds.contains(p)) {
                    if (hoveredLandmark != -1) {
                        hoveredLandmark = -1;
                        repaint();
                    }
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    return;
                }

                int prevHover = hoveredLandmark;
                int newHover = hitTestLandmark(p.x, p.y);

                if (newHover != prevHover) {
                    hoveredLandmark = newHover;
                    if (hoveredLandmark >= 0) {
                        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    } else {
                        setCursor(Cursor.getDefaultCursor());
                    }
                    repaint();
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (isExpanding) return;
                Point p = e.getPoint();

                // Back Button clicked
                if (backButtonBounds.contains(p)) {
                    app.showStartScreen();
                    return;
                }

                // Settings Button clicked
                if (settingsButtonBounds.contains(p)) {
                    app.showSettingsDialog();
                    return;
                }

                // Fullscreen Button clicked
                if (fullscreenButtonBounds.contains(p)) {
                    app.toggleFullScreen();
                    return;
                }

                // Landmark clicked
                int clicked = hitTestLandmark(p.x, p.y);
                if (clicked >= 0) {
                    handleLandmarkClick(clicked);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (hoveredLandmark != -1) {
                    hoveredLandmark = -1;
                    repaint();
                }
                setCursor(Cursor.getDefaultCursor());
            }
        });
    }

    /**
     * Hit tests all 4 landmarks with priority given to foreground buildings,
     * followed by Mount Apo in the background.
     */
    private int hitTestLandmark(int mouseX, int mouseY) {
        int w = getWidth();
        int h = getHeight();

        // 1. Check Foreground Buildings: Blue Building (0), Davao Museum (1), San Pedro (2)
        // Check in visual order of overlap (Davao Museum is central foreground)
        int[] checkOrder = {LANDMARK_DURIAN_MUSEUM, LANDMARK_BLUE_BUILDING, LANDMARK_SAN_PEDRO};
        for (int idx : checkOrder) {
            Rectangle r = landmarkBounds[idx];
            if (r != null && r.contains(mouseX, mouseY)) {
                BufferedImage sprite = getSpriteForLandmark(idx);
                if (sprite != null) {
                    // Check alpha pixel
                    double relX = (double) (mouseX - r.x) / r.width;
                    double relY = (double) (mouseY - r.y) / r.height;
                    int imgX = (int) (relX * sprite.getWidth());
                    int imgY = (int) (relY * sprite.getHeight());
                    if (imgX >= 0 && imgX < sprite.getWidth() && imgY >= 0 && imgY < sprite.getHeight()) {
                        int pixel = sprite.getRGB(imgX, imgY);
                        int alpha = (pixel >> 24) & 0xff;
                        if (alpha > 30) {
                            return idx;
                        }
                    }
                }
            }
        }

        // 2. Check Background Landmark: Mount Apo (3)
        // If not hitting any foreground building, test mountain visible region (y < h * 0.72)
        if (mouseY > 60 && mouseY < h * 0.72) {
            if (mtApo != null) {
                int apoImgX = (int) ((double) mouseX / w * mtApo.getWidth());
                int apoImgY = (int) ((double) mouseY / h * mtApo.getHeight());
                if (apoImgX >= 0 && apoImgX < mtApo.getWidth() && apoImgY >= 0 && apoImgY < mtApo.getHeight()) {
                    int pixel = mtApo.getRGB(apoImgX, apoImgY);
                    int alpha = (pixel >> 24) & 0xff;
                    if (alpha > 40) {
                        return LANDMARK_MT_APO;
                    }
                }
            }
        }

        return -1;
    }

    private BufferedImage getSpriteForLandmark(int idx) {
        switch (idx) {
            case LANDMARK_BLUE_BUILDING: return blueBuildingImg;
            case LANDMARK_DURIAN_MUSEUM: return durianMuseumImg;
            case LANDMARK_SAN_PEDRO: return sanPedroImg;
            case LANDMARK_MT_APO: return mtApo;
            default: return null;
        }
    }

    private void handleLandmarkClick(int landmarkIdx) {
        Player player = app.getCurrentPlayer();
        int maxUnlocked = (player != null) ? player.unlockedMapIndex : 1;

        int requiredMapIdx = landmarkIdx + 1; // 1: Blue Building, 2: Davao Museum, 3: San Pedro, 4: Mt Apo
        boolean isUnlocked = (maxUnlocked >= requiredMapIdx);

        String mapName;
        String predefinedDiff;
        switch (landmarkIdx) {
            case LANDMARK_BLUE_BUILDING:
                mapName = "Blue Building";
                predefinedDiff = "Sayon"; // Easy
                break;
            case LANDMARK_DURIAN_MUSEUM:
                mapName = "Davao Museum";
                predefinedDiff = "Sakto"; // Medium
                break;
            case LANDMARK_SAN_PEDRO:
                mapName = "San Pedro Cathedral";
                predefinedDiff = "Lisod"; // Hard
                break;
            case LANDMARK_MT_APO:
                mapName = "Mt. Apo";
                predefinedDiff = "Extreme"; // Extreme
                break;
            default:
                return;
        }

        if (isUnlocked) {
            // Trigger expand transition, which smoothly scales up the landmark and then starts battle!
            triggerExpandTransition(landmarkIdx, mapName, predefinedDiff);
        } else {
            // Show Dabawenyo themed lock notification
            String prevMapName = (landmarkIdx == LANDMARK_DURIAN_MUSEUM) ? "Blue Building (Easy)"
                               : (landmarkIdx == LANDMARK_SAN_PEDRO) ? "Davao Museum (Medium)"
                               : "San Pedro Cathedral (Hard)";
            String lockMessage = "🔒 NAKA-LOCK PA KINI NGA MAPA!\n\n"
                    + "Kinahanglan nimo pildihon una ang \"" + prevMapName + "\"\n"
                    + "aron maablihan ang bag-ong mapa ug ang predefined difficulty niini (" + predefinedDiff + ").\n\n"
                    + "Padayon sa pagkat-on ug pagduwa, Juan!";

            JOptionPane.showMessageDialog(
                    this,
                    lockMessage,
                    "Mapa Naka-Lock (" + mapName + ")",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    public boolean isExpanding() {
        return isExpanding;
    }

    public float getExpandProgress() {
        return expandProgress;
    }

    public int getExpandingLandmark() {
        return expandingLandmark;
    }

    public void setExpandProgressForTesting(int landmarkIdx, float progress) {
        this.isExpanding = (progress < 1.0f);
        this.expandingLandmark = landmarkIdx;
        this.expandProgress = progress;
        repaint();
    }

    public void triggerExpandTransition(int landmarkIdx, String mapName, String predefinedDiff) {
        if (isExpanding) return;
        isExpanding = true;
        expandingLandmark = landmarkIdx;
        expandProgress = 0.0f;

        // Reset hover and cursor
        hoveredLandmark = -1;
        setCursor(Cursor.getDefaultCursor());

        final int durationMs = 320;
        final int frameDelay = 16; // ~60fps
        final long startTime = System.currentTimeMillis();

        if (expandTimer != null && expandTimer.isRunning()) {
            expandTimer.stop();
        }

        expandTimer = new Timer(frameDelay, e -> {
            long elapsed = System.currentTimeMillis() - startTime;
            float rawT = Math.min(1.0f, (float) elapsed / durationMs);
            expandProgress = rawT;
            repaint();

            if (rawT >= 1.0f) {
                ((Timer) e.getSource()).stop();
                isExpanding = false;
                expandProgress = 0.0f;
                expandingLandmark = -1;
                app.startBattle(mapName, 1, predefinedDiff);
            }
        });
        expandTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth();
        int h = getHeight();
        if (w <= 0 || h <= 0) {
            g2.dispose();
            return;
        }

        // 1. Sky Background (Layer 1: bg.png)
        if (skyBg != null) {
            g2.drawImage(skyBg, 0, 0, w, h, null);
        }

        // Get player progress for lock/unlock states
        Player player = app.getCurrentPlayer();
        int maxUnlocked = (player != null) ? player.unlockedMapIndex : 1;

        // Entrance animation ease-out cubic
        float ease = (float) (1.0 - Math.pow(1.0 - animProgress, 3));
        float maxRiseOffset = h * 0.72f;
        int animOffset = (int) (maxRiseOffset * (1.0f - ease));

        float S = Math.min((float) w / 960f, (float) h / 540f);
        int stageW = Math.min(w, (int) (h * 16.0 / 9.0));
        int stageOffset = (w - stageW) / 2;

        // Compute base layout coordinates for all 4 landmarks
        // Map 1: Blue Building
        boolean isBbUnlocked = (maxUnlocked >= 1);
        BufferedImage bbSprite = isBbUnlocked ? blueBuildingImg : (silBlueBuilding != null ? silBlueBuilding : blueBuildingImg);
        int bbH = (int) (h * 0.44);
        int bbW = (bbSprite != null) ? (int) (bbH * (bbSprite.getWidth() / (double) bbSprite.getHeight())) : (int) (stageW * 0.28);
        int bbX = stageOffset + (int) (stageW * 0.025);
        int bbY = h - bbH - (int) (18 * S) + animOffset;
        landmarkBounds[LANDMARK_BLUE_BUILDING].setBounds(bbX, bbY, bbW, bbH);

        // Map 2: Davao Museum
        boolean isDmUnlocked = (maxUnlocked >= 2);
        BufferedImage dmSprite = isDmUnlocked ? durianMuseumImg : (silDurianMuseum != null ? silDurianMuseum : durianMuseumImg);
        int dmW = (int) (stageW * 0.32);
        int dmH = (int) (h * 0.46);
        int dmX = stageOffset + (stageW - dmW) / 2 - (int) (10 * S);
        int dmY = h - dmH - (int) (18 * S) + animOffset;
        landmarkBounds[LANDMARK_DURIAN_MUSEUM].setBounds(dmX, dmY, dmW, dmH);

        // Map 3: San Pedro Cathedral
        boolean isSpUnlocked = (maxUnlocked >= 3);
        BufferedImage spSprite = isSpUnlocked ? sanPedroImg : (silSanPedro != null ? silSanPedro : sanPedroImg);
        int spH = (int) (h * 0.48);
        int spW = (spSprite != null) ? (int) (spH * (spSprite.getWidth() / (double) spSprite.getHeight())) : (int) (stageW * 0.30);
        int spX = stageOffset + stageW - spW + (int) (50 * S);
        int spY = h - spH - (int) (18 * S) + animOffset;
        landmarkBounds[LANDMARK_SAN_PEDRO].setBounds(spX, spY, spW, spH);

        // Map 4: Mount Apo
        boolean isMtApoUnlocked = (maxUnlocked >= 4);
        BufferedImage apoSprite = isMtApoUnlocked ? mtApo : (silMtApo != null ? silMtApo : mtApo);

        if (!isExpanding) {
            // Normal Map Selection Rendering
            // 2. Mount Apo at the Back Layer
            if (apoSprite != null) {
                float apoScale = currentScale[LANDMARK_MT_APO];
                if (apoScale > 1.001f) {
                    int sw = (int) (w * apoScale);
                    int sh = (int) (h * apoScale);
                    g2.drawImage(apoSprite, (w - sw) / 2, (h - sh) / 2, sw, sh, null);
                } else {
                    g2.drawImage(apoSprite, 0, 0, w, h, null);
                }

                if (!isMtApoUnlocked) {
                    int apoLockSize = 34;
                    int apoLockX = w / 2 - apoLockSize / 2;
                    int apoLockY = (int) (h * 0.28);
                    drawLockEmblem(g2, apoLockX, apoLockY, apoLockSize);
                }
            }

            // 3. Foreground Buildings
            if (bbSprite != null) {
                drawBuilding(g2, bbSprite, bbX, bbY, bbW, bbH, currentScale[LANDMARK_BLUE_BUILDING], isBbUnlocked);
            }
            if (dmSprite != null) {
                drawBuilding(g2, dmSprite, dmX, dmY, dmW, dmH, currentScale[LANDMARK_DURIAN_MUSEUM], isDmUnlocked);
            }
            if (spSprite != null) {
                drawBuilding(g2, spSprite, spX, spY, spW, spH, currentScale[LANDMARK_SAN_PEDRO], isSpUnlocked);
            }

            // 4. Indigenous Frame (Top layer framing the viewport)
            if (indigFrame != null) {
                g2.drawImage(indigFrame, 0, 0, w, h, null);
            }

            // 5. Navigation & HUD Overlay
            drawNavigationAndHUD(g2, w, h, maxUnlocked);
        } else {
            // EXPAND TRANSITION ANIMATION:
            // The selected icon smoothly expands into the battle background position,
            // while non-selected landmarks and HUD fade away!
            float t = (float) (1.0 - Math.pow(1.0 - expandProgress, 3)); // ease-out cubic
            float fadeAlpha = Math.max(0.0f, 1.0f - expandProgress * 2.2f);

            // A. Draw non-selected landmarks fading out
            if (fadeAlpha > 0.005f) {
                Graphics2D gFade = (Graphics2D) g2.create();
                gFade.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha));

                if (expandingLandmark != LANDMARK_MT_APO && apoSprite != null) {
                    gFade.drawImage(apoSprite, 0, 0, w, h, null);
                    if (!isMtApoUnlocked) {
                        drawLockEmblem(gFade, w / 2 - 17, (int) (h * 0.28), 34);
                    }
                }
                if (expandingLandmark != LANDMARK_BLUE_BUILDING && bbSprite != null) {
                    drawBuilding(gFade, bbSprite, bbX, bbY, bbW, bbH, 1.0f, isBbUnlocked);
                }
                if (expandingLandmark != LANDMARK_DURIAN_MUSEUM && dmSprite != null) {
                    drawBuilding(gFade, dmSprite, dmX, dmY, dmW, dmH, 1.0f, isDmUnlocked);
                }
                if (expandingLandmark != LANDMARK_SAN_PEDRO && spSprite != null) {
                    drawBuilding(gFade, spSprite, spX, spY, spW, spH, 1.0f, isSpUnlocked);
                }
                gFade.dispose();
            }

            // B. Draw expanding landmark scaling up and morphing into Battle background position
            BufferedImage activeSprite = getSpriteForLandmark(expandingLandmark);
            if (activeSprite != null) {
                int startX, startY, startW, startH;
                int targetX, targetY, targetW, targetH;

                if (expandingLandmark == LANDMARK_MT_APO) {
                    startX = 0; startY = 0; startW = w; startH = h;
                    targetW = (int) (w * 1.15f); targetH = (int) (h * 1.15f);
                    targetX = (w - targetW) / 2; targetY = (h - targetH) / 2;
                } else {
                    if (expandingLandmark == LANDMARK_BLUE_BUILDING) {
                        startX = bbX; startY = bbY; startW = bbW; startH = bbH;
                    } else if (expandingLandmark == LANDMARK_DURIAN_MUSEUM) {
                        startX = dmX; startY = dmY; startW = dmW; startH = dmH;
                    } else {
                        startX = spX; startY = spY; startW = spW; startH = spH;
                    }

                    int imgW = activeSprite.getWidth();
                    int imgH = activeSprite.getHeight();
                    double scale = Math.min((double) (w - 100) / imgW, (double) (h - 60) / imgH) * 1.08;
                    targetW = (int) Math.round(imgW * scale);
                    targetH = (int) Math.round(imgH * scale);
                    targetX = (w - targetW) / 2;
                    targetY = h - targetH - 22;
                }

                int curX = (int) (startX + (targetX - startX) * t);
                int curY = (int) (startY + (targetY - startY) * t);
                int curW = (int) (startW + (targetW - startW) * t);
                int curH = (int) (startH + (targetH - startH) * t);

                g2.drawImage(activeSprite, curX, curY, curW, curH, null);
            }

            // C. Indigenous Border Frame stays crisp on top of the landmark
            if (indigFrame != null) {
                g2.drawImage(indigFrame, 0, 0, w, h, null);
            }

            // D. Soft battle scrim fading in as transition completes
            if (expandProgress > 0.20f) {
                float scrimProgress = (expandProgress - 0.20f) / 0.80f;
                int alpha = (int) (scrimProgress * 95);
                g2.setColor(new Color(12, 16, 52, Math.min(95, alpha)));
                g2.fillRect(0, 0, w, h);
            }

            // E. Navigation & HUD fade out rapidly
            float hudAlpha = Math.max(0.0f, 1.0f - expandProgress * 2.8f);
            if (hudAlpha > 0.01f) {
                Graphics2D gHud = (Graphics2D) g2.create();
                gHud.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, hudAlpha));
                drawNavigationAndHUD(gHud, w, h, maxUnlocked);
                gHud.dispose();
            }
        }

        g2.dispose();
    }

    private void drawBuilding(Graphics2D g2, BufferedImage img, int x, int y, int w, int h, float scale, boolean unlocked) {
        int drawW = (int) (w * scale);
        int drawH = (int) (h * scale);
        int drawX = x - (drawW - w) / 2;
        int drawY = y - (drawH - h); // Grounded baseline

        if (scale > 1.002f) {
            // Luminous ambient glow on hover
            Graphics2D gGlow = (Graphics2D) g2.create();
            gGlow.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, unlocked ? 0.35f : 0.20f));
            gGlow.drawImage(img, drawX - 3, drawY - 3, drawW + 6, drawH + 6, null);
            gGlow.dispose();
        }

        g2.drawImage(img, drawX, drawY, drawW, drawH, null);

        // If locked, render the lock emblem badge centered over the silhouette
        if (!unlocked) {
            int lockSize = 36;
            int lockX = drawX + (drawW - lockSize) / 2;
            int lockY = drawY + (drawH - lockSize) / 2;
            drawLockEmblem(g2, lockX, lockY, lockSize);
        }
    }

    private void drawLockEmblem(Graphics2D g2, int lockX, int lockY, int lockSize) {
        Graphics2D gLock = (Graphics2D) g2.create();
        gLock.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gLock.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Lock icon rendered cleanly over the silhouette without the yellow circular badge
        int fontSize = Math.max(16, (int) (lockSize * 0.78));
        gLock.setFont(new Font("Segoe UI Emoji", Font.PLAIN, fontSize));
        // Soft drop shadow for crisp depth
        gLock.setColor(new Color(0, 0, 0, 180));
        gLock.drawString("🔒", lockX + 3, lockY + fontSize + 1);
        gLock.setColor(Color.WHITE);
        gLock.drawString("🔒", lockX + 2, lockY + fontSize);
        gLock.dispose();
    }

    private void drawNavigationAndHUD(Graphics2D g2, int w, int h, int maxUnlocked) {
        float uiScale = Math.max(0.85f, Math.min(2.0f, Math.min((float) w / 960f, (float) h / 540f)));
        int stageW = Math.min(w, (int) (h * 16.0 / 9.0));
        int stageOffset = (w - stageW) / 2;
        int barY = Math.max(16, (int) (22 * uiScale));

        // --- BACK BUTTON ---
        int backW = (int) (120 * uiScale);
        int backH = (int) (36 * uiScale);
        backButtonBounds.setBounds(stageOffset + (int) (28 * uiScale), barY, backW, backH);
        boolean backHovered = backButtonBounds.contains(getMousePositionSafe());
        g2.setColor(backHovered ? new Color(42, 54, 88, 235) : new Color(24, 32, 54, 210));
        g2.fillRoundRect(backButtonBounds.x, backButtonBounds.y, backButtonBounds.width, backButtonBounds.height, (int) (12 * uiScale), (int) (12 * uiScale));
        g2.setColor(backHovered ? new Color(255, 215, 100) : new Color(242, 190, 75));
        g2.setStroke(new BasicStroke(1.5f * uiScale));
        g2.drawRoundRect(backButtonBounds.x, backButtonBounds.y, backButtonBounds.width, backButtonBounds.height, (int) (12 * uiScale), (int) (12 * uiScale));
        g2.setFont(new Font("Arial", Font.BOLD, Math.max(11, (int) (13 * uiScale))));
        g2.setColor(Color.WHITE);
        g2.drawString("← Balik (Back)", backButtonBounds.x + (int) (10 * uiScale), backButtonBounds.y + (int) (23 * uiScale));

        // --- SETTINGS BUTTON (Right) ---
        int setSize = (int) (38 * uiScale);
        int setX = stageOffset + stageW - setSize - (int) (28 * uiScale);
        settingsButtonBounds.setBounds(setX, barY, setSize, setSize);
        boolean setHovered = settingsButtonBounds.contains(getMousePositionSafe());
        g2.setColor(setHovered ? new Color(42, 54, 88, 235) : new Color(24, 32, 54, 210));
        g2.fillRoundRect(setX, barY, setSize, setSize, (int) (12 * uiScale), (int) (12 * uiScale));
        g2.setColor(setHovered ? new Color(255, 215, 100) : new Color(242, 190, 75));
        g2.drawRoundRect(setX, barY, setSize, setSize, (int) (12 * uiScale), (int) (12 * uiScale));
        if (settingsIcon != null) {
            int iconInset = (int) (6 * uiScale);
            int iconSize = setSize - iconInset * 2;
            g2.drawImage(settingsIcon, setX + iconInset, barY + iconInset, iconSize, iconSize, null);
        } else {
            g2.setFont(new Font("Segoe UI Symbol", Font.PLAIN, (int) (18 * uiScale)));
            g2.drawString("⚙", setX + (int) (11 * uiScale), barY + (int) (25 * uiScale));
        }

        // --- FULLSCREEN TOGGLE BUTTON ---
        int fsX = setX - setSize - (int) (8 * uiScale);
        fullscreenButtonBounds.setBounds(fsX, barY, setSize, setSize);
        boolean fsHovered = fullscreenButtonBounds.contains(getMousePositionSafe());
        g2.setColor(fsHovered ? new Color(42, 54, 88, 235) : new Color(24, 32, 54, 210));
        g2.fillRoundRect(fsX, barY, setSize, setSize, (int) (12 * uiScale), (int) (12 * uiScale));
        g2.setColor(fsHovered ? new Color(255, 215, 100) : new Color(242, 190, 75));
        g2.drawRoundRect(fsX, barY, setSize, setSize, (int) (12 * uiScale), (int) (12 * uiScale));
        g2.setFont(new Font("Segoe UI Symbol", Font.BOLD, Math.max(13, (int) (16 * uiScale))));
        g2.setColor(Color.WHITE);
        String fsIcon = app.isFullScreen() ? "🗗" : "⛶";
        int fsTw = g2.getFontMetrics().stringWidth(fsIcon);
        g2.drawString(fsIcon, fsX + (setSize - fsTw) / 2, barY + (int) (25 * uiScale));

        // --- CENTER TITLE BADGE ---
        int titleW = (int) (290 * uiScale);
        int titleH = (int) (36 * uiScale);
        int titleX = (w - titleW) / 2;
        g2.setColor(new Color(24, 32, 54, 210));
        g2.fillRoundRect(titleX, barY, titleW, titleH, (int) (12 * uiScale), (int) (12 * uiScale));
        g2.setColor(new Color(242, 190, 75));
        g2.drawRoundRect(titleX, barY, titleW, titleH, (int) (12 * uiScale), (int) (12 * uiScale));
        g2.setFont(new Font("Arial", Font.BOLD, Math.max(12, (int) (14 * uiScale))));
        g2.setColor(new Color(254, 240, 200));
        String title = "PILIA ANG MAPA  •  MAP SELECT";
        int tw = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (w - tw) / 2, barY + (int) (23 * uiScale));

        // --- DYNAMIC HOVER HUD BADGE (LEVEL BANNER) ---
        if (hoveredLandmark >= 0) {
            String mapName;
            String diffName;
            Color bColor;

            if (hoveredLandmark == LANDMARK_BLUE_BUILDING) {
                mapName = "BLUE BUILDING";
                diffName = "EASY";
                bColor = new Color(110, 230, 130);
            } else if (hoveredLandmark == LANDMARK_DURIAN_MUSEUM) {
                mapName = "DAVAO MUSEUM";
                diffName = "MEDIUM";
                bColor = new Color(245, 175, 75);
            } else if (hoveredLandmark == LANDMARK_SAN_PEDRO) {
                mapName = "SAN PEDRO CATHEDRAL";
                diffName = "HARD";
                bColor = new Color(255, 120, 90);
            } else {
                mapName = "MT. APO";
                diffName = "EXTREME";
                bColor = new Color(185, 120, 255);
            }

            int bw = (int) (290 * uiScale);
            int bh = (int) (46 * uiScale);
            int bx = (w - bw) / 2;
            int by = barY + titleH + (int) (8 * uiScale);

            g2.setColor(new Color(15, 20, 36, 235));
            g2.fillRoundRect(bx, by, bw, bh, (int) (14 * uiScale), (int) (14 * uiScale));
            g2.setColor(bColor);
            g2.setStroke(new BasicStroke(1.8f * uiScale));
            g2.drawRoundRect(bx, by, bw, bh, (int) (14 * uiScale), (int) (14 * uiScale));

            g2.setFont(new Font("Arial", Font.BOLD, Math.max(12, (int) (14 * uiScale))));
            g2.setColor(new Color(255, 248, 233));
            int nw = g2.getFontMetrics().stringWidth(mapName);
            g2.drawString(mapName, (w - nw) / 2, by + (int) (20 * uiScale));

            g2.setFont(new Font("Arial", Font.BOLD, Math.max(10, (int) (12 * uiScale))));
            g2.setColor(bColor);
            int dw = g2.getFontMetrics().stringWidth(diffName);
            g2.drawString(diffName, (w - dw) / 2, by + (int) (37 * uiScale));
        }
    }

    private Point getMousePositionSafe() {
        Point p = getMousePosition();
        return (p != null) ? p : new Point(-100, -100);
    }
}
