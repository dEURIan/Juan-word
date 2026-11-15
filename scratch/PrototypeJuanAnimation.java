package scratch;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.*;

public class PrototypeJuanAnimation {

    public enum HeroAnimState {
        IDLE, PRE_ATTACK, ATTACK
    }

    public static class TestJuanAvatarPanel extends JPanel {
        private final Image idleSprite;
        private final Image preAttackSprite;
        private final Image attackSprite;

        private HeroAnimState currentAnimState = HeroAnimState.IDLE;
        private boolean isAttacking = false;
        private int lungeOffset = 0;
        private int shakeOffset = 0;
        private boolean isHurt = false;
        private float hitFlashAlpha = 0f;

        public TestJuanAvatarPanel(Image idle, Image preAttack, Image attack) {
            this.idleSprite = idle;
            this.preAttackSprite = preAttack;
            this.attackSprite = attack;
            setOpaque(false);
            setPreferredSize(new Dimension(140, 110));
        }

        public HeroAnimState getAnimState() {
            return currentAnimState;
        }

        public void setManualState(HeroAnimState state, int lunge) {
            this.currentAnimState = state;
            this.lungeOffset = lunge;
            this.isAttacking = (state != HeroAnimState.IDLE || lunge != 0);
            repaint();
        }

        public void triggerHit() {
            this.isHurt = true;
            this.hitFlashAlpha = 0.75f;
            this.shakeOffset = -8;
            repaint();

            final int[] frame = {0};
            final int[] offsets = {-8, 8, -6, 5, -3, 0};
            javax.swing.Timer shakeTimer = new javax.swing.Timer(45, null);
            shakeTimer.addActionListener(e -> {
                frame[0]++;
                if (frame[0] < offsets.length) {
                    shakeOffset = offsets[frame[0]];
                    hitFlashAlpha = Math.max(0f, hitFlashAlpha - 0.15f);
                    repaint();
                } else {
                    shakeOffset = 0;
                    isHurt = false;
                    hitFlashAlpha = 0f;
                    repaint();
                    shakeTimer.stop();
                }
            });
            shakeTimer.start();
        }

        public void triggerAttack(Runnable onImpact, Runnable onComplete) {
            this.isAttacking = true;
            this.isHurt = false;
            this.shakeOffset = 0;
            this.lungeOffset = 0;
            this.currentAnimState = HeroAnimState.PRE_ATTACK;

            // Frame animation steps:
            // 0..2: PRE_ATTACK (wind-up, subtle pullback)
            // 3..7: ATTACK (sword strike thrust, peak at frame 5)
            // 8..10: IDLE (recovery back to 0)
            final int[] frame = {0};
            final int[] offsets = {-3, -6, -4, 15, 30, 42, 38, 26, 14, 5, 0};
            final HeroAnimState[] states = {
                HeroAnimState.PRE_ATTACK,
                HeroAnimState.PRE_ATTACK,
                HeroAnimState.PRE_ATTACK,
                HeroAnimState.ATTACK,
                HeroAnimState.ATTACK,
                HeroAnimState.ATTACK,
                HeroAnimState.ATTACK,
                HeroAnimState.ATTACK,
                HeroAnimState.IDLE,
                HeroAnimState.IDLE,
                HeroAnimState.IDLE
            };

            javax.swing.Timer attackTimer = new javax.swing.Timer(36, null);
            attackTimer.addActionListener(e -> {
                frame[0]++;
                if (frame[0] < offsets.length) {
                    lungeOffset = offsets[frame[0]];
                    currentAnimState = states[frame[0]];
                    if (frame[0] == 5) { // Apex of sword strike
                        if (onImpact != null) onImpact.run();
                    }
                    repaint();
                } else {
                    lungeOffset = 0;
                    currentAnimState = HeroAnimState.IDLE;
                    isAttacking = false;
                    repaint();
                    attackTimer.stop();
                    if (onComplete != null) onComplete.run();
                }
            });
            attackTimer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setClip(null); // Allow sword lunge without boundary clipping

            int h = Math.min(getHeight(), 115);
            if (h <= 0) h = 110;
            int w = (int) Math.round(h * (300.0 / 500.0)); // Preserve exact 3:5 aspect ratio

            int x = (getWidth() - w) / 2 + shakeOffset + lungeOffset;
            int y = (getHeight() - h) / 2;

            // Shadow under Juan
            g2.setColor(new Color(0, 0, 0, 55));
            g2.fillOval(x + 4, y + h - 7, w - 8, 12);

            // Motion wind/slash trails when lunging forward
            if (isAttacking && lungeOffset > 10) {
                g2.setColor(new Color(255, 220, 110, Math.min(220, lungeOffset * 5)));
                g2.setStroke(new BasicStroke(2.2f));
                g2.drawLine(x - 24, y + h / 2 - 12, x - 4, y + h / 2 - 12);
                g2.drawLine(x - 36, y + h / 2 + 6, x - 6, y + h / 2 + 6);
                g2.drawLine(x - 20, y + h / 2 + 22, x - 4, y + h / 2 + 22);
            }

            Image spriteToDraw;
            switch (currentAnimState) {
                case PRE_ATTACK:
                    spriteToDraw = (preAttackSprite != null) ? preAttackSprite : idleSprite;
                    break;
                case ATTACK:
                    spriteToDraw = (attackSprite != null) ? attackSprite : idleSprite;
                    break;
                case IDLE:
                default:
                    spriteToDraw = idleSprite;
                    break;
            }

            if (spriteToDraw != null) {
                g2.drawImage(spriteToDraw, x, y, w, h, null);
                if (isHurt && hitFlashAlpha > 0f) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, hitFlashAlpha));
                    g2.setColor(new Color(210, 40, 40));
                    g2.fillRect(x, y, w, h);
                }
            } else {
                g2.setFont(new Font("Arial", Font.BOLD, 18));
                g2.setColor(isHurt ? Color.RED : Color.WHITE);
                g2.drawString("👤 JUAN", x + 5, y + h / 2);
            }

            if (isHurt) {
                g2.setFont(new Font("Arial", Font.BOLD, 22));
                g2.drawString("💥", x + w / 2, y + 20);
            }

            g2.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        BufferedImage idle = ImageIO.read(new File("src/juanword/maincharacteridle.png"));
        BufferedImage pre = ImageIO.read(new File("src/juanword/maincharapreattack.png"));
        BufferedImage att = ImageIO.read(new File("src/juanword/maincharattack.png"));

        TestJuanAvatarPanel panel = new TestJuanAvatarPanel(idle, pre, att);
        panel.setSize(185, 110);

        // Capture 4 key animation frames:
        // 1. Idle stance (lunge 0)
        // 2. Pre-attack wind-up stance (lunge -6)
        // 3. Attack strike stance (apex lunge +42)
        // 4. Return to Idle stance (lunge 0)
        Object[][] testCases = {
            {"1. IDLE STANCE", HeroAnimState.IDLE, 0, "scratch/anim_juan_01_idle.png"},
            {"2. PRE-ATTACK WINDUP", HeroAnimState.PRE_ATTACK, -6, "scratch/anim_juan_02_preattack.png"},
            {"3. ATTACK STRIKE APEX", HeroAnimState.ATTACK, 42, "scratch/anim_juan_03_attack.png"},
            {"4. RETURN TO IDLE", HeroAnimState.IDLE, 0, "scratch/anim_juan_04_return_idle.png"}
        };

        BufferedImage composite = new BufferedImage(185 * 4 + 40, 160, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gComp = composite.createGraphics();
        gComp.setColor(new Color(16, 16, 86));
        gComp.fillRect(0, 0, composite.getWidth(), composite.getHeight());

        for (int i = 0; i < testCases.length; i++) {
            String title = (String) testCases[i][0];
            HeroAnimState state = (HeroAnimState) testCases[i][1];
            int lunge = (Integer) testCases[i][2];
            String filename = (String) testCases[i][3];

            panel.setManualState(state, lunge);

            BufferedImage frameImg = new BufferedImage(185, 110, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gFrame = frameImg.createGraphics();
            panel.paint(gFrame);
            gFrame.dispose();

            ImageIO.write(frameImg, "PNG", new File(filename));

            int targetX = 10 + i * (185 + 5);
            gComp.drawImage(frameImg, targetX, 30, null);
            gComp.setColor(Color.WHITE);
            gComp.setFont(new Font("Arial", Font.BOLD, 11));
            FontMetrics fm = gComp.getFontMetrics();
            gComp.drawString(title, targetX + (185 - fm.stringWidth(title)) / 2, 20);
        }
        gComp.dispose();

        File compOut = new File("scratch/anim_juan_sequence_composite.png");
        ImageIO.write(composite, "PNG", compOut);
        System.out.println("Generated animation sequence composite at: " + compOut.getAbsolutePath());
    }
}
