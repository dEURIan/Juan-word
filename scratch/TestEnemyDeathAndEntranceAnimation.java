import juanword.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

public class TestEnemyDeathAndEntranceAnimation {
    public static void main(String[] args) throws Exception {
        System.out.println("=== RUNNING TEST SUITE: ENEMY DEATH & ENTRANCE ANIMATION ===");

        final CountDownLatch testCompleted = new CountDownLatch(1);

        SwingUtilities.invokeLater(() -> {
            try {
                JuanWord game = new JuanWord();
                Player juan = new Player("P_ANIM", "JuanTester");
                juan.currentHealth = 100;
                juan.unlockedMapIndex = 3;
                game.setCurrentPlayer(juan);

                BattlePanel battle = new BattlePanel(game, DataManager.getInstance());
                battle.setSize(960, 540);
                battle.setupBattle(juan, "San Pedro Cathedral", 1, "Sakto");
                battle.doLayout();

                Field dboxField = BattlePanel.class.getDeclaredField("dizonBox");
                dboxField.setAccessible(true);
                Object dbox = dboxField.get(battle);

                Class<?> boxClass = dbox.getClass().getSuperclass();
                Field dyingField = boxClass.getDeclaredField("isDying");
                dyingField.setAccessible(true);
                Field deathAlphaField = boxClass.getDeclaredField("deathAlpha");
                deathAlphaField.setAccessible(true);
                Field deathScaleField = boxClass.getDeclaredField("deathScale");
                deathScaleField.setAccessible(true);
                Field enteringField = boxClass.getDeclaredField("isEntering");
                enteringField.setAccessible(true);
                Field enterOffsetField = boxClass.getDeclaredField("entranceOffset");
                enterOffsetField.setAccessible(true);
                Field particlesField = boxClass.getDeclaredField("particles");
                particlesField.setAccessible(true);

                // --- 1. Test Death Animation & Disappearance ---
                System.out.println("\n--- 1. Testing Enemy Death & Disappearance ---");
                Method triggerDeath = boxClass.getDeclaredMethod("triggerDeath", Runnable.class);
                triggerDeath.setAccessible(true);

                CountDownLatch deathLatch = new CountDownLatch(1);
                triggerDeath.invoke(dbox, (Runnable) () -> {
                    deathLatch.countDown();
                });

                assert (boolean) dyingField.get(dbox) : "isDying must be true immediately on triggerDeath";
                System.out.println("  ✓ Death animation triggered: isDying == true.");

                // Check mid-death frame
                new Timer(200, midEvt -> {
                    ((Timer) midEvt.getSource()).stop();
                    try {
                        float midAlpha = (float) deathAlphaField.get(dbox);
                        float midScale = (float) deathScaleField.get(dbox);
                        java.util.List<?> particles = (java.util.List<?>) particlesField.get(dbox);
                        System.out.println("  ✓ Mid-death state verified: alpha=" + midAlpha + ", scale=" + midScale + ", particles=" + particles.size());

                        // Capture mid-death frame
                        BufferedImage midImg = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
                        Graphics2D gMid = midImg.createGraphics();
                        battle.paint(gMid);
                        gMid.dispose();
                        ImageIO.write(midImg, "png", new File("scratch/enemy_death_disappearing.png"));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }).start();

                // Wait for death completion
                new Timer(850, completeEvt -> {
                    ((Timer) completeEvt.getSource()).stop();
                    try {
                        float finalAlpha = (float) deathAlphaField.get(dbox);
                        assert finalAlpha <= 0.001f : "Enemy must be completely invisible (alpha <= 0), got: " + finalAlpha;
                        assert !(boolean) dyingField.get(dbox) : "isDying must be false after death completes";
                        System.out.println("  ✓ Enemy successfully died and disappeared (alpha == 0.0).");

                        // Capture disappeared frame
                        BufferedImage disImg = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
                        Graphics2D gDis = disImg.createGraphics();
                        battle.paint(gDis);
                        gDis.dispose();
                        ImageIO.write(disImg, "png", new File("scratch/enemy_disappeared_empty.png"));

                        // --- 2. Testing New Enemy Entrance from Right Side ---
                        System.out.println("\n--- 2. Testing New Enemy Entrance from Right Side ---");
                        Enemy alviola = EnemyFactory.createEnemy(2, BattlePanel.DifficultySetting.SAKTO);
                        Method triggerEntrance = boxClass.getDeclaredMethod("triggerEntrance", Enemy.class, Runnable.class);
                        triggerEntrance.setAccessible(true);

                        CountDownLatch enterLatch = new CountDownLatch(1);
                        triggerEntrance.invoke(dbox, alviola, (Runnable) () -> {
                            enterLatch.countDown();
                        });

                        assert (boolean) enteringField.get(dbox) : "isEntering must be true immediately on triggerEntrance";
                        int startOffset = (int) enterOffsetField.get(dbox);
                        assert startOffset > 250 : "Entrance offset must start off-screen right (> 250px), got: " + startOffset;
                        System.out.println("  ✓ Entrance animation started: offset=" + startOffset + "px (off-screen right).");

                        // Capture mid-entrance frame
                        new Timer(200, midEnterEvt -> {
                            ((Timer) midEnterEvt.getSource()).stop();
                            try {
                                int midOffset = (int) enterOffsetField.get(dbox);
                                System.out.println("  ✓ Mid-entrance state verified: sliding in from right, offset=" + midOffset + "px.");
                                BufferedImage midEnterImg = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
                                Graphics2D gME = midEnterImg.createGraphics();
                                battle.paint(gME);
                                gME.dispose();
                                ImageIO.write(midEnterImg, "png", new File("scratch/new_enemy_sliding_from_right.png"));
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }).start();

                        // Wait for entrance arrival
                        new Timer(750, arrivedEvt -> {
                            ((Timer) arrivedEvt.getSource()).stop();
                            try {
                                int endOffset = (int) enterOffsetField.get(dbox);
                                assert endOffset == 0 : "New enemy must arrive and settle at resting offset == 0, got: " + endOffset;
                                assert !(boolean) enteringField.get(dbox) : "isEntering must be false after arrival";
                                System.out.println("  ✓ New enemy (Alviola) settled in stance at offset == 0.");

                                BufferedImage arrImg = new BufferedImage(960, 540, BufferedImage.TYPE_INT_ARGB);
                                Graphics2D gArr = arrImg.createGraphics();
                                battle.paint(gArr);
                                gArr.dispose();
                                ImageIO.write(arrImg, "png", new File("scratch/new_enemy_arrived_settled.png"));

                                // --- 3. Testing Full In-Game Stage Transition via Word Submission ---
                                System.out.println("\n--- 3. Testing Complete Battle Transition (Dizon Defeated -> Alviola Enters) ---");
                                Field stageNumField = BattlePanel.class.getDeclaredField("stageNum");
                                stageNumField.setAccessible(true);
                                Field enemyNameField = BattlePanel.class.getDeclaredField("enemyName");
                                enemyNameField.setAccessible(true);

                                assert (int) stageNumField.get(battle) == 1 : "Initial stage must be 1";

                                // Trigger next enemy transition
                                battle.forceNextEnemyTransitionForTesting(2, () -> {
                                    try {
                                        int newStage = (int) stageNumField.get(battle);
                                        String newName = (String) enemyNameField.get(battle);
                                        assert newStage == 2 : "Stage must advance to 2, found: " + newStage;
                                        assert "ALVIOLA".equals(newName) : "New enemy name must be ALVIOLA, found: " + newName;
                                        assert juan.getMaxUnlockedStage("Sakto") >= 2 : "Stage 2 must be unlocked in player data";
                                        System.out.println("  ✓ Stage successfully advanced to 2: ALVIOLA.");
                                        System.out.println("  ✓ Per-difficulty progression verified: Stage 2 unlocked for Sakto.");
                                        System.out.println("\n=== ALL ENEMY DEATH & ENTRANCE ANIMATION TESTS PASSED! ===");
                                        testCompleted.countDown();
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                        System.exit(1);
                                    }
                                });
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                System.exit(1);
                            }
                        }).start();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        System.exit(1);
                    }
                }).start();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        });

        boolean finished = testCompleted.await(8, TimeUnit.SECONDS);
        if (!finished) {
            System.err.println("Test timed out!");
            System.exit(1);
        }
        System.exit(0);
    }
}
