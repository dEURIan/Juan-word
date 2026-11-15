import juanword.*;
import juanword.BattlePanel.HeroAnimState;
import juanword.BattlePanel.JuanAvatarPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;

public class TestJuanAttackAnimation {

    public static void main(String[] args) throws Exception {
        System.out.println("=== RUNNING TEST SUITE: JUAN CHARACTER SPRITE & ATTACK ANIMATION ===");

        final CountDownLatch testCompleted = new CountDownLatch(1);

        SwingUtilities.invokeLater(() -> {
            try {
                JuanWord game = new JuanWord();
                Player juan = new Player("P_TEST", "Juan");
                juan.currentHealth = 100;
                juan.unlockedMapIndex = 1;
                game.setCurrentPlayer(juan);

                BattlePanel battle = new BattlePanel(game, DataManager.getInstance());
                battle.setSize(960, 540);
                battle.setupBattle(juan, "Blue Building", 1, "Sayon");
                battle.doLayout();

                JuanAvatarPanel avatar = battle.getJuanAvatarPanel();
                assert avatar != null : "JuanAvatarPanel must be initialized in BattlePanel";

                // --- 1. Test Initial Idle Stance ---
                System.out.println("\n--- 1. Testing Initial Idle Stance ---");
                assert avatar.getAnimState() == HeroAnimState.IDLE : "Initial state must be IDLE, got: " + avatar.getAnimState();
                assert avatar.getLungeOffset() == 0 : "Initial lunge offset must be 0";
                assert !avatar.isAttacking() : "isAttacking must initially be false";
                System.out.println("  ✓ Initial stance is IDLE with 0 lunge offset and isAttacking == false.");

                // --- 2. Test Manual Animation States & Visual Rendering ---
                System.out.println("\n--- 2. Testing Manual Animation States & Sprite Rendering ---");
                // 2a. IDLE
                avatar.setManualStateForTesting(HeroAnimState.IDLE, 0);
                assert avatar.getAnimState() == HeroAnimState.IDLE : "State must be IDLE";
                BufferedImage idleImg = renderAvatarToImage(avatar);
                assert idleImg != null : "Must render IDLE image";
                ImageIO.write(idleImg, "PNG", new File("scratch/anim_juan_idle_stance.png"));

                // 2b. PRE_ATTACK
                avatar.setManualStateForTesting(HeroAnimState.PRE_ATTACK, -6);
                assert avatar.getAnimState() == HeroAnimState.PRE_ATTACK : "State must be PRE_ATTACK";
                assert avatar.getLungeOffset() == -6 : "Pre-attack lunge offset must be -6";
                BufferedImage preImg = renderAvatarToImage(avatar);
                assert preImg != null : "Must render PRE_ATTACK image";
                ImageIO.write(preImg, "PNG", new File("scratch/anim_juan_preattack_stance.png"));

                // 2c. ATTACK
                avatar.setManualStateForTesting(HeroAnimState.ATTACK, 42);
                assert avatar.getAnimState() == HeroAnimState.ATTACK : "State must be ATTACK";
                assert avatar.getLungeOffset() == 42 : "Attack lunge offset must be +42";
                BufferedImage attImg = renderAvatarToImage(avatar);
                assert attImg != null : "Must render ATTACK image";
                ImageIO.write(attImg, "PNG", new File("scratch/anim_juan_attack_stance.png"));

                // Reset back to IDLE
                avatar.setManualStateForTesting(HeroAnimState.IDLE, 0);
                System.out.println("  ✓ Manual states (IDLE, PRE_ATTACK, ATTACK) verified and rendered.");

                // --- 3. Test Full Attack Animation Sequence & Callbacks ---
                System.out.println("\n--- 3. Testing Triggered Attack Animation & Timing Callbacks ---");
                AtomicBoolean impactFired = new AtomicBoolean(false);
                AtomicInteger impactLunge = new AtomicInteger(0);
                HeroAnimState[] impactState = new HeroAnimState[1];
                AtomicBoolean completeFired = new AtomicBoolean(false);

                avatar.triggerAttack(
                    () -> {
                        impactFired.set(true);
                        impactLunge.set(avatar.getLungeOffset());
                        impactState[0] = avatar.getAnimState();
                    },
                    () -> {
                        completeFired.set(true);
                        assert avatar.getAnimState() == HeroAnimState.IDLE : "Must return to IDLE on completion";
                        assert avatar.getLungeOffset() == 0 : "Must reset lunge offset to 0 on completion";
                        assert !avatar.isAttacking() : "isAttacking must be false on completion";
                        System.out.println("  ✓ Attack complete callback executed: returned to IDLE with lungeOffset == 0.");

                        assert impactFired.get() : "onImpact callback must have fired during attack";
                        assert impactState[0] == HeroAnimState.ATTACK : "onImpact must fire during ATTACK stance, got: " + impactState[0];
                        assert impactLunge.get() > 0 : "onImpact must fire with positive forward lunge, got: " + impactLunge.get();
                        System.out.println("  ✓ Strike impact verified: fired during ATTACK stance with forward lunge = " + impactLunge.get() + "px.");

                        // --- 4. Test Hit Reaction Animation ---
                        testHitReaction(avatar, () -> {
                            // --- 5. Test Word Submission Triggers Attack Sequence ---
                            testBattleWordAttack(game, juan, testCompleted);
                        });
                    }
                );

            } catch (Throwable t) {
                t.printStackTrace();
                System.exit(1);
            }
        });

        boolean ok = testCompleted.await(12, TimeUnit.SECONDS);
        if (!ok) {
            System.err.println("❌ Test timed out waiting for animation callbacks!");
            System.exit(1);
        }

        System.out.println("\n=== ALL JUAN CHARACTER SPRITE & ATTACK ANIMATION TESTS PASSED! ===");
        System.exit(0);
    }

    private static void testHitReaction(JuanAvatarPanel avatar, Runnable onDone) {
        System.out.println("\n--- 4. Testing Juan Hit Reaction Animation ---");
        avatar.triggerHit();

        try {
            Field hurtField = JuanAvatarPanel.class.getDeclaredField("isHurt");
            hurtField.setAccessible(true);
            boolean hurt = (Boolean) hurtField.get(avatar);
            assert hurt : "Juan must be in hurt state immediately upon triggerHit()";
            System.out.println("  ✓ Juan entered hurt state with red flash & shake.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Wait 350ms for shake animation to finish
        Timer checkTimer = new Timer(350, e -> {
            ((Timer) e.getSource()).stop();
            try {
                Field hurtField = JuanAvatarPanel.class.getDeclaredField("isHurt");
                hurtField.setAccessible(true);
                boolean hurt = (Boolean) hurtField.get(avatar);
                assert !hurt : "Juan must recover from hurt state after shake timer";

                Field shakeField = JuanAvatarPanel.class.getDeclaredField("shakeOffset");
                shakeField.setAccessible(true);
                int shake = (Integer) shakeField.get(avatar);
                assert shake == 0 : "Shake offset must reset to 0 after hurt animation";

                System.out.println("  ✓ Juan recovered from hit reaction (isHurt == false, shakeOffset == 0).");
                onDone.run();
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        });
        checkTimer.setRepeats(false);
        checkTimer.start();
    }

    private static void testBattleWordAttack(JuanWord game, Player player, CountDownLatch latch) {
        System.out.println("\n--- 5. Testing Full Word Submission & Attack Execution in Battle ---");
        try {
            BattlePanel battle = new BattlePanel(game, DataManager.getInstance());
            battle.setSize(960, 540);
            battle.setupBattle(player, "Blue Building", 1, "Sayon");
            battle.doLayout();

            // Dismiss title card overlay
            Field overlayField = BattlePanel.class.getDeclaredField("titleCardOverlay");
            overlayField.setAccessible(true);
            JPanel overlay = (JPanel) overlayField.get(battle);
            if (overlay != null) overlay.setVisible(false);

            // Populate known valid Cebuano word tiles: A B A G A (abaga)
            battle.restoreTiles("A:1:STANDARD;B:2:STANDARD;A:1:STANDARD;G:2:STANDARD;A:1:STANDARD;D:3:STANDARD;A:1:STANDARD;B:2:STANDARD;A:1:STANDARD;W:4:STANDARD;E:1:STANDARD;N:2:STANDARD;Y:3:STANDARD;O:1:STANDARD;L:2:STANDARD;A:1:STANDARD");

            Field tilesField = BattlePanel.class.getDeclaredField("tiles");
            tilesField.setAccessible(true);
            JButton[] tiles = (JButton[]) tilesField.get(battle);

            // Click A B A G A
            tiles[0].doClick();
            tiles[1].doClick();
            tiles[2].doClick();
            tiles[3].doClick();
            tiles[4].doClick();

            Field enemyHpField = BattlePanel.class.getDeclaredField("enemyCurHp");
            enemyHpField.setAccessible(true);
            int initialEnemyHp = (Integer) enemyHpField.get(battle);

            Field attackBtnField = BattlePanel.class.getDeclaredField("attackButton");
            attackBtnField.setAccessible(true);
            JButton attackBtn = (JButton) attackBtnField.get(battle);

            JuanAvatarPanel avatar = battle.getJuanAvatarPanel();

            // Trigger attack!
            attackBtn.doClick();

            assert avatar.isAttacking() : "Juan must be attacking upon clicking attack button";
            System.out.println("  ✓ Clicking Attack button triggered Juan's attack animation.");

            // Wait 550ms for attack sequence to complete impact and return to idle
            Timer postAttackTimer = new Timer(550, e -> {
                ((Timer) e.getSource()).stop();
                try {
                    int remainingEnemyHp = (Integer) enemyHpField.get(battle);
                    assert remainingEnemyHp < initialEnemyHp : "Enemy must take damage on strike impact! initial=" + initialEnemyHp + ", current=" + remainingEnemyHp;
                    System.out.println("  ✓ Enemy took damage from Juan's attack! HP dropped from " + initialEnemyHp + " to " + remainingEnemyHp + ".");

                    assert avatar.getAnimState() == HeroAnimState.IDLE : "Juan must return to IDLE after attack sequence, got: " + avatar.getAnimState();
                    System.out.println("  ✓ Juan smoothly returned to IDLE stance after word submission attack.");

                    latch.countDown();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    System.exit(1);
                }
            });
            postAttackTimer.setRepeats(false);
            postAttackTimer.start();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static BufferedImage renderAvatarToImage(JuanAvatarPanel panel) {
        BufferedImage img = new BufferedImage(185, 110, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(16, 16, 86));
        g.fillRect(0, 0, 185, 110);
        panel.setSize(185, 110);
        panel.paint(g);
        g.dispose();
        return img;
    }
}
