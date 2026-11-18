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
import java.awt.Shape;
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
import juanword.model.DifficultySetting;
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
    private BufferedImage enemyAttackSprite;

    private long lastTickTime;
    private BufferedImage cachedBgImage;
    private int cachedBgW = -1;
    private int cachedBgH = -1;
    private BufferedImage cachedBgLandmark;
    private BufferedImage cachedIndigFrame;
    private Rectangle cachedFrameInner;
    private int cachedFrameW = -1;
    private int cachedFrameH = -1;

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
    private final Rectangle[][] tileBounds = new Rectangle[4][4]; //2d aray 4x4 tiles

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
        this.lastTickTime = System.currentTimeMillis();
        this.cachedBgLandmark = null;

        // Landmark artwork for center watermark
        String lmName = switch (session.getMapIndex()) {
            case 1 -> "Blue Building.png";
            case 2 -> "durianmuseum.png";
            case 3 -> "sanpedro.png";
            case 4 -> "mtapo.png";
            default -> "Blue Building.png";
        };
        this.landmarkIcon = ImageLoader.load(lmName);
        if (session.getPlayer() != null && session.getPlayer().getPlayerName().equalsIgnoreCase("Gudo")) {
            this.playerSprite = ImageLoader.load("gudoidle.png");
            this.playerPreAttackSprite = ImageLoader.load("gudoidle.png");
            this.playerAttackSprite = ImageLoader.load("gudoattack.png");
        } else {
            this.playerSprite = ImageLoader.load("maincharacteridle.png");
            this.playerPreAttackSprite = ImageLoader.load("maincharapreattack.png");
            this.playerAttackSprite = ImageLoader.load("maincharattack.png");
        }
        this.enemySprite = ImageLoader.load(session.getEnemy().getSpritePath());
        String enemyAtkPath = session.getEnemy().getAttackSpritePath();
        this.enemyAttackSprite = (enemyAtkPath != null) ? ImageLoader.load(enemyAtkPath) : null;

        if (session.getMapIndex() == 4 && session.getStageIndex() == 4) {
            this.dialogueText = "MIPAKITA SI SIR GUDO! Andam na ba ka sa katapusang pagsulay?";
        }

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
        long now = System.currentTimeMillis();
        long dt = (lastTickTime == 0) ? 16L : (now - lastTickTime);
        lastTickTime = now;
        long dtMs = Math.clamp(dt, 1L, 100L);

        if (paused) {
            repaint();
            return;
        }

        playerAnimator.update(dtMs);
        enemyAnimator.update(dtMs);

        for (AttackEffect ae : attackEffects) ae.update(dtMs);
        attackEffects.removeIf(AttackEffect::isFinished);

        for (FloatingText ft : floatingTexts) ft.update(dtMs);
        floatingTexts.removeIf(FloatingText::isExpired);

        if (uiShakeFrames > 0) {
            uiShakeFrames = Math.max(0, uiShakeFrames - Math.max(1, (int) Math.round(dtMs / 16.0)));
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
        tickAccumulatorMs += dtMs;
        boolean secondElapsed = false;
        while (tickAccumulatorMs >= 1000) {
            tickAccumulatorMs -= 1000;
            secondElapsed = true;
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

        boolean busy = playerAnimator.isAttacking() || enemyAnimator.isAttacking() || enemyAnimator.isEntering()
            || uiShakeFrames > 0 || !attackEffects.isEmpty() || !floatingTexts.isEmpty()
            || pendingImpactAction != null || secondElapsed;
        if (busy || getWidth() <= 0) {
            repaint();
            return;
        }
        // Idle: only the breathing sprites move, so repaint just the two sprite columns instead of the whole screen.
        double sx = getWidth() / 960.0;
        double sy = getHeight() / 540.0;
        int cx = (int) (20 * sx);
        int cy = (int) (54 * sy);
        int colW = (int) (185 * sx) + (int) (30 * sx);
        int colH = (int) (228 * sy) + 4;
        repaint(cx - (int) (15 * sx), cy, colW, colH);
        repaint(getWidth() - cx - colW + (int) (15 * sx), cy, colW, colH);
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

            // Clicking Bag-o inflicts self damage
            int selfDamage = 5;
            ctx.damagePlayer(selfDamage);
            playerAnimator.triggerHit();
            uiShakeFrames = 6;
            spawnFloatingText("-" + selfDamage + " HP", new Point(140, 220), Color.RED);

            dialogueText = "Gibag-o ang tanang letra! Naangol ka ug " + selfDamage + " HP sa Bag-o.";
            if (session.getPlayerHp() <= 0) {
                onDefeat();
            }
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

    public void onClear() {
        selectedPath.clear();
        repaint();
    }

    public Rectangle getScrambleBtnBounds() {
        return new Rectangle(scrambleBtnBounds);
    }

    public Rectangle getAttackBtnBounds() {
        return new Rectangle(attackBtnBounds);
    }

    private void onSubmit() {
        if (selectedPath.isEmpty()) return;
        String word = getSelectedWord();
        if (!DataManager.getInstance().isValidCebuanoWord(word)) {
            uiShakeFrames = 6;
            spawnFloatingText("Dili balido!", new Point(attackBtnBounds.x + attackBtnBounds.width / 2, attackBtnBounds.y - 12), new Color(255, 120, 80));
            return;
        }
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

    public void submitWord(String word) {
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

    public int computeDamage(List<TileComponent> tiles) {
        if (tiles == null || tiles.isEmpty()) return 0;
        int sum = 0;
        for (TileComponent t : tiles) {
            sum += t.getPoints();
        }
        int wordLen = tiles.size();
        double mult = (session != null && session.getDifficulty() != null)
            ? session.getDifficulty().getLengthMultiplier(wordLen)
            : 1.0;
        double dmg = sum * mult;

        if (session != null) {
            boolean isExtreme = session.getDifficulty() == DifficultySetting.EXTREME || session.getMapIndex() == 4;
            boolean isFinalBoss = session.getEnemy() != null && "GUDO".equalsIgnoreCase(session.getEnemy().getEnemyId());
            boolean isBoss = session.getEnemy() != null && session.getEnemy().isBoss();

            if (isExtreme) {
                // Ghost increase for Extreme: scale damage up to match 1.5x enemy HP pools
                double ghostMult = 2.0;
                int ghostFlat = 10 + Math.max(0, (wordLen - 2) * 4);
                dmg = dmg * ghostMult + ghostFlat;

                if (isFinalBoss) {
                    // Extra ghost increase specifically for the final boss (Gudo)
                    dmg = dmg * 1.35 + 12;
                } else if (isBoss) {
                    // Ghost increase for Extreme boss (e.g. Porter)
                    dmg = dmg * 1.30 + 10;
                }
            } else if (isFinalBoss) {
                // If Gudo is fought outside Extreme, still grant the final boss ghost boost
                dmg = dmg * 1.50 + 20;
            }
        }

        return Math.max(1, (int) Math.round(dmg));
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
        boolean isGudoPlayer = session != null && session.getPlayer() != null &&
            session.getPlayer().getPlayerName() != null &&
            session.getPlayer().getPlayerName().equalsIgnoreCase("Gudo");
        long duration = isGudoPlayer ? 800L : 450L;
        playerAnimator.startAttack(AttackType.SLASH, duration);
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
        boolean isGudo = session != null && session.getEnemy() != null &&
            "GUDO".equalsIgnoreCase(session.getEnemy().getEnemyId());
        long duration = isGudo ? 800L : 450L;
        enemyAnimator.startAttack(type, duration);
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

    public void onVictory() {
        loop.stop();
        int scoreAward = 100 * session.getStageIndex();
        session.getPlayer().addScore(scoreAward);
        session.getPlayer().recordDefeat(session.getEnemy().getEnemyId());

        // In Mt. Apo, beating Boss Porter (stage 3) unlocks Gudo and proceeds immediately to the Gudo boss
        if (session.getMapIndex() == 4 && session.getStageIndex() == 3) {
            DataManager.getInstance().clearActiveBattle(session.getPlayer());
            DataManager.getInstance().savePlayers();
            BattleSession nextSession = new BattleSessionBuilder()
                .withPlayer(session.getPlayer())
                .onMap(4)
                .atStage(4)
                .withSavedHealth(session.getPlayerHp(), null)
                .build();
            begin(nextSession);
            return;
        }

        if (session.isBossStage()) {
            if (session.getMapIndex() == session.getPlayer().getUnlockedMap() && session.getMapIndex() < 4) {
                session.getPlayer().unlockMap(session.getMapIndex() + 1);
            }
            DataManager.getInstance().clearActiveBattle(session.getPlayer());
            DataManager.getInstance().savePlayers();

            if (session.isFinalBoss()) {
                JOptionPane.showMessageDialog(this, "NADAUG KA SA TANANG MAPA UG NALUPIG NIMO SI GUDO!\nCongratulations!", "KADAUGAN!", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "NADAUG ANG BOSS!\nNaabli ang sunod nga mapa!", "DAUG!", JOptionPane.INFORMATION_MESSAGE);
            }
            host.returnToMap();
        } else {
            DataManager.getInstance().clearActiveBattle(session.getPlayer());
            DataManager.getInstance().savePlayers();
            // Automatically advance to the next enemy with zero confirmation prompt
            BattleSession nextSession = new BattleSessionBuilder()
                .withPlayer(session.getPlayer())
                .onMap(session.getMapIndex())
                .atStage(session.getStageIndex() + 1)
                .withSavedHealth(session.getPlayerHp(), null)
                .build();
            begin(nextSession);
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
    public void resumeGame() {
        this.paused = false;
        this.lastTickTime = System.currentTimeMillis();
    }

    public int getSelectedDamagePreview() {
        if (selectedPath == null || selectedPath.isEmpty() || session == null) return 0;
        TileComponent[][] grid = session.getGrid();
        if (grid == null) return 0;
        List<TileComponent> tiles = new ArrayList<>();
        for (int[] pos : selectedPath) {
            if (pos[0] >= 0 && pos[0] < 4 && pos[1] >= 0 && pos[1] < 4) {
                tiles.add(grid[pos[0]][pos[1]]);
            }
        }
        return computeDamage(tiles);
    }

    public EnemyAnimator getEnemyAnimator() { return enemyAnimator; }
    public PlayerAnimator getPlayerAnimator() { return playerAnimator; }
    public BattleSession getSession() { return session; }

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

        // 1-3. Cached Composite Background (Sky + Landmark + Scrim)
        renderCachedBackground(g2, w, h);

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
        double scaleX = w / 960.0;
        double scaleY = h / 540.0;
        double S = Math.min(scaleX, scaleY);

        // Back to map button
        g2.setColor(CERULEAN);
        g2.fillRoundRect(backToMapBounds.x, backToMapBounds.y, backToMapBounds.width, backToMapBounds.height, 10, 10);
        g2.setColor(Color.WHITE);
        int backFontSize = Math.max(13, (int) Math.round(13 * S));
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, backFontSize));
        FontMetrics fmBack = g2.getFontMetrics();
        String backText = "← Balik sa Mapa";
        int bx = backToMapBounds.x + (backToMapBounds.width - fmBack.stringWidth(backText)) / 2;
        int by = backToMapBounds.y + (backToMapBounds.height + fmBack.getAscent() - fmBack.getDescent()) / 2;
        g2.drawString(backText, bx, by);

        // Map location & stage badge
        String mapLabel = switch (session.getMapIndex()) {
            case 1 -> "BLUE BUILDING";
            case 2 -> "DAVAO MUSEUM";
            case 3 -> "SAN PEDRO CATHEDRAL";
            case 4 -> "MOUNT APO";
            default -> "BLUE BUILDING";
        };
        String headerTitle = mapLabel + " - STAGE " + session.getStageIndex() + "  ";
        int titleFontSize = Math.max(16, (int) Math.round(17 * S));
        Font titleFont = new Font(Font.SERIF, Font.BOLD, titleFontSize);
        g2.setFont(titleFont);
        g2.setColor(Color.WHITE);
        FontMetrics fm = g2.getFontMetrics();
        int titleW = fm.stringWidth(headerTitle);

        String diffName = "[ " + session.getDifficulty().getDisplayName().toUpperCase() + " ]";
        int badgeFontSize = Math.max(11, (int) Math.round(12 * S));
        Font badgeFont = new Font(Font.SANS_SERIF, Font.BOLD, badgeFontSize);
        g2.setFont(badgeFont);
        FontMetrics fmBadge = g2.getFontMetrics();
        int badgeW = fmBadge.stringWidth(diffName) + (int) Math.round(18 * S);
        int badgeH = Math.max(22, (int) Math.round(24 * S));
        int centerStartX = (w - (titleW + badgeW)) / 2;

        int headerTextY = backToMapBounds.y + (backToMapBounds.height + fm.getAscent() - fm.getDescent()) / 2;
        g2.setFont(titleFont);
        g2.drawString(headerTitle, centerStartX, headerTextY);

        // Difficulty Pill
        int badgeX = centerStartX + titleW;
        int badgeY = backToMapBounds.y + (backToMapBounds.height - badgeH) / 2;
        g2.setColor(new Color(40, 160, 60));
        g2.fillRoundRect(badgeX, badgeY, badgeW, badgeH, 8, 8);
        g2.setColor(Color.WHITE);
        g2.setFont(badgeFont);
        int btx = badgeX + (badgeW - fmBadge.stringWidth(diffName)) / 2;
        int bty = badgeY + (badgeH + fmBadge.getAscent() - fmBadge.getDescent()) / 2;
        g2.drawString(diffName, btx, bty);

        // Right side pacing / turn timer
        String pacingText = session.getDifficulty().isTimed()
            ? "Turn Timer: " + session.getTurnSeconds() + "s"
            : "Pacing: Relaxed (No Timer)";
        int paceFontSize = Math.max(12, (int) Math.round(13 * S));
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, paceFontSize));
        g2.setColor(session.getTurnSeconds() <= 5 && session.getDifficulty().isTimed() ? Color.RED : new Color(180, 220, 255));
        FontMetrics fmPace = g2.getFontMetrics();
        int pw = fmPace.stringWidth(pacingText);
        int py = backToMapBounds.y + (backToMapBounds.height + fmPace.getAscent() - fmPace.getDescent()) / 2;
        g2.drawString(pacingText, w - pw - (int) Math.round(45 * scaleX), py);
    }

    private void drawArenaCard(Graphics2D g2, int w, int h) {
        double scaleX = w / 960.0;
        double scaleY = h / 540.0;

        String word = getSelectedWord();
        boolean isWordValid = !word.isEmpty() && DataManager.getInstance().isValidCebuanoWord(word);
        int dmgPreview = getSelectedDamagePreview();

        int cardX = (int) (20 * scaleX);
        int cardY = (int) (54 * scaleY);
        int cardW = w - 2 * cardX;
        int cardH = (int) (228 * scaleY);

        // Translucent deep indigo card background
        g2.setColor(new Color(16, 16, 86, 115));
        fillPanelBox(g2, cardX, cardY, cardW, cardH, 14, 14);

        // Border: Dusk Navy (2px)
        g2.setColor(new Color(40, 50, 95));
        g2.setStroke(new BasicStroke(2.0f));
        g2.drawRoundRect(cardX, cardY, cardW, cardH, 14, 14);

        int juanW = (int) (185 * scaleX);
        int enemyW = (int) (185 * scaleX);

        // Responsive sprite sizing: bigger in general, with extra scaling on full screen
        boolean isFullScreen = (w >= 1200 || h >= 700 || (host != null && host.isFullScreen()));
        double fullScreenBoost = isFullScreen ? 1.18 : (scaleY > 1.05 ? 1.0 + (scaleY - 1.0) * 0.18 : 1.0);
        int baseSpriteH = 155; // Enlarged from 130 in general (+19% bigger in windowed mode)

        // --- Left: Player (Ikaw) ---
        double S = Math.min(scaleX, scaleY);
        int playerTitleSize = Math.max(15, (int) Math.round(16 * S));
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, playerTitleSize));
        g2.setColor(CREAM_TILE);
        String playerTitle = "IKAW";
        int jtw = g2.getFontMetrics().stringWidth(playerTitle);
        g2.drawString(playerTitle, cardX + (juanW - jtw) / 2, cardY + (int) (22 * scaleY));

        int baselineY = cardY + (int) (166 * scaleY);
        int spriteH = (int) Math.round(baseSpriteH * scaleY * fullScreenBoost);
        int spriteW = (playerSprite != null) ? (int) Math.round(spriteH * (300.0 / 500.0)) : (int) (80 * scaleX);
        int spriteX = cardX + (juanW - spriteW) / 2;

        BufferedImage activeJuan = playerAnimator.getActiveSprite(playerSprite, playerPreAttackSprite, playerAttackSprite);
        boolean isGudoPlayerIdle = (activeJuan == playerSprite && session.getPlayer() != null && session.getPlayer().getPlayerName().equalsIgnoreCase("Gudo"));
        double playerFootRatio = isGudoPlayerIdle ? (361.0 / 500.0) : (440.0 / 500.0);
        int spriteY = baselineY - (int) Math.round(spriteH * playerFootRatio);

        // Ground shadow directly under boots, synchronized with player animation
        int shadowW = (int) Math.round(spriteW * 0.85 * playerAnimator.getShadowScale());
        int shadowX = spriteX + (int) playerAnimator.getDrawOffsetX();
        g2.setColor(new Color(0, 0, 0, 60));
        g2.fillOval(shadowX + (spriteW - shadowW) / 2, baselineY - 4, shadowW, 10);

        // Paint Juan with attack stance frame progression and grounded pivot
        playerAnimator.paint(g2, activeJuan, spriteX, spriteY, spriteW, spriteH, playerFootRatio);

        // Juan Health Bar
        int hpBarW = (int) (160 * scaleX);
        int hpBarH = (int) (12 * scaleY);
        int hpBarX = cardX + (juanW - hpBarW) / 2;
        int hpBarY = cardY + (int) (172 * scaleY);
        drawFlatHpBar(g2, hpBarX, hpBarY, hpBarW, hpBarH, session.getPlayerHp(), session.getPlayer().getMaxHp(), HP_GREEN);

        // HP & Score Text below
        int hpFontSize = Math.max(12, (int) Math.round(13 * S));
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, hpFontSize));
        g2.setColor(Color.WHITE);
        String hpText = "HP: " + session.getPlayerHp() + " / " + session.getPlayer().getMaxHp();
        int hpTw = g2.getFontMetrics().stringWidth(hpText);
        g2.drawString(hpText, cardX + (juanW - hpTw) / 2, hpBarY + hpBarH + (int) (16 * scaleY));

        int coinsFontSize = Math.max(11, (int) Math.round(12 * S));
        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, coinsFontSize));
        g2.setColor(new Color(160, 215, 255));
        String coinsScore = "Coins: 0 • Score: " + session.getPlayer().getScore();
        int csW = g2.getFontMetrics().stringWidth(coinsScore);
        g2.drawString(coinsScore, cardX + (juanW - csW) / 2, hpBarY + hpBarH + (int) (31 * scaleY));

        // --- Right: Enemy ---
        int enemyX = cardX + cardW - enemyW;
        String eName = session.getEnemy().getName().toUpperCase();
        int eNameFontSize = Math.max(15, (int) Math.round(16 * S));
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, eNameFontSize));
        g2.setColor(new Color(255, 205, 50));
        int ew = g2.getFontMetrics().stringWidth(eName);
        g2.drawString(eName, enemyX + (enemyW - ew) / 2, cardY + (int) (22 * scaleY));

        int eSpriteH = (int) Math.round(baseSpriteH * scaleY * fullScreenBoost);
        double eAspect = (enemySprite != null && enemySprite.getHeight() > 0)
            ? (double) enemySprite.getWidth() / enemySprite.getHeight()
            : (300.0 / 500.0);
        int maxEnemyW = (int) (enemyW * 0.92);
        int eSpriteW = (int) Math.round(eSpriteH * eAspect);
        if (eSpriteW > maxEnemyW) {
            eSpriteW = maxEnemyW;
            eSpriteH = (int) Math.round(eSpriteW / eAspect);
        }
        int eSpriteX = enemyX + (enemyW - eSpriteW) / 2;

        BufferedImage activeEnemy = enemyAnimator.getActiveSprite(enemySprite, enemyAttackSprite);
        boolean isGudoEnemyIdle = (activeEnemy == enemySprite && session.getEnemy().getEnemyId().equals("GUDO"));
        double enemyFootRatio = isGudoEnemyIdle ? (361.0 / 500.0) : (440.0 / 500.0);
        int eSpriteY = baselineY - (int) Math.round(eSpriteH * enemyFootRatio);

        // Ground shadow directly under boots, synchronized with enemy animation
        int eShadowW = (int) Math.round(eSpriteW * 0.85 * enemyAnimator.getShadowScale());
        int eShadowX = eSpriteX + (int) enemyAnimator.getDrawOffsetX();
        g2.setColor(new Color(0, 0, 0, 60));
        g2.fillOval(eShadowX + (eSpriteW - eShadowW) / 2, baselineY - 4, eShadowW, 10);

        // Paint Enemy with attack stance frame progression and grounded pivot
        enemyAnimator.paint(g2, activeEnemy, eSpriteX, eSpriteY, eSpriteW, eSpriteH, enemyFootRatio);

        // Enemy Health Bar with Prospective Damage Preview
        int enemyCurHp = session.getEnemy().getCurrentHp();
        int enemyMaxHp = session.getEnemy().getMaxHp();
        int eHpBarX = enemyX + (enemyW - hpBarW) / 2;
        drawEnemyHpBarWithPreview(g2, eHpBarX, hpBarY, hpBarW, hpBarH, enemyCurHp, enemyMaxHp, dmgPreview, isWordValid);

        // Enemy stats below
        // Row 1: Enemy HP Text with Damage Preview Deduction
        int eHpFontSize = Math.max(12, (int) Math.round(13 * S));
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, eHpFontSize));
        FontMetrics fmHp = g2.getFontMetrics();

        if (dmgPreview > 0) {
            int remHp = Math.max(0, enemyCurHp - dmgPreview);
            String mainHp = "HP: " + remHp + " / " + enemyMaxHp + " ";
            String dmgTag = "(-" + dmgPreview + (remHp == 0 ? " LETHAL" : "") + ")";

            int mainW = fmHp.stringWidth(mainHp);
            int tagW = fmHp.stringWidth(dmgTag);
            int totalTextW = mainW + tagW;
            int textX = enemyX + (enemyW - totalTextW) / 2;
            int textY = hpBarY + hpBarH + (int) (16 * scaleY);

            g2.setColor(Color.WHITE);
            g2.drawString(mainHp, textX, textY);

            g2.setColor(new Color(255, 80, 75));
            g2.drawString(dmgTag, textX + mainW, textY);
        } else {
            String eHpText = "HP: " + enemyCurHp + " / " + enemyMaxHp;
            int eHpTw = fmHp.stringWidth(eHpText);
            g2.setColor(Color.WHITE);
            g2.drawString(eHpText, enemyX + (enemyW - eHpTw) / 2, hpBarY + hpBarH + (int) (16 * scaleY));
        }

        // Row 2: Enemy Attack Stat
        int atkFontSize = Math.max(11, (int) Math.round(12 * S));
        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, atkFontSize));
        g2.setColor(new Color(160, 215, 255));
        String atkStr = "ATK: " + session.getEnemy().getAttack() + " kada turno";
        int atkW = g2.getFontMetrics().stringWidth(atkStr);
        g2.drawString(atkStr, enemyX + (enemyW - atkW) / 2, hpBarY + hpBarH + (int) (31 * scaleY));

        // --- Center: Dialogue, Word Rack, Word Status Bar & ATAKE Button ---
        int centerX = cardX + juanW + (int) (10 * scaleX);
        int centerW = cardW - juanW - enemyW - (int) (20 * scaleX);

        // 1. Top Dialogue/Status log box
        int logY = cardY + (int) (8 * scaleY);
        int logH = (int) (30 * scaleY);
        g2.setColor(new Color(22, 28, 60, 210));
        fillPanelBox(g2, centerX, logY, centerW, logH, 6, 6);
        g2.setColor(ARENA_BOX_BORDER);
        g2.drawRoundRect(centerX, logY, centerW, logH, 6, 6);

        int logFontSize = Math.max(11, (int) Math.round(12 * S));
        Font logFont = new Font(Font.SANS_SERIF, Font.BOLD, logFontSize);
        FontMetrics fmLog = g2.getFontMetrics(logFont);
        int maxLogW = centerW - (int) Math.round(20 * scaleX);
        while (fmLog.stringWidth(dialogueText) > maxLogW && logFontSize > 9) {
            logFontSize--;
            logFont = new Font(Font.SANS_SERIF, Font.BOLD, logFontSize);
            fmLog = g2.getFontMetrics(logFont);
        }
        g2.setFont(logFont);
        g2.setColor(CREAM_TILE);
        int dtw = fmLog.stringWidth(dialogueText);
        int dtx = centerX + Math.max((int) Math.round(10 * scaleX), (centerW - dtw) / 2);
        int dty = logY + (logH + fmLog.getAscent() - fmLog.getDescent()) / 2;
        g2.drawString(dialogueText, dtx, dty);

        // 2. Middle Word Rack Box
        int rackY = logY + logH + (int) (5 * scaleY);
        int rackH = (int) (122 * scaleY);
        g2.setColor(new Color(16, 20, 50, 180));
        fillPanelBox(g2, centerX, rackY, centerW, rackH, 6, 6);
        g2.setColor(ARENA_BOX_BORDER);
        g2.drawRoundRect(centerX, rackY, centerW, rackH, 6, 6);

        if (word.isEmpty()) {
            String prompt = "📖 Pindota ang mga letra sa ubos aron makaporma ug pulong";
            int promptFontSize = Math.max(12, (int) Math.round(13 * S));
            Font promptFont = new Font(Font.SANS_SERIF, Font.PLAIN, promptFontSize);
            FontMetrics fmPr = g2.getFontMetrics(promptFont);
            int maxPromptW = centerW - (int) Math.round(24 * scaleX);
            while (fmPr.stringWidth(prompt) > maxPromptW && promptFontSize > 9) {
                promptFontSize--;
                promptFont = new Font(Font.SANS_SERIF, Font.PLAIN, promptFontSize);
                fmPr = g2.getFontMetrics(promptFont);
            }
            g2.setFont(promptFont);
            g2.setColor(new Color(180, 200, 230));
            int pw = fmPr.stringWidth(prompt);
            int prx = centerX + Math.max((int) Math.round(12 * scaleX), (centerW - pw) / 2);
            int pry = rackY + (rackH + fmPr.getAscent() - fmPr.getDescent()) / 2;
            g2.drawString(prompt, prx, pry);
        } else {
            // Render selected letter tiles inside the rack!
            int tileCount = selectedPath.size();
            int pTileSize = (int) Math.round(48 * Math.min(scaleX, scaleY));
            int pGap = 6;
            int maxRackW = centerW - (int) Math.round(20 * scaleX);
            int totalRackW = tileCount * pTileSize + Math.max(0, tileCount - 1) * pGap;
            if (totalRackW > maxRackW && tileCount > 0) {
                pTileSize = Math.max(20, (maxRackW - Math.max(0, tileCount - 1) * pGap) / tileCount);
                totalRackW = tileCount * pTileSize + Math.max(0, tileCount - 1) * pGap;
            }
            int startTileX = centerX + Math.max((int) Math.round(10 * scaleX), (centerW - totalRackW) / 2);
            int tileY = rackY + (rackH - pTileSize) / 2;

            TileComponent[][] grid = session.getGrid();
            for (int i = 0; i < tileCount; i++) {
                int[] pos = selectedPath.get(i);
                TileComponent t = grid[pos[0]][pos[1]];
                int tx = startTileX + i * (pTileSize + pGap);

                BufferedImage pTileImg = ImageLoader.loadLetterTile(t.getLetter());
                if (pTileImg != null) {
                    g2.drawImage(ImageLoader.scaledCached(pTileImg, pTileSize, pTileSize), tx, tileY, null);
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

                int ptSize = Math.max(9, (int) Math.round(pTileSize * 0.22));
                g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, ptSize));
                g2.setColor(new Color(100, 100, 100));
                FontMetrics fmPts = g2.getFontMetrics();
                String ptStr = String.valueOf(t.getPoints());
                int ptPad = Math.max(3, (int) (pTileSize * 0.08));
                g2.drawString(ptStr, tx + pTileSize - fmPts.stringWidth(ptStr) - ptPad, tileY + pTileSize - ptPad);
            }
        }

        // 3. Bottom Action Row: Word Preview/Hint & ATAKE Button
        int actY = rackY + rackH + (int) (5 * scaleY);
        int actH = (int) (44 * scaleY);
        int prevW = (int) (centerW * 0.66);

        // Left Word preview / prompt box
        g2.setColor(new Color(22, 28, 60, 210));
        fillPanelBox(g2, centerX, actY, prevW, actH, 6, 6);
        g2.setColor(ARENA_BOX_BORDER);
        g2.drawRoundRect(centerX, actY, prevW, actH, 6, 6);

        Shape origClip = g2.getClip();
        g2.clipRect(centerX, actY, prevW, actH);

        if (word.isEmpty()) {
            String emptyHint = "Porma ug pulong sa ubos";
            int hintFontSize = Math.max(12, (int) Math.round(13 * S));
            Font hintFont = new Font(Font.SANS_SERIF, Font.BOLD, hintFontSize);
            FontMetrics fmEh = g2.getFontMetrics(hintFont);
            int maxHintW = prevW - (int) Math.round(20 * scaleX);
            while (fmEh.stringWidth(emptyHint) > maxHintW && hintFontSize > 9) {
                hintFontSize--;
                hintFont = new Font(Font.SANS_SERIF, Font.BOLD, hintFontSize);
                fmEh = g2.getFontMetrics(hintFont);
            }
            g2.setFont(hintFont);
            g2.setColor(new Color(210, 225, 245));
            int ehw = fmEh.stringWidth(emptyHint);
            int ehx = centerX + Math.max((int) Math.round(10 * scaleX), (prevW - ehw) / 2);
            int ehy = actY + (actH + fmEh.getAscent() - fmEh.getDescent()) / 2;
            g2.drawString(emptyHint, ehx, ehy);
        } else {
            int dmgFontSize = Math.max(13, (int) Math.round(15 * S));
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, dmgFontSize));
            g2.setColor(new Color(100, 255, 120));
            String dmgStr = dmgPreview + " DMG";
            FontMetrics fmDmg = g2.getFontMetrics();
            int dw = fmDmg.stringWidth(dmgStr);
            int dy = actY + (actH + fmDmg.getAscent() - fmDmg.getDescent()) / 2;
            int dx = centerX + prevW - dw - (int) Math.round(16 * scaleX);
            g2.drawString(dmgStr, dx, dy);

            int wordFontSize = Math.max(20, (int) Math.round(22 * S));
            Font wordFont = new Font(Font.SERIF, Font.BOLD, wordFontSize);
            FontMetrics fmWord = g2.getFontMetrics(wordFont);
            int maxWordW = dx - centerX - (int) Math.round(24 * scaleX);
            while (fmWord.stringWidth(word) > maxWordW && wordFontSize > 11) {
                wordFontSize--;
                wordFont = new Font(Font.SERIF, Font.BOLD, wordFontSize);
                fmWord = g2.getFontMetrics(wordFont);
            }
            g2.setFont(wordFont);
            g2.setColor(new Color(255, 230, 100));
            int wy = actY + (actH + fmWord.getAscent() - fmWord.getDescent()) / 2;
            g2.drawString(word, centerX + (int) Math.round(14 * scaleX), wy);
        }
        g2.setClip(origClip);

        // Right Hero Attack Button (⚔ ATAKE!) - Lights up only if word is valid in CSV
        int atkBtnW = centerW - prevW - (int) (8 * scaleX);
        int atkX = centerX + prevW + (int) (8 * scaleX);
        attackBtnBounds.setBounds(atkX, actY, atkBtnW, actH);

        int atkBtnFontSize = Math.max(13, (int) Math.round(15 * S));
        int subFontSize = Math.max(10, (int) Math.round(11 * S));
        Font fontAtk = new Font(Font.SANS_SERIF, Font.BOLD, atkBtnFontSize);
        Font fontSub = new Font(Font.SANS_SERIF, isWordValid ? Font.BOLD : Font.PLAIN, subFontSize);

        FontMetrics fmA = g2.getFontMetrics(fontAtk);
        FontMetrics fmS = g2.getFontMetrics(fontSub);
        int gap = (int) Math.round(2 * S);
        int totalTextH = fmA.getAscent() + gap + fmS.getAscent();
        int topY = attackBtnBounds.y + (attackBtnBounds.height - totalTextH) / 2;
        int line1Y = topY + fmA.getAscent();
        int line2Y = line1Y + gap + fmS.getAscent();

        String atkLabel = "⚔ ATAKE!";
        String subLabel = isWordValid ? "(ISUMITER)" : (word.isEmpty() ? "(Pili ug letra)" : "(Dili balido)");

        if (isWordValid) {
            // Radiant glowing gold button when word is in the dictionary CSV
            g2.setColor(new Color(255, 195, 25));
            g2.fillRoundRect(attackBtnBounds.x, attackBtnBounds.y, attackBtnBounds.width, attackBtnBounds.height, 6, 6);
            g2.setColor(new Color(255, 245, 160));
            g2.setStroke(new BasicStroke(2.0f));
            g2.drawRoundRect(attackBtnBounds.x, attackBtnBounds.y, attackBtnBounds.width, attackBtnBounds.height, 6, 6);

            g2.setFont(fontAtk);
            g2.setColor(new Color(15, 20, 40));
            int aw = fmA.stringWidth(atkLabel);
            g2.drawString(atkLabel, attackBtnBounds.x + (attackBtnBounds.width - aw) / 2, line1Y);

            g2.setFont(fontSub);
            g2.setColor(new Color(40, 50, 80));
            int sw = fmS.stringWidth(subLabel);
            g2.drawString(subLabel, attackBtnBounds.x + (attackBtnBounds.width - sw) / 2, line2Y);
        } else {
            // Dimmed/unlit button when word is not in CSV or empty
            g2.setColor(new Color(18, 22, 45, 180));
            g2.fillRoundRect(attackBtnBounds.x, attackBtnBounds.y, attackBtnBounds.width, attackBtnBounds.height, 6, 6);
            g2.setColor(new Color(45, 55, 85));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(attackBtnBounds.x, attackBtnBounds.y, attackBtnBounds.width, attackBtnBounds.height, 6, 6);

            g2.setFont(fontAtk);
            g2.setColor(new Color(110, 125, 155));
            int aw = fmA.stringWidth(atkLabel);
            g2.drawString(atkLabel, attackBtnBounds.x + (attackBtnBounds.width - aw) / 2, line1Y);

            g2.setFont(fontSub);
            g2.setColor(new Color(90, 105, 135));
            int sw = fmS.stringWidth(subLabel);
            g2.drawString(subLabel, attackBtnBounds.x + (attackBtnBounds.width - sw) / 2, line2Y);
        }
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
                    g2.drawImage(ImageLoader.scaledCached(tileImg, size, size), drawX, drawY, null);

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
                    int ptFontSize = Math.max(10, (int) Math.round(size * 0.20));
                    g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, ptFontSize));
                    g2.setColor(new Color(100, 100, 100));
                    String ptStr = String.valueOf(tile.getPoints());
                    FontMetrics fmPt = g2.getFontMetrics();
                    int ptPad = Math.max(3, (int) (size * 0.08));
                    g2.drawString(ptStr, drawX + size - fmPt.stringWidth(ptStr) - ptPad, drawY + size - ptPad);

                    // Order badge if selected
                    if (isSelected) {
                        int badgeDiam = Math.max(14, (int) Math.round(size * 0.30));
                        int bx = drawX + size - badgeDiam - 2;
                        int by = drawY + 2;
                        g2.setColor(new Color(240, 150, 30));
                        g2.fillOval(bx, by, badgeDiam, badgeDiam);
                        g2.setColor(Color.WHITE);
                        int orderFontSize = Math.max(9, (int) Math.round(badgeDiam * 0.65));
                        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, orderFontSize));
                        FontMetrics fmOrd = g2.getFontMetrics();
                        String ordStr = String.valueOf(order);
                        int ox = bx + (badgeDiam - fmOrd.stringWidth(ordStr)) / 2;
                        int oy = by + (badgeDiam + fmOrd.getAscent() - fmOrd.getDescent()) / 2;
                        g2.drawString(ordStr, ox, oy);
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
                    int ptFontSize = Math.max(10, (int) Math.round(b.height * 0.20));
                    g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, ptFontSize));
                    g2.setColor(new Color(100, 100, 100));
                    String ptStr = String.valueOf(tile.getPoints());
                    FontMetrics fmPt = g2.getFontMetrics();
                    int ptPad = Math.max(3, (int) (b.height * 0.08));
                    g2.drawString(ptStr, b.x + b.width - fmPt.stringWidth(ptStr) - ptPad, b.y + b.height - ptPad);

                    // Order badge if selected
                    if (isSelected) {
                        int badgeDiam = Math.max(14, (int) Math.round(b.height * 0.30));
                        int bx = b.x + b.width - badgeDiam - 2;
                        int by = b.y + 2;
                        g2.setColor(TILE_BORDER_SELECTED);
                        g2.fillOval(bx, by, badgeDiam, badgeDiam);
                        g2.setColor(Color.WHITE);
                        int orderFontSize = Math.max(9, (int) Math.round(badgeDiam * 0.65));
                        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, orderFontSize));
                        FontMetrics fmOrd = g2.getFontMetrics();
                        String ordStr = String.valueOf(order);
                        int ox = bx + (badgeDiam - fmOrd.stringWidth(ordStr)) / 2;
                        int oy = by + (badgeDiam + fmOrd.getAscent() - fmOrd.getDescent()) / 2;
                        g2.drawString(ordStr, ox, oy);
                    }
                }
            }
        }

        // --- Right Action Button: Bag-o (Scramble) & Self-damage penalty hint ---
        drawFlatYellowButton(g2, scrambleBtnBounds, "Bag-o");
        int penaltyFontSize = Math.max(10, (int) Math.round(11 * Math.min(w / 960.0, h / 540.0)));
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, penaltyFontSize));
        g2.setColor(new Color(255, 95, 80));
        String penaltyHint = "-5 HP Kadaot";
        FontMetrics fmPenalty = g2.getFontMetrics();
        int pw = fmPenalty.stringWidth(penaltyHint);
        int py = scrambleBtnBounds.y + scrambleBtnBounds.height + (int) Math.round(16 * (h / 540.0));
        g2.drawString(penaltyHint, scrambleBtnBounds.x + (scrambleBtnBounds.width - pw) / 2, py);
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
        int btnFontSize = Math.max(13, (int) Math.round(14 * Math.min(getWidth() / 960.0, getHeight() / 540.0)));
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, btnFontSize));
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
        if (fillW > 0) {
            g2.setColor(fillCol);
            g2.fillRect(x, y, fillW, bh);
        }
        g2.setColor(new Color(40, 45, 70));
        g2.drawRect(x, y, bw, bh);
    }

    private void drawEnemyHpBarWithPreview(Graphics2D g2, int x, int y, int bw, int bh, int curHp, int maxHp, int dmgPreview, boolean isWordValid) {
        // Trough background
        g2.setColor(new Color(20, 20, 30));
        g2.fillRect(x, y, bw, bh);

        float curFrac = (maxHp > 0) ? Math.clamp((float) curHp / maxHp, 0f, 1f) : 0f;
        int curW = (int) (bw * curFrac);

        if (dmgPreview <= 0) {
            // Normal orange bar
            if (curW > 0) {
                g2.setColor(HP_ORANGE);
                g2.fillRect(x, y, curW, bh);
            }
        } else {
            int remHp = Math.max(0, curHp - dmgPreview);
            float remFrac = (maxHp > 0) ? Math.clamp((float) remHp / maxHp, 0f, 1f) : 0f;
            int remW = (int) (bw * remFrac);
            int dmgW = curW - remW;

            // 1. Remaining HP in solid HP_ORANGE
            if (remW > 0) {
                g2.setColor(HP_ORANGE);
                g2.fillRect(x, y, remW, bh);
            }

            // 2. Prospective Damage slice
            if (dmgW > 0) {
                int dmgX = x + remW;
                double pulse = 0.85 + 0.15 * Math.sin(System.currentTimeMillis() * 0.009);
                int r = (int) Math.clamp(245 * pulse, 180, 255);
                Color damageFill = isWordValid
                    ? new Color(r, 42, 38)
                    : new Color(240, 95, 45, 220);

                g2.setColor(damageFill);
                g2.fillRect(dmgX, y, dmgW, bh);

                // Subtle dividing line between remaining HP and damage chunk
                if (remW > 0) {
                    g2.setColor(new Color(25, 25, 35));
                    g2.fillRect(dmgX, y, 1, bh);
                }

                // Crisp border highlight around the damage slice
                g2.setColor(new Color(255, 205, 205, 190));
                g2.drawRect(dmgX, y, Math.max(0, dmgW - 1), Math.max(0, bh - 1));
            }
        }

        // Clean border around entire trough
        g2.setColor(new Color(40, 45, 70));
        g2.drawRect(x, y, bw, bh);
    }

    /**
     * Large translucent rounded box. A shape fill over the whole area is the main per-frame cost, so the body is
     * filled with plain rects and only the four corner quadrants are antialiased.
     */
    private static void fillPanelBox(Graphics2D g2, int x, int y, int w, int h, int arcW, int arcH) {
        int r = Math.max(1, arcW / 2);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g2.fillRect(x + r, y, w - 2 * r, h);
        g2.fillRect(x, y + r, r, h - 2 * r);
        g2.fillRect(x + w - r, y + r, r, h - 2 * r);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        java.awt.Shape old = g2.getClip();
        int[][] corners = {{x, y}, {x + w - r, y}, {x, y + h - r}, {x + w - r, y + h - r}};
        for (int[] c : corners) {
            g2.setClip(old);
            g2.clipRect(c[0], c[1], r, r);
            g2.fillRoundRect(x, y, w, h, arcW, arcH);
        }
        g2.setClip(old);
    }

    private void renderCachedBackground(Graphics2D g2, int w, int h) {
        if (w <= 0 || h <= 0) return;
        if (cachedBgImage == null || cachedBgW != w || cachedBgH != h || cachedBgLandmark != landmarkIcon) {
            cachedBgImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D bgG2 = cachedBgImage.createGraphics();
            bgG2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            bgG2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            // Layer 1: Sky
            if (skyBg != null) {
                bgG2.drawImage(skyBg, 0, 0, w, h, null);
            } else {
                bgG2.setColor(MIDNIGHT_NAVY);
                bgG2.fillRect(0, 0, w, h);
            }

            // Layer 2: Landmark Background
            if (landmarkIcon != null) {
                int imgW = landmarkIcon.getWidth();
                int imgH = landmarkIcon.getHeight();
                if (imgW > 0 && imgH > 0) {
                    double scale = Math.max((double) (w * 0.88) / imgW, (double) (h * 0.92) / imgH);
                    int drawW = (int) Math.round(imgW * scale);
                    int drawH = (int) Math.round(imgH * scale);
                    int drawX = (w - drawW) / 2;
                    int drawY = h - drawH - (int) (10 * (h / 540.0));
                    bgG2.drawImage(landmarkIcon, drawX, drawY, drawW, drawH, null);
                }
            }

            // Layer 3: Soft Scrim
            bgG2.setColor(new Color(12, 16, 52, 105));
            bgG2.fillRect(0, 0, w, h);

            bgG2.dispose();
            cachedBgW = w;
            cachedBgH = h;
            cachedBgLandmark = landmarkIcon;
        }

        g2.drawImage(cachedBgImage, 0, 0, null);
    }

    private void drawIndigenousFrame(Graphics2D g2, int w, int h) {
        if (indigFrame == null || w <= 0 || h <= 0) return;
        if (cachedIndigFrame == null || cachedFrameW != w || cachedFrameH != h) {
            cachedIndigFrame = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D fg = cachedIndigFrame.createGraphics();
            fg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            fg.drawImage(indigFrame, 0, 0, w, h, null);
            fg.dispose();
            cachedFrameW = w;
            cachedFrameH = h;
            cachedFrameInner = findTransparentInterior(cachedIndigFrame);
        }
        Rectangle in = cachedFrameInner;
        if (in == null) {
            g2.drawImage(cachedIndigFrame, 0, 0, null);
            return;
        }
        // Interior is fully transparent: blit only the four border strips (saves a full-screen alpha blend per frame).
        g2.drawImage(cachedIndigFrame, 0, 0, w, in.y, 0, 0, w, in.y, null);
        g2.drawImage(cachedIndigFrame, 0, in.y + in.height, w, h, 0, in.y + in.height, w, h, null);
        g2.drawImage(cachedIndigFrame, 0, in.y, in.x, in.y + in.height, 0, in.y, in.x, in.y + in.height, null);
        g2.drawImage(cachedIndigFrame, in.x + in.width, in.y, w, in.y + in.height, in.x + in.width, in.y, w, in.y + in.height, null);
    }

    /** Returns the fully transparent inner rectangle of the frame, or null if it cannot be proven. */
    private static Rectangle findTransparentInterior(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        int left = 0;
        while (left < w && (img.getRGB(left, h / 2) >>> 24) != 0) left++;
        int right = w - 1;
        while (right > left && (img.getRGB(right, h / 2) >>> 24) != 0) right--;
        int top = 0;
        while (top < h && (img.getRGB(w / 2, top) >>> 24) != 0) top++;
        int bottom = h - 1;
        while (bottom > top && (img.getRGB(w / 2, bottom) >>> 24) != 0) bottom--;
        if (right <= left || bottom <= top) return null;
        int[] row = new int[right - left + 1];
        for (int y = top; y <= bottom; y++) {
            img.getRGB(left, y, row.length, 1, row, 0, row.length);
            for (int p : row) {
                if ((p >>> 24) != 0) return null;
            }
        }
        return new Rectangle(left, top, right - left + 1, bottom - top + 1);
    }

    public static int calculatePreviewRemainingHp(int curHp, int dmgPreview) {
        return Math.max(0, curHp - Math.max(0, dmgPreview));
    }

    public static int calculateDamageSliceWidth(int barWidth, int curHp, int maxHp, int dmgPreview) {
        if (maxHp <= 0 || curHp <= 0 || dmgPreview <= 0) return 0;
        float curFrac = Math.clamp((float) curHp / maxHp, 0f, 1f);
        int curW = (int) (barWidth * curFrac);
        int remHp = Math.max(0, curHp - dmgPreview);
        float remFrac = Math.clamp((float) remHp / maxHp, 0f, 1f);
        int remW = (int) (barWidth * remFrac);
        return curW - remW;
    }

    private void drawPauseOverlay(Graphics2D g2, int w, int h) {
        g2.setColor(new Color(0, 0, 0, 185));
        g2.fillRect(0, 0, w, h);
        int pauseFontSize = Math.max(32, (int) Math.round(36 * Math.min(w / 960.0, h / 540.0)));
        g2.setFont(new Font(Font.SERIF, Font.BOLD, pauseFontSize));
        g2.setColor(Color.WHITE);
        String pStr = "GI-PAUSE";
        FontMetrics fm = g2.getFontMetrics();
        int pw = fm.stringWidth(pStr);
        int py = (h + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(pStr, (w - pw) / 2, py);
    }
}
