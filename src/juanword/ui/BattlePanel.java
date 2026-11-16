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
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;
import juanword.JuanWord;
import juanword.data.DataManager;
import juanword.model.AttackType;
import juanword.model.BattleContext;
import juanword.model.BattleOutcome;
import juanword.model.BattleSession;
import juanword.model.BattleSessionBuilder;
import juanword.model.Player;
import juanword.tile.TileComponent;
import juanword.tile.TileGridGenerator;

public final class BattlePanel extends JPanel {
    // --- Dabawenyo Game Palette ---
    private static final Color MIDNIGHT_NAVY = new Color(15, 23, 42);
    private static final Color ARENA_BOX_BG = new Color(24, 32, 68, 235);
    private static final Color ARENA_BOX_BORDER = new Color(60, 80, 140);
    private static final Color CERULEAN = new Color(64, 180, 200);
    private static final Color DARK_BLUE_BTN = new Color(20, 36, 100);
    private static final Color YELLOW_BTN = new Color(232, 214, 98);
    private static final Color YELLOW_BTN_HOVER = new Color(245, 228, 120);
    private static final Color YELLOW_BTN_BORDER = new Color(160, 140, 40);
    private static final Color CREAM_TILE = new Color(254, 252, 246);
    private static final Color CREAM_TILE_SELECTED = new Color(255, 236, 160);
    private static final Color TILE_BORDER = new Color(185, 175, 155);
    private static final Color TILE_BORDER_SELECTED = new Color(212, 145, 20);
    private static final Color HP_GREEN = new Color(52, 168, 83);
    private static final Color HP_ORANGE = new Color(242, 133, 0);

    private final JuanWord host;
    private BattleSession session;
    private Timer loop;
    private final List<int[]> selectedPath = new ArrayList<>();
    private final List<FloatingText> floatingTexts = new ArrayList<>();
    private EnemyAnimator enemyAnimator;
    private PlayerAnimator playerAnimator;
    private final List<AttackEffect> attackEffects = new ArrayList<>();
    private Runnable pendingImpactAction;

    // Assets
    private BufferedImage skyBg;
    private BufferedImage indigFrame;
    private BufferedImage landmarkIcon;
    private BufferedImage playerSprite;
    private BufferedImage playerPreAttackSprite;
    private BufferedImage playerAttackSprite;
    private BufferedImage enemySprite;

    private int tickAccumulatorMs;
    private boolean paused;
    private boolean inputLocked;
    private int uiShakeFrames;
    private BattleOutcome outcome = BattleOutcome.ONGOING;
    private String dialogueText = "Nagsugod na ang gubat! Pagpili ug mga letra gikan sa ubos para makatake.";

    // Interactive button bounds
    private final Rectangle backToMapBounds = new Rectangle(38, 20, 130, 32);
    private final Rectangle backTileBtnBounds = new Rectangle(230, 420, 110, 38);
    private final Rectangle clearBtnBounds = new Rectangle(230, 468, 110, 38);
    private final Rectangle attackBtnBounds = new Rectangle(565, 335, 175, 42);
    private final Rectangle scrambleBtnBounds = new Rectangle(620, 420, 110, 38);
    private final Rectangle[][] tileBounds = new Rectangle[4][4];

    public BattlePanel(JuanWord host) {
        this.host = host;
        this.playerAnimator = new PlayerAnimator(85, 245);
        this.enemyAnimator = new EnemyAnimator();
        this.loop = new Timer(16, this::gameTick);

        // Preload constant frames
        this.skyBg = ImageLoader.load("bg.png");
        this.indigFrame = ImageLoader.load("indigframe.png");

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onMouseClicked(e);
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                onMouseMoved(e);
            }
        });
    }

    public void begin(BattleSession s) {
        this.session = s;
        this.outcome = BattleOutcome.ONGOING;
        this.paused = false;
        this.inputLocked = false;
        this.tickAccumulatorMs = 0;
        this.uiShakeFrames = 0;
        this.selectedPath.clear();
        this.floatingTexts.clear();
        this.attackEffects.clear();
        this.pendingImpactAction = null;
        this.dialogueText = "Nagsugod na ang gubat! Pagpili ug mga letra gikan sa ubos para makatake.";

        // Landmark artwork for center watermark
        String lmName = switch (session.getMapIndex()) {
            case 1 -> "Blue Building.png";
            case 2 -> "durianmuseum.png";
            case 3 -> "sanpedro.png";
            case 4 -> "mtapo.png";
            default -> "Blue Building.png";
        };
        this.landmarkIcon = ImageLoader.load(lmName);
        this.playerSprite = ImageLoader.load("maincharacteridle.png");
        this.playerPreAttackSprite = ImageLoader.load("maincharapreattack.png");
        this.playerAttackSprite = ImageLoader.load("maincharattack.png");
        this.enemySprite = ImageLoader.load(session.getEnemy().getSpritePath());

        this.playerAnimator = new PlayerAnimator(85, 245);
        this.enemyAnimator = new EnemyAnimator();
        this.enemyAnimator.startEntrance(815);

        if (!loop.isRunning()) {
            loop.start();
        }
        recalculateLayout();
        repaint();
    }

    private void recalculateLayout() {
        int w = getWidth() > 0 ? getWidth() : 960;
        int h = getHeight() > 0 ? getHeight() : 540;

        // Scale factors relative to 960x540
        double scaleX = w / 960.0;
        double scaleY = h / 540.0;
        double S = Math.min(scaleX, scaleY);

        // Tile grid dimensions
        int tileSize = (int) Math.round(50 * S);
        int tileGap = (int) Math.round(6 * S);
        int totalGridW = 4 * tileSize + 3 * tileGap;
        int gridX = (w - totalGridW) / 2;
        int gridY = (int) Math.round(296 * scaleY);

        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                tileBounds[r][c] = new Rectangle(
                    gridX + c * (tileSize + tileGap),
                    gridY + r * (tileSize + tileGap),
                    tileSize, tileSize
                );
            }
        }

        // Left control buttons
        int btnW = (int) Math.round(112 * scaleX);
        int btnH = (int) Math.round(34 * scaleY);
        backTileBtnBounds.setBounds(gridX - btnW - (int) (20 * scaleX), gridY + (int) (54 * scaleY), btnW, btnH);
        clearBtnBounds.setBounds(gridX - btnW - (int) (20 * scaleX), gridY + (int) (98 * scaleY), btnW, btnH);

        // Right control buttons
        scrambleBtnBounds.setBounds(gridX + totalGridW + (int) (20 * scaleX), gridY + (int) (54 * scaleY), btnW, btnH);

        // Top back to map button
        backToMapBounds.setBounds((int) (24 * scaleX), (int) (14 * scaleY), (int) (145 * scaleX), (int) (34 * scaleY));

        // Arena card bounds & attack button
        int cardX = (int) (20 * scaleX);
        int cardY = (int) (54 * scaleY);
        int cardW = w - 2 * cardX;
        int juanW = (int) (185 * scaleX);
        int enemyW = (int) (185 * scaleX);
        int centerX = cardX + juanW + (int) (10 * scaleX);
        int centerW = cardW - juanW - enemyW - (int) (20 * scaleX);
        int actY = cardY + (int) (172 * scaleY);
        int actH = (int) (44 * scaleY);
        int prevW = (int) (centerW * 0.66);
        int atkW = centerW - prevW - (int) (8 * scaleX);
        attackBtnBounds.setBounds(centerX + prevW + (int) (8 * scaleX), actY, atkW, actH);
    }

    private void gameTick(ActionEvent e) {
        if (paused) {
            repaint();
            return;
        }

        playerAnimator.update(16);
        enemyAnimator.update(16);

        for (AttackEffect ae : attackEffects) ae.update(16);
        attackEffects.removeIf(AttackEffect::isFinished);

        for (FloatingText ft : floatingTexts) ft.update(16);
        floatingTexts.removeIf(FloatingText::isExpired);

        if (uiShakeFrames > 0) {
            uiShakeFrames--;
        }

        if (playerAnimator.consumeImpact()) {
            if (pendingImpactAction != null) {
                Runnable act = pendingImpactAction;
                pendingImpactAction = null;
                act.run();
            }
        }
        if (enemyAnimator.consumeImpact()) {
            if (pendingImpactAction != null) {
                Runnable act = pendingImpactAction;
                pendingImpactAction = null;
                act.run();
            }
        }

        // Count turn timer
        tickAccumulatorMs += 16;
        if (tickAccumulatorMs >= 1000) {
            tickAccumulatorMs -= 1000;
            if (session != null && session.getDifficulty().isTimed() && !inputLocked) {
                int sec = session.getTurnSeconds() - 1;
                session.setTurnSeconds(sec);
                if (sec <= 0) {
                    onTurnTimerExpired();
                }
            }
        }

        outcome = evaluateOutcome();
        if (outcome == BattleOutcome.VICTORY) {
            onVictory();
            return;
        } else if (outcome == BattleOutcome.DEFEAT) {
            onDefeat();
            return;
        }

        repaint();
    }

    private void onMouseMoved(MouseEvent e) {
        Point p = e.getPoint();
        boolean hover = backToMapBounds.contains(p) || attackBtnBounds.contains(p)
            || backTileBtnBounds.contains(p) || clearBtnBounds.contains(p) || scrambleBtnBounds.contains(p);

        if (!hover) {
            for (int r = 0; r < 4; r++) {
                for (int c = 0; c < 4; c++) {
                    if (tileBounds[r][c] != null && tileBounds[r][c].contains(p)) {
                        hover = true;
                        break;
                    }
                }
            }
        }
        setCursor(hover ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
    }

    private void onMouseClicked(MouseEvent e) {
        if (inputLocked || paused) return;
        Point p = e.getPoint();

        if (backToMapBounds.contains(p)) {
            saveMidBattle();
            loop.stop();
            host.returnToMap();
            return;
        }
        if (attackBtnBounds.contains(p)) {
            onSubmit();
            return;
        }
        if (backTileBtnBounds.contains(p)) {
            if (!selectedPath.isEmpty()) {
                selectedPath.remove(selectedPath.size() - 1);
                repaint();
            }
            return;
        }
        if (clearBtnBounds.contains(p)) {
            onClear();
            return;
        }
        if (scrambleBtnBounds.contains(p)) {
            BattleContext ctx = new BattleContext(session, "", (txt, col) -> spawnFloatingText(txt, new Point(getWidth() / 2, 340), col));
            ctx.scrambleGrid();
            selectedPath.clear();
            dialogueText = "Gibag-o ang tanang mga letra sa grid!";
            repaint();
            return;
        }

        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                if (tileBounds[r][c] != null && tileBounds[r][c].contains(p)) {
                    onTileClicked(r, c);
                    return;
                }
            }
        }
    }

    private void onTileClicked(int row, int col) {
        int existingIndex = -1;
        for (int i = 0; i < selectedPath.size(); i++) {
            int[] pos = selectedPath.get(i);
            if (pos[0] == row && pos[1] == col) {
                existingIndex = i;
                break;
            }
        }
        if (existingIndex >= 0) {
            while (selectedPath.size() > existingIndex) {
                selectedPath.remove(selectedPath.size() - 1);
            }
        } else {
            selectedPath.add(new int[]{row, col});
        }
        repaint();
    }

    private void onClear() {
        selectedPath.clear();
        repaint();
    }

    private void onSubmit() {
        if (selectedPath.isEmpty()) return;
        String word = getSelectedWord();
        submitWord(word);
    }

    private String getSelectedWord() {
        StringBuilder sb = new StringBuilder();
        TileComponent[][] grid = session.getGrid();
        for (int[] pos : selectedPath) {
            sb.append(grid[pos[0]][pos[1]].getLetter());
        }
        return sb.toString();
    }

    void submitWord(String word) {
        if (DataManager.getInstance().isValidCebuanoWord(word)) {
            List<TileComponent> tiles = new ArrayList<>();
            TileComponent[][] grid = session.getGrid();
            for (int[] pos : selectedPath) {
                tiles.add(grid[pos[0]][pos[1]]);
            }
            resolveValidWord(tiles);
        } else {
            handleInvalidWord();
        }
    }

    private int computeDamage(List<TileComponent> tiles) {
        int sum = 0;
        for (TileComponent t : tiles) {
            sum += t.getPoints();
        }
        double mult = session.getDifficulty().getLengthMultiplier(tiles.size());
        return Math.max(1, (int) Math.round(sum * mult));
    }

    private void resolveValidWord(List<TileComponent> tiles) {
        int dmg = computeDamage(tiles);
        String word = getSelectedWord();
        dialogueText = "Giatake nimo si " + session.getEnemy().getName() + " gamit ang pulong '" + word + "' (" + dmg + " DMG)!";
        playPlayerAttack(() -> onPlayerImpact(dmg));

        BattleContext ctx = new BattleContext(session, word, (txt, col) -> spawnFloatingText(txt, getPlayerCenter(), col));
        for (TileComponent t : tiles) {
            t.onWordSubmitted(ctx);
        }

        refillConsumedTiles(selectedPath);
        selectedPath.clear();
    }

    private void playPlayerAttack(Runnable onImpact) {
        inputLocked = true;
        this.pendingImpactAction = onImpact;
        playerAnimator.startAttack(AttackType.SLASH);
    }

    private void onPlayerImpact(int damage) {
        enemyAnimator.triggerHit();
        Point ep = getEnemyCenter();
        attackEffects.add(new AttackEffect(AttackType.SLASH, ep.x, ep.y));
        session.getEnemy().takeDamage(damage);
        spawnFloatingText("-" + damage, ep, new Color(255, 140, 0));

        if (session.getEnemy().isDefeated()) {
            outcome = BattleOutcome.VICTORY;
        } else {
            enemyTurn();
        }
    }

    private void playEnemyAttack() {
        inputLocked = true;
        AttackType type = session.getEnemy().getAttackType();
        this.pendingImpactAction = this::onEnemyImpact;
        enemyAnimator.startAttack(type);
    }

    private void onEnemyImpact() {
        playerAnimator.triggerHit();
        Point pp = getPlayerCenter();
        attackEffects.add(new AttackEffect(session.getEnemy().getAttackType(), pp.x, pp.y));
        dialogueText = session.getEnemy().getName() + " mibalos ug atake (" + session.getEnemy().getAttack() + " ATK)!";
        BattleContext ctx = new BattleContext(session, "", (txt, col) -> spawnFloatingText(txt, getEnemyCenter(), col));
        session.getEnemy().onEnemyTurn(ctx);

        if (session.getPlayerHp() <= 0) {
            outcome = BattleOutcome.DEFEAT;
        } else {
            inputLocked = false;
            session.setTurnSeconds(session.getDifficulty().getTurnTimeSeconds());
        }
    }

    private void handleInvalidWord() {
        uiShakeFrames = 15;
        dialogueText = "Sayop nga pulong! Dili kini anaa sa diksyonaryo sa Bisaya.";
        spawnFloatingText("Sayop!", new Point(getWidth() / 2, 280), Color.RED);

        int counterDmg = session.getDifficulty().getInvalidCounterDamage();
        if (counterDmg > 0) {
            session.setPlayerHp(session.getPlayerHp() - counterDmg);
            spawnFloatingText("-" + counterDmg + " (Sayop)", getPlayerCenter(), Color.RED);
            if (session.getPlayerHp() <= 0) {
                outcome = BattleOutcome.DEFEAT;
            }
        }
        selectedPath.clear();
        repaint();
    }

    private void onTurnTimerExpired() {
        selectedPath.clear();
        dialogueText = "Nahurot ang oras! Turno na sa kontra.";
        spawnFloatingText("Nahurot ang Oras!", new Point(getWidth() / 2, 280), Color.YELLOW);
        playEnemyAttack();
    }

    private void enemyTurn() {
        Timer timer = new Timer(400, ev -> {
            ((Timer) ev.getSource()).stop();
            playEnemyAttack();
        });
        timer.setRepeats(false);
        timer.start();
    }

    private void refillConsumedTiles(List<int[]> used) {
        TileGridGenerator.refill(session.getGrid(), used);
    }

    private BattleOutcome evaluateOutcome() {
        if (session != null && session.getEnemy().isDefeated()) {
            return BattleOutcome.VICTORY;
        }
        if (session != null && session.getPlayerHp() <= 0) {
            return BattleOutcome.DEFEAT;
        }
        return BattleOutcome.ONGOING;
    }

    private void onVictory() {
        loop.stop();
        int scoreAward = 100 * session.getStageIndex();
        session.getPlayer().addScore(scoreAward);
        session.getPlayer().recordDefeat(session.getEnemy().getEnemyId());

        if (session.isBossStage()) {
            if (session.getMapIndex() == session.getPlayer().getUnlockedMap() && session.getMapIndex() < 4) {
                session.getPlayer().unlockMap(session.getMapIndex() + 1);
            }
            DataManager.getInstance().clearActiveBattle(session.getPlayer());
            DataManager.getInstance().savePlayers();

            if (session.isFinalBoss()) {
                JOptionPane.showMessageDialog(this, "NADAUG KA SA TANANG MAPA!\nCongratulations Juan!", "DAUG!", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "NADAUG ANG BOSS!\nNaabli ang sunod nga mapa!", "DAUG!", JOptionPane.INFORMATION_MESSAGE);
            }
            host.returnToMap();
        } else {
            DataManager.getInstance().clearActiveBattle(session.getPlayer());
            DataManager.getInstance().savePlayers();
            int res = JOptionPane.showConfirmDialog(
                this,
                "NADAUG!\nGusto ba nimong mopadayon sa Stage " + (session.getStageIndex() + 1) + "?",
                "Stage Clear",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE
            );
            if (res == JOptionPane.YES_OPTION) {
                BattleSession nextSession = new BattleSessionBuilder()
                    .withPlayer(session.getPlayer())
                    .onMap(session.getMapIndex())
                    .atStage(session.getStageIndex() + 1)
                    .withSavedHealth(session.getPlayerHp(), null)
                    .build();
                begin(nextSession);
            } else {
                host.returnToMap();
            }
        }
    }

    private void onDefeat() {
        loop.stop();
        DataManager.getInstance().clearActiveBattle(session.getPlayer());
        DataManager.getInstance().savePlayers();
        JOptionPane.showMessageDialog(this, "NAPILDE KA!\nSulayi pag-usab.", "Pilde", JOptionPane.ERROR_MESSAGE);
        host.returnToMap();
    }

    public void pauseGame() { this.paused = true; }
    public void resumeGame() { this.paused = false; }

    public EnemyAnimator getEnemyAnimator() { return enemyAnimator; }
    public PlayerAnimator getPlayerAnimator() { return playerAnimator; }

    public void saveMidBattle() {
        if (isBattleActive()) {
            DataManager.getInstance().saveActiveBattle(session.getPlayer(), session);
        }
    }

    public boolean isBattleActive() {
        return session != null && outcome == BattleOutcome.ONGOING
            && !session.getEnemy().isDefeated() && session.getPlayerHp() > 0;
    }

    private void spawnFloatingText(String text, Point at, Color c) {
        floatingTexts.add(new FloatingText(text, at.x, at.y, c));
    }

    private Point getPlayerCenter() { return new Point(140, 310); }
    private Point getEnemyCenter() { return new Point(810, 310); }

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

        // Screen shake
        if (uiShakeFrames > 0) {
            int sx = (int) (Math.sin(uiShakeFrames * 1.5) * 6);
            g2.translate(sx, 0);
        }

        // 1. Layer 1: Sky Background
        if (skyBg != null) {
            g2.drawImage(skyBg, 0, 0, w, h, null);
        } else {
            g2.setColor(MIDNIGHT_NAVY);
            g2.fillRect(0, 0, w, h);
        }

        // 2. Layer 2: Landmark Watermark in Arena
        if (landmarkIcon != null) {
            int imgW = landmarkIcon.getWidth();
            int imgH = landmarkIcon.getHeight();
            if (imgW > 0 && imgH > 0) {
                double scale = Math.min((double) (w - 120) / imgW, (double) (h - 70) / imgH) * 1.05;
                int drawW = (int) Math.round(imgW * scale);
                int drawH = (int) Math.round(imgH * scale);
                int drawX = (w - drawW) / 2;
                int drawY = h - drawH - 24;
                g2.drawImage(landmarkIcon, drawX, drawY, drawW, drawH, null);
            }
        }

        // 3. Layer 3: Soft Scrim for readability
        g2.setColor(new Color(12, 16, 52, 105));
        g2.fillRect(0, 0, w, h);

        if (session == null) {
            drawIndigenousFrame(g2, w, h);
            return;
        }

        // 4. TOP HEADER BAR
        drawTopHeaderBar(g2, w, h);

        // 5. COMBAT ARENA CARD (Juan, Enemy, Dialogue Log, Word Rack & Attack Action)
        drawArenaCard(g2, w, h);

        // 6. BOTTOM LETTER TILES & CONTROLS
        drawBottomLetterGridAndButtons(g2, w, h);

        // 7. ATTACK EFFECTS & FLOATING TEXTS
        for (AttackEffect ae : attackEffects) ae.paint(g2);
        for (FloatingText ft : floatingTexts) ft.paint(g2);

        // 8. LAYER 4: INDIGENOUS FRAME BORDER (Strictly overlays border on all resolutions)
        drawIndigenousFrame(g2, w, h);

        // 9. PAUSE OVERLAY
        if (paused) drawPauseOverlay(g2, w, h);
    }

    private void drawTopHeaderBar(Graphics2D g2, int w, int h) {
        // Back to map button
        g2.setColor(CERULEAN);
        g2.fillRoundRect(backToMapBounds.x, backToMapBounds.y, backToMapBounds.width, backToMapBounds.height, 10, 10);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g2.drawString("← Balik sa Mapa", backToMapBounds.x + 14, backToMapBounds.y + 20);

        // Map location & stage badge
        String mapLabel = switch (session.getMapIndex()) {
            case 1 -> "BLUE BUILDING";
            case 2 -> "DAVAO MUSEUM";
            case 3 -> "SAN PEDRO CATHEDRAL";
            case 4 -> "MOUNT APO";
            default -> "BLUE BUILDING";
        };
        String headerTitle = mapLabel + " - STAGE " + session.getStageIndex() + "  ";
        g2.setFont(new Font(Font.SERIF, Font.BOLD, 16));
        g2.setColor(Color.WHITE);
        FontMetrics fm = g2.getFontMetrics();
        int titleW = fm.stringWidth(headerTitle);

        String diffName = "[ " + session.getDifficulty().getDisplayName().toUpperCase() + " ]";
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        int badgeW = g2.getFontMetrics().stringWidth(diffName) + 16;
        int centerStartX = (w - (titleW + badgeW)) / 2;

        g2.setFont(new Font(Font.SERIF, Font.BOLD, 16));
        g2.drawString(headerTitle, centerStartX, backToMapBounds.y + 22);

        // Difficulty Pill
        int badgeX = centerStartX + titleW;
        int badgeY = backToMapBounds.y + 5;
        g2.setColor(new Color(40, 160, 60));
        g2.fillRoundRect(badgeX, badgeY, badgeW, 22, 8, 8);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g2.drawString(diffName, badgeX + 8, badgeY + 15);

        // Right side pacing / turn timer
        String pacingText = session.getDifficulty().isTimed()
            ? "Turn Timer: " + session.getTurnSeconds() + "s"
            : "Pacing: Relaxed (No Timer)";
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g2.setColor(session.getTurnSeconds() <= 5 && session.getDifficulty().isTimed() ? Color.RED : new Color(180, 220, 255));
        int pw = g2.getFontMetrics().stringWidth(pacingText);
        g2.drawString(pacingText, w - pw - 45, backToMapBounds.y + 21);
    }

    private void drawArenaCard(Graphics2D g2, int w, int h) {
        double scaleX = w / 960.0;
        double scaleY = h / 540.0;

        int cardX = (int) (20 * scaleX);
        int cardY = (int) (54 * scaleY);
        int cardW = w - 2 * cardX;
        int cardH = (int) (228 * scaleY);

        // Translucent deep indigo card background
        g2.setColor(new Color(16, 16, 86, 115));
        g2.fillRoundRect(cardX, cardY, cardW, cardH, 14, 14);

        // Border: Dusk Navy (2px)
        g2.setColor(new Color(40, 50, 95));
        g2.setStroke(new BasicStroke(2.0f));
        g2.drawRoundRect(cardX, cardY, cardW, cardH, 14, 14);

        int juanW = (int) (185 * scaleX);
        int enemyW = (int) (185 * scaleX);

        // --- Left: Juan (Ikaw) ---
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g2.setColor(CREAM_TILE);
        String juanTitle = "JUAN (Ikaw)";
        int jtw = g2.getFontMetrics().stringWidth(juanTitle);
        g2.drawString(juanTitle, cardX + (juanW - jtw) / 2, cardY + (int) (20 * scaleY));

        int baselineY = cardY + (int) (160 * scaleY);
        int spriteH = (int) (130 * scaleY);
        int spriteW = (playerSprite != null) ? (int) Math.round(spriteH * (300.0 / 500.0)) : (int) (80 * scaleX);
        int spriteX = cardX + (juanW - spriteW) / 2;
        int spriteY = baselineY - (int) Math.round(spriteH * (440.0 / 500.0));

        // Ground shadow directly under boots
        int shadowW = (int) Math.round(spriteW * 0.85);
        g2.setColor(new Color(0, 0, 0, 60));
        g2.fillOval(spriteX + (spriteW - shadowW) / 2, baselineY - 4, shadowW, 10);

        // Paint Juan with attack stance frame progression
        BufferedImage activeJuan = playerAnimator.getActiveSprite(playerSprite, playerPreAttackSprite, playerAttackSprite);
        playerAnimator.paint(g2, activeJuan, spriteX, spriteY, spriteW, spriteH);

        // Juan Health Bar
        int hpBarW = (int) (160 * scaleX);
        int hpBarH = (int) (12 * scaleY);
        int hpBarX = cardX + (juanW - hpBarW) / 2;
        int hpBarY = cardY + (int) (166 * scaleY);
        drawFlatHpBar(g2, hpBarX, hpBarY, hpBarW, hpBarH, session.getPlayerHp(), session.getPlayer().getMaxHp(), HP_GREEN);

        // HP & Score Text below
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        g2.setColor(Color.WHITE);
        String hpText = "HP: " + session.getPlayerHp() + " / " + session.getPlayer().getMaxHp();
        int hpTw = g2.getFontMetrics().stringWidth(hpText);
        g2.drawString(hpText, cardX + (juanW - hpTw) / 2, hpBarY + hpBarH + (int) (14 * scaleY));

        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g2.setColor(new Color(160, 215, 255));
        String coinsScore = "Coins: 0 • Score: " + session.getPlayer().getScore();
        int csW = g2.getFontMetrics().stringWidth(coinsScore);
        g2.drawString(coinsScore, cardX + (juanW - csW) / 2, hpBarY + hpBarH + (int) (27 * scaleY));

        // --- Right: Enemy ---
        int enemyX = cardX + cardW - enemyW;
        String eName = session.getEnemy().getName().toUpperCase();
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g2.setColor(new Color(255, 205, 50));
        int ew = g2.getFontMetrics().stringWidth(eName);
        g2.drawString(eName, enemyX + (enemyW - ew) / 2, cardY + (int) (20 * scaleY));

        int eSpriteH = (int) (130 * scaleY);
        int eSpriteW = (enemySprite != null) ? (int) Math.round(eSpriteH * (enemySprite.getWidth() / (double) Math.max(1, enemySprite.getHeight()))) : (int) (80 * scaleX);
        int eSpriteX = enemyX + (enemyW - eSpriteW) / 2;
        int eSpriteY = baselineY - (int) Math.round(eSpriteH * (440.0 / 500.0));

        // Ground shadow directly under boots
        int eShadowW = (int) Math.round(eSpriteW * 0.85);
        g2.setColor(new Color(0, 0, 0, 60));
        g2.fillOval(eSpriteX + (eSpriteW - eShadowW) / 2, baselineY - 4, eShadowW, 10);

        // Paint Enemy
        enemyAnimator.paint(g2, enemySprite, eSpriteX, eSpriteY, eSpriteW, eSpriteH);

        // Enemy Health Bar
        int eHpBarX = enemyX + (enemyW - hpBarW) / 2;
        drawFlatHpBar(g2, eHpBarX, hpBarY, hpBarW, hpBarH, session.getEnemy().getCurrentHp(), session.getEnemy().getMaxHp(), HP_ORANGE);

        // Enemy stats below
        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g2.setColor(new Color(160, 215, 255));
        String atkStr = "ATK: " + session.getEnemy().getAttack() + " kada turno";
        int atkW = g2.getFontMetrics().stringWidth(atkStr);
        g2.drawString(atkStr, enemyX + (enemyW - atkW) / 2, hpBarY + hpBarH + (int) (14 * scaleY));

        // --- Center: Dialogue, Word Rack, Word Status Bar & ATAKE Button ---
        int centerX = cardX + juanW + (int) (10 * scaleX);
        int centerW = cardW - juanW - enemyW - (int) (20 * scaleX);

        // 1. Top Dialogue/Status log box
        int logY = cardY + (int) (8 * scaleY);
        int logH = (int) (30 * scaleY);
        g2.setColor(new Color(22, 28, 60, 210));
        g2.fillRoundRect(centerX, logY, centerW, logH, 6, 6);
        g2.setColor(ARENA_BOX_BORDER);
        g2.drawRoundRect(centerX, logY, centerW, logH, 6, 6);

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g2.setColor(CREAM_TILE);
        int dtw = g2.getFontMetrics().stringWidth(dialogueText);
        g2.drawString(dialogueText, centerX + (centerW - dtw) / 2, logY + (int) (20 * scaleY));

        // 2. Middle Word Rack Box
        int rackY = logY + logH + (int) (5 * scaleY);
        int rackH = (int) (122 * scaleY);
        g2.setColor(new Color(16, 20, 50, 180));
        g2.fillRoundRect(centerX, rackY, centerW, rackH, 6, 6);
        g2.setColor(ARENA_BOX_BORDER);
        g2.drawRoundRect(centerX, rackY, centerW, rackH, 6, 6);

        String word = getSelectedWord();
        if (word.isEmpty()) {
            String prompt = "📖 Pindota ang mga letra sa ubos aron makaporma ug pulong";
            g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
            g2.setColor(new Color(180, 200, 230));
            int pw = g2.getFontMetrics().stringWidth(prompt);
            g2.drawString(prompt, centerX + (centerW - pw) / 2, rackY + rackH / 2 + 5);
        } else {
            // Render selected letter tiles inside the rack!
            int tileCount = selectedPath.size();
            int pTileSize = (int) Math.round(48 * Math.min(scaleX, scaleY));
            int pGap = 6;
            int totalRackW = tileCount * pTileSize + Math.max(0, tileCount - 1) * pGap;
            int startTileX = centerX + (centerW - totalRackW) / 2;
            int tileY = rackY + (rackH - pTileSize) / 2;

            TileComponent[][] grid = session.getGrid();
            for (int i = 0; i < tileCount; i++) {
                int[] pos = selectedPath.get(i);
                TileComponent t = grid[pos[0]][pos[1]];
                int tx = startTileX + i * (pTileSize + pGap);

                BufferedImage pTileImg = ImageLoader.loadLetterTile(t.getLetter());
                if (pTileImg != null) {
                    g2.drawImage(pTileImg, tx, tileY, pTileSize, pTileSize, null);
                    t.renderTileOverlay(g2, tx, tileY, pTileSize);
                    g2.setColor(new Color(240, 150, 30));
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(tx, tileY, pTileSize, pTileSize, 6, 6);
                } else {
                    g2.setColor(t.getTileColor());
                    g2.fillRoundRect(tx, tileY, pTileSize, pTileSize, 6, 6);
                    t.renderTileOverlay(g2, tx, tileY, pTileSize);
                    g2.setColor(t.getBorderColor());
                    g2.setStroke(new BasicStroke(1.5f));
                    g2.drawRoundRect(tx, tileY, pTileSize, pTileSize, 6, 6);

                    g2.setFont(new Font(Font.SERIF, Font.BOLD, (int) (pTileSize * 0.54)));
                    g2.setColor(new Color(25, 25, 25));
                    String ls = String.valueOf(t.getLetter());
                    FontMetrics fm = g2.getFontMetrics();
                    int lx = tx + (pTileSize - fm.stringWidth(ls)) / 2;
                    int ly = tileY + (pTileSize + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(ls, lx, ly);
                }

                t.renderBadge(g2, tx, tileY, pTileSize);

                g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 9));
                g2.setColor(new Color(100, 100, 100));
                g2.drawString(String.valueOf(t.getPoints()), tx + pTileSize - 10, tileY + pTileSize - 4);
            }
        }

        // 3. Bottom Action Row: Word Preview/Hint & ATAKE Button
        int actY = rackY + rackH + (int) (5 * scaleY);
        int actH = (int) (44 * scaleY);
        int prevW = (int) (centerW * 0.66);

        // Left Word preview / prompt box
        g2.setColor(new Color(22, 28, 60, 210));
        g2.fillRoundRect(centerX, actY, prevW, actH, 6, 6);
        g2.setColor(ARENA_BOX_BORDER);
        g2.drawRoundRect(centerX, actY, prevW, actH, 6, 6);

        if (word.isEmpty()) {
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
            g2.setColor(Color.WHITE);
            String emptyHint = "Pili ug mga letra sa ubos para makaporma ug pulong";
            int ehw = g2.getFontMetrics().stringWidth(emptyHint);
            g2.drawString(emptyHint, centerX + (prevW - ehw) / 2, actY + (int) (27 * scaleY));
        } else {
            List<TileComponent> tiles = new ArrayList<>();
            TileComponent[][] grid = session.getGrid();
            for (int[] pos : selectedPath) tiles.add(grid[pos[0]][pos[1]]);
            int dmgPreview = computeDamage(tiles);

            g2.setFont(new Font(Font.SERIF, Font.BOLD, 20));
            g2.setColor(new Color(255, 230, 100));
            g2.drawString(word, centerX + 16, actY + (int) (29 * scaleY));

            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
            g2.setColor(new Color(100, 255, 120));
            String dmgStr = dmgPreview + " DMG";
            int dw = g2.getFontMetrics().stringWidth(dmgStr);
            g2.drawString(dmgStr, centerX + prevW - dw - 16, actY + (int) (27 * scaleY));
        }

        // Right Hero Attack Button (⚔ ATAKE!)
        int atkBtnW = centerW - prevW - (int) (8 * scaleX);
        int atkX = centerX + prevW + (int) (8 * scaleX);
        attackBtnBounds.setBounds(atkX, actY, atkBtnW, actH);

        g2.setColor(DARK_BLUE_BTN);
        g2.fillRoundRect(attackBtnBounds.x, attackBtnBounds.y, attackBtnBounds.width, attackBtnBounds.height, 6, 6);
        g2.setColor(ARENA_BOX_BORDER);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(attackBtnBounds.x, attackBtnBounds.y, attackBtnBounds.width, attackBtnBounds.height, 6, 6);

        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        g2.setColor(Color.WHITE);
        String atkLabel = "⚔ ATAKE!";
        int aw = g2.getFontMetrics().stringWidth(atkLabel);
        g2.drawString(atkLabel, attackBtnBounds.x + (attackBtnBounds.width - aw) / 2, attackBtnBounds.y + 19);

        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        g2.setColor(new Color(180, 200, 240));
        String subLabel = word.isEmpty() ? "(Pili ug letra)" : "(Isumiter)";
        int sw = g2.getFontMetrics().stringWidth(subLabel);
        g2.drawString(subLabel, attackBtnBounds.x + (attackBtnBounds.width - sw) / 2, attackBtnBounds.y + 33);
    }

    private void drawBottomLetterGridAndButtons(Graphics2D g2, int w, int h) {
        // --- Left Action Buttons: ← Back & × Clear ---
        drawFlatYellowButton(g2, backTileBtnBounds, "← Back");
        drawFlatYellowButton(g2, clearBtnBounds, "× Clear");

        // --- Center: 4x4 Tiles ---
        TileComponent[][] grid = session.getGrid();
        Point mouseP = getMousePosition();
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                Rectangle b = tileBounds[r][c];
                if (b == null) continue;
                TileComponent tile = grid[r][c];

                boolean isSelected = false;
                int order = -1;
                for (int i = 0; i < selectedPath.size(); i++) {
                    int[] pos = selectedPath.get(i);
                    if (pos[0] == r && pos[1] == c) {
                        isSelected = true;
                        order = i + 1;
                        break;
                    }
                }
                boolean isHovered = (mouseP != null && b.contains(mouseP));

                BufferedImage tileImg = ImageLoader.loadLetterTile(tile.getLetter());
                if (tileImg != null) {
                    int size = Math.min(b.width, b.height);
                    int drawX = b.x + (b.width - size) / 2;
                    int drawY = b.y + (b.height - size) / 2;

                    if (isSelected) {
                        // Golden aura for selected tile
                        g2.setColor(new Color(245, 180, 50, 175));
                        g2.setStroke(new BasicStroke(3.5f));
                        g2.drawRoundRect(drawX - 1, drawY - 1, size + 2, size + 2, 8, 8);
                    } else if (isHovered) {
                        // Sky blue hover aura
                        g2.setColor(new Color(90, 190, 255, 160));
                        g2.setStroke(new BasicStroke(2.5f));
                        g2.drawRoundRect(drawX - 1, drawY - 1, size + 2, size + 2, 8, 8);
                    }

                    // High-resolution alphabet tile sprite
                    g2.drawImage(tileImg, drawX, drawY, size, size, null);

                    // Tile decorator color overlay (Gold / Healing)
                    tile.renderTileOverlay(g2, drawX, drawY, size);

                    if (isSelected) {
                        Graphics2D gTint = (Graphics2D) g2.create();
                        gTint.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.16f));
                        gTint.setColor(new Color(255, 200, 50));
                        gTint.fillRoundRect(drawX, drawY, size, size, 8, 8);
                        gTint.dispose();

                        g2.setColor(new Color(240, 150, 30));
                        g2.setStroke(new BasicStroke(2.2f));
                        g2.drawRoundRect(drawX, drawY, size, size, 8, 8);
                    } else if (isHovered) {
                        g2.setColor(new Color(110, 200, 255));
                        g2.setStroke(new BasicStroke(1.8f));
                        g2.drawRoundRect(drawX, drawY, size, size, 8, 8);
                    }

                    // Decorator badge (Gold / Healing)
                    tile.renderBadge(g2, drawX, drawY, size);

                    // Points in bottom right
                    g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
                    g2.setColor(new Color(100, 100, 100));
                    g2.drawString(String.valueOf(tile.getPoints()), drawX + size - 12, drawY + size - 4);

                    // Order badge if selected
                    if (isSelected) {
                        g2.setColor(new Color(240, 150, 30));
                        g2.fillOval(drawX + size - 16, drawY + 2, 14, 14);
                        g2.setColor(Color.WHITE);
                        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 9));
                        g2.drawString(String.valueOf(order), drawX + size - 12, drawY + 13);
                    }
                } else {
                    // Tile Background
                    g2.setColor(isSelected ? CREAM_TILE_SELECTED : tile.getTileColor());
                    g2.fillRoundRect(b.x, b.y, b.width, b.height, 6, 6);

                    // Decorator color overlay
                    tile.renderTileOverlay(g2, b.x, b.y, b.width);

                    // Border
                    g2.setColor(isSelected ? TILE_BORDER_SELECTED : tile.getBorderColor());
                    g2.setStroke(new BasicStroke(isSelected ? 2.5f : 1.2f));
                    g2.drawRoundRect(b.x, b.y, b.width, b.height, 6, 6);

                    // Badge (Gold / Healing)
                    tile.renderBadge(g2, b.x, b.y, b.width);

                    // Letter (Bold Black Serif)
                    g2.setFont(new Font(Font.SERIF, Font.BOLD, (int) (b.height * 0.52)));
                    g2.setColor(new Color(25, 25, 25));
                    String ls = String.valueOf(tile.getLetter());
                    FontMetrics fm = g2.getFontMetrics();
                    int lx = b.x + (b.width - fm.stringWidth(ls)) / 2;
                    int ly = b.y + (b.height + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(ls, lx, ly);

                    // Points in bottom right
                    g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
                    g2.setColor(new Color(100, 100, 100));
                    g2.drawString(String.valueOf(tile.getPoints()), b.x + b.width - 12, b.y + b.height - 4);

                    // Order badge if selected
                    if (isSelected) {
                        g2.setColor(TILE_BORDER_SELECTED);
                        g2.fillOval(b.x + b.width - 16, b.y + 2, 14, 14);
                        g2.setColor(Color.WHITE);
                        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 9));
                        g2.drawString(String.valueOf(order), b.x + b.width - 12, b.y + 13);
                    }
                }
            }
        }

        // --- Right Action Button: Bag-o (Scramble) & Bonus hint ---
        drawFlatYellowButton(g2, scrambleBtnBounds, "Bag-o");
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g2.setColor(new Color(40, 180, 80));
        String bonusHint = "✓ 5+ Letra";
        String bonusHint2 = "Dako'g Bili!";
        g2.drawString(bonusHint, scrambleBtnBounds.x + 18, scrambleBtnBounds.y + 55);
        g2.drawString(bonusHint2, scrambleBtnBounds.x + 12, scrambleBtnBounds.y + 70);
    }

    private void drawFlatYellowButton(Graphics2D g2, Rectangle b, String label) {
        if (b == null) return;
        Point mouseP = getMousePosition();
        boolean hover = mouseP != null && b.contains(mouseP);

        // Drop shadow for tactile depth
        g2.setColor(new Color(15, 16, 40, 160));
        g2.fillRoundRect(b.x + 1, b.y + 2, b.width, b.height, 8, 8);

        // Dabawenyo SEQUESTA button background
        Color btnBg = hover ? new Color(225, 220, 100) : new Color(0xD3, 0xCD, 0x56);
        g2.setColor(btnBg);
        g2.fillRoundRect(b.x, b.y, b.width, b.height, 8, 8);

        // Dusk Navy border
        g2.setColor(new Color(0x1F, 0x20, 0x58));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(b.x, b.y, b.width, b.height, 8, 8);

        // Midnight Navy label text
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        g2.setColor(new Color(0x01, 0x00, 0x48));
        FontMetrics fm = g2.getFontMetrics();
        int lx = b.x + (b.width - fm.stringWidth(label)) / 2;
        int ly = b.y + (b.height + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(label, lx, ly);
    }

    private void drawFlatHpBar(Graphics2D g2, int x, int y, int bw, int bh, int cur, int max, Color fillCol) {
        g2.setColor(new Color(20, 20, 30));
        g2.fillRect(x, y, bw, bh);
        float frac = (max > 0) ? Math.clamp((float) cur / max, 0f, 1f) : 0f;
        int fillW = (int) (bw * frac);
        g2.setColor(fillCol);
        g2.fillRect(x, y, fillW, bh);
    }

    private void drawIndigenousFrame(Graphics2D g2, int w, int h) {
        if (indigFrame != null) {
            g2.drawImage(indigFrame, 0, 0, w, h, null);
        }
    }

    private void drawPauseOverlay(Graphics2D g2, int w, int h) {
        g2.setColor(new Color(0, 0, 0, 185));
        g2.fillRect(0, 0, w, h);
        g2.setFont(new Font(Font.SERIF, Font.BOLD, 32));
        g2.setColor(Color.WHITE);
        String pStr = "GI-PAUSE";
        int pw = g2.getFontMetrics().stringWidth(pStr);
        g2.drawString(pStr, (w - pw) / 2, h / 2);
    }
}
