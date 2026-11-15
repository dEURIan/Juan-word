import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import juanword.*;

public class PrototypeEnemyTransition {
    static class DefeatParticle {
        double x, y, vx, vy;
        int size;
        Color color;
        float alpha;

        DefeatParticle(double x, double y, double vx, double vy, int size, Color color) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.size = size;
            this.color = color;
            this.alpha = 1.0f;
        }

        void update() {
            x += vx;
            y += vy;
            vy += 0.4; // gravity
            alpha = Math.max(0f, alpha - 0.05f);
        }

        void render(Graphics2D g2) {
            if (alpha <= 0.01f) return;
            Graphics2D gP = (Graphics2D) g2.create();
            gP.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            gP.setColor(color);
            gP.fillOval((int) x, (int) y, size, size);
            gP.dispose();
        }
    }

    public static void main(String[] args) throws Exception {
        BufferedImage bg = ImageIO.read(new File("scratch/battle_with_new_alphabet_tiles.png"));
        int w = bg.getWidth();
        int h = bg.getHeight();

        Enemy dizon = EnemyFactory.createEnemy(1, BattlePanel.DifficultySetting.SAKTO);
        Enemy alviola = EnemyFactory.createEnemy(2, BattlePanel.DifficultySetting.SAKTO);

        // Enemy position in battle screen (from our measurements: resting center x=780, y=180)
        int restingX = 740;
        int restingY = 135;
        int enemyW = 110;
        int enemyH = 85;

        // --- MILESTONE 1: Enemy Fatal Hit (Red Flash & Shaking) ---
        BufferedImage f1 = copy(bg);
        Graphics2D g1 = f1.createGraphics();
        g1.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Cover resting enemy area with clean background if needed, or draw hurt state
        dizon.render(g1, restingX - 8, restingY, enemyW, enemyH, true, -8);
        g1.dispose();
        ImageIO.write(f1, "png", new File("scratch/anim_death_01_hit.png"));

        // --- MILESTONE 2: Mid-Death Shrink, Sink & Particles ---
        BufferedImage f2 = copy(bg);
        Graphics2D g2 = f2.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Paint over resting enemy with background slice from clean backdrop or draw fading
        float alpha = 0.45f;
        float scale = 0.65f;
        int dropY = 15;
        Graphics2D gDeath = (Graphics2D) g2.create();
        gDeath.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        int cx = restingX + enemyW / 2;
        int cy = restingY + enemyH / 2;
        gDeath.translate(cx, cy + dropY);
        gDeath.scale(scale, scale);
        gDeath.translate(-cx, -cy);
        dizon.render(gDeath, restingX, restingY, enemyW, enemyH, true, 0);
        gDeath.dispose();

        // Particles
        List<DefeatParticle> particles = new ArrayList<>();
        Color[] pColors = {new Color(245, 184, 32), new Color(211, 111, 2), Color.WHITE, new Color(140, 150, 175)};
        for (int i = 0; i < 16; i++) {
            double angle = (2 * Math.PI / 16) * i;
            double speed = 3.5 + (i % 3) * 1.5;
            particles.add(new DefeatParticle(cx, cy, Math.cos(angle) * speed, Math.sin(angle) * speed - 2.5, 6 + (i % 4) * 2, pColors[i % pColors.length]));
        }
        for (DefeatParticle p : particles) {
            p.update();
            p.update();
            p.render(g2);
        }
        g2.dispose();
        ImageIO.write(f2, "png", new File("scratch/anim_death_02_particles.png"));

        // --- MILESTONE 3: Complete Disappearance (Poofed Away) ---
        BufferedImage f3 = copy(bg);
        // In the real panel, dizonBox is simply not rendering the enemy body here!
        ImageIO.write(f3, "png", new File("scratch/anim_death_03_disappeared.png"));

        // --- MILESTONE 4: New Enemy (Alviola) Entering From Right Edge (+260px) ---
        BufferedImage f4 = copy(bg);
        Graphics2D g4 = f4.createGraphics();
        g4.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int enterOffset4 = 220; // Entering from right border
        // Wind trail
        g4.setColor(new Color(110, 210, 255, 180));
        g4.setStroke(new BasicStroke(3.0f));
        g4.drawLine(restingX + enterOffset4 + enemyW + 5, restingY + 25, restingX + enterOffset4 + enemyW + 40, restingY + 25);
        g4.drawLine(restingX + enterOffset4 + enemyW + 10, restingY + 50, restingX + enterOffset4 + enemyW + 55, restingY + 50);
        alviola.render(g4, restingX + enterOffset4, restingY, enemyW, enemyH, false, 0);
        g4.dispose();
        ImageIO.write(f4, "png", new File("scratch/anim_enter_04_rush_in.png"));

        // --- MILESTONE 5: New Enemy Settled in Stance (Offset = 0) ---
        BufferedImage f5 = copy(bg);
        Graphics2D g5 = f5.createGraphics();
        g5.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        alviola.render(g5, restingX, restingY, enemyW, enemyH, false, 0);
        g5.dispose();
        ImageIO.write(f5, "png", new File("scratch/anim_enter_05_settled.png"));

        System.out.println("Rendered 5 milestone animation frames successfully!");
    }

    private static BufferedImage copy(BufferedImage src) {
        BufferedImage b = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = b.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return b;
    }
}
