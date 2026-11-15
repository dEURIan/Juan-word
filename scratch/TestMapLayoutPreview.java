import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class TestMapLayoutPreview {
    public static void main(String[] args) throws Exception {
        BufferedImage bg = ImageIO.read(new File("bg.png"));
        BufferedImage mtapo = ImageIO.read(new File("mtapo.png"));
        BufferedImage frame = ImageIO.read(new File("indigframe.png"));
        BufferedImage blueBldg = ImageIO.read(new File("Blue Building.png")).getSubimage(0, 0, 648, 512);
        BufferedImage durianMuseum = ImageIO.read(new File("durianmuseum.png")).getSubimage(0, 300, 1080, 1620);
        BufferedImage sanPedro = ImageIO.read(new File("sanpedro.png")).getSubimage(0, 201, 1612, 865);

        int w = 960;
        int h = 540;

        // Silhouette color: rich obsidian dark with a subtle blue/charcoal tint
        Color silColor = new Color(14, 18, 28, 252);
        BufferedImage silMtApo = createSilhouette(mtapo, new Color(18, 24, 36, 245));
        BufferedImage silDurian = createSilhouette(durianMuseum, silColor);
        BufferedImage silSanPedro = createSilhouette(sanPedro, silColor);

        // Simulation: Juan has ONLY Blue Building unlocked (unlockedMapIndex = 1)
        int unlockedMap = 1;

        BufferedImage preview = renderMapScreen(w, h, bg, mtapo, frame, blueBldg, durianMuseum, sanPedro,
                                                silMtApo, silDurian, silSanPedro, unlockedMap, -1, 1.0f);

        File out = new File("scratch/juan_only_blue_building_preview.png");
        ImageIO.write(preview, "png", out);
        System.out.println("Saved preview to " + out.getAbsolutePath());

        // Also test hover on Blue Building
        BufferedImage hoverBB = renderMapScreen(w, h, bg, mtapo, frame, blueBldg, durianMuseum, sanPedro,
                                                silMtApo, silDurian, silSanPedro, unlockedMap, 0, 1.0f);
        ImageIO.write(hoverBB, "png", new File("scratch/hover_blue_building_preview.png"));

        // Also test hover on locked Davao Museum
        BufferedImage hoverDM = renderMapScreen(w, h, bg, mtapo, frame, blueBldg, durianMuseum, sanPedro,
                                                silMtApo, silDurian, silSanPedro, unlockedMap, 1, 1.0f);
        ImageIO.write(hoverDM, "png", new File("scratch/hover_locked_museum_preview.png"));
    }

    private static BufferedImage renderMapScreen(int w, int h, Image bg, BufferedImage mtapo, Image frame,
                                                 BufferedImage blueBldg, BufferedImage durianMuseum, BufferedImage sanPedro,
                                                 BufferedImage silMtApo, BufferedImage silDurian, BufferedImage silSanPedro,
                                                 int maxUnlockedMap, int hoveredLandmark, float animT) {
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = out.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Sky
        g2.drawImage(bg, 0, 0, w, h, null);

        // 2. Map 4 at the back: Mt Apo
        boolean isMtApoUnlocked = maxUnlockedMap >= 4;
        BufferedImage apoImg = isMtApoUnlocked ? mtapo : silMtApo;
        float apoScale = (hoveredLandmark == 3 ? 1.03f : 1.0f);
        if (apoScale > 1.001f) {
            int sw = (int)(w * apoScale);
            int sh = (int)(h * apoScale);
            g2.drawImage(apoImg, (w - sw) / 2, (h - sh) / 2, sw, sh, null);
        } else {
            g2.drawImage(apoImg, 0, 0, w, h, null);
        }

        // Animation offsets:
        float maxOffset = h * 0.72f;
        float ease = (float)(1.0 - Math.pow(1.0 - animT, 3));
        int animOffset = (int)(maxOffset * (1.0f - ease));

        // 3. Map 1: Blue Building (Left)
        boolean isBbUnlocked = maxUnlockedMap >= 1;
        BufferedImage bbImg = blueBldg; // Blue building is always unlocked for map 1
        int bbH = (int)(h * 0.44);
        int bbW = (int)(bbH * (blueBldg.getWidth() / (double)blueBldg.getHeight()));
        int bbX = 24;
        int bbY = h - bbH - 18 + animOffset;
        drawBuilding(g2, bbImg, bbX, bbY, bbW, bbH, (hoveredLandmark == 0 ? 1.07f : 1.0f), isBbUnlocked);

        // 4. Map 2: Davao Museum (Center)
        boolean isDmUnlocked = maxUnlockedMap >= 2;
        BufferedImage dmImg = isDmUnlocked ? durianMuseum : silDurian;
        int dmW = (int)(w * 0.32);
        int dmH = (int)(h * 0.46);
        int dmX = (w - dmW) / 2 - 10;
        int dmY = h - dmH - 18 + animOffset;
        drawBuilding(g2, dmImg, dmX, dmY, dmW, dmH, (hoveredLandmark == 1 ? 1.07f : 1.0f), isDmUnlocked);

        // 5. Map 3: San Pedro (Right)
        boolean isSpUnlocked = maxUnlockedMap >= 3;
        BufferedImage spImg = isSpUnlocked ? sanPedro : silSanPedro;
        int spH = (int)(h * 0.48);
        int spW = (int)(spH * (sanPedro.getWidth() / (double)sanPedro.getHeight()));
        int spX = w - spW + 50;
        int spY = h - spH - 18 + animOffset;
        drawBuilding(g2, spImg, spX, spY, spW, spH, (hoveredLandmark == 2 ? 1.07f : 1.0f), isSpUnlocked);

        // 6. Indigenous Frame on top
        g2.drawImage(frame, 0, 0, w, h, null);

        // 7. Header bar & HUD badge
        drawHeaderAndBadge(g2, w, hoveredLandmark, maxUnlockedMap);

        g2.dispose();
        return out;
    }

    private static void drawBuilding(Graphics2D g2, BufferedImage img, int x, int y, int w, int h, float scale, boolean unlocked) {
        int drawW = (int)(w * scale);
        int drawH = (int)(h * scale);
        int drawX = x - (drawW - w) / 2;
        int drawY = y - (drawH - h);

        if (scale > 1.001f) {
            // Glow
            Graphics2D gGlow = (Graphics2D) g2.create();
            gGlow.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, unlocked ? 0.35f : 0.20f));
            gGlow.drawImage(img, drawX - 2, drawY - 2, drawW + 4, drawH + 4, null);
            gGlow.dispose();
        }

        g2.drawImage(img, drawX, drawY, drawW, drawH, null);

        // If locked, draw an elegant lock emblem on top of the silhouette
        if (!unlocked) {
            int lockSize = 36;
            int lockX = drawX + (drawW - lockSize) / 2;
            int lockY = drawY + (drawH - lockSize) / 2;

            Graphics2D gLock = (Graphics2D) g2.create();
            gLock.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            gLock.setColor(new Color(20, 25, 40, 210));
            gLock.fillOval(lockX - 4, lockY - 4, lockSize + 8, lockSize + 8);
            gLock.setColor(new Color(245, 170, 70));
            gLock.setStroke(new BasicStroke(2.0f));
            gLock.drawOval(lockX - 4, lockY - 4, lockSize + 8, lockSize + 8);
            gLock.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
            gLock.drawString("🔒", lockX + 6, lockY + 25);
            gLock.dispose();
        }
    }

    private static void drawHeaderAndBadge(Graphics2D g2, int w, int hovered, int maxUnlocked) {
        int barY = 22;
        // Back button on left
        g2.setColor(new Color(24, 32, 54, 210));
        g2.fillRoundRect(28, barY, 110, 36, 12, 12);
        g2.setColor(new Color(242, 190, 75));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(28, barY, 110, 36, 12, 12);
        g2.setFont(new Font("Arial", Font.BOLD, 13));
        g2.setColor(Color.WHITE);
        g2.drawString("← Balik (Back)", 38, barY + 23);

        // Center Title badge
        int titleW = 280;
        int titleX = (w - titleW) / 2;
        g2.setColor(new Color(24, 32, 54, 210));
        g2.fillRoundRect(titleX, barY, titleW, 36, 12, 12);
        g2.setColor(new Color(242, 190, 75));
        g2.drawRoundRect(titleX, barY, titleW, 36, 12, 12);
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.setColor(new Color(254, 240, 200));
        String title = "PILIA ANG MAPA  •  MAP SELECT";
        int tw = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (w - tw) / 2, barY + 23);

        if (hovered >= 0) {
            String bTitle;
            String bSub;
            Color bColor;
            if (hovered == 0) {
                bTitle = "📍 1. BLUE BUILDING  •  [ BUKAS / UNLOCKED ]";
                bSub = "Predefined Difficulty: Sayon (Easy) • I-klik aron makadula!";
                bColor = new Color(110, 230, 130);
            } else if (hovered == 1) {
                boolean unlocked = maxUnlocked >= 2;
                bTitle = (unlocked ? "📍 2. DAVAO MUSEUM  •  [ BUKAS ]" : "🔒 2. DAVAO MUSEUM  •  [ NAKA-LOCK ]");
                bSub = "Predefined Difficulty: Sakto (Medium) • " + (unlocked ? "I-klik aron makadula!" : "Pildaha una ang Blue Building!");
                bColor = unlocked ? new Color(110, 230, 130) : new Color(245, 170, 70);
            } else if (hovered == 2) {
                boolean unlocked = maxUnlocked >= 3;
                bTitle = (unlocked ? "📍 3. SAN PEDRO CATHEDRAL  •  [ BUKAS ]" : "🔒 3. SAN PEDRO CATHEDRAL  •  [ NAKA-LOCK ]");
                bSub = "Predefined Difficulty: Lisod (Hard) • " + (unlocked ? "I-klik aron makadula!" : "Pildaha una ang Davao Museum!");
                bColor = unlocked ? new Color(110, 230, 130) : new Color(245, 170, 70);
            } else {
                boolean unlocked = maxUnlocked >= 4;
                bTitle = (unlocked ? "📍 4. MT APO  •  [ BUKAS ]" : "🔒 4. MT APO  •  [ NAKA-LOCK ]");
                bSub = "Predefined Difficulty: Extreme • " + (unlocked ? "I-klik aron makadula!" : "Pildaha una ang San Pedro Cathedral!");
                bColor = unlocked ? new Color(160, 100, 255) : new Color(245, 170, 70);
            }

            int bw = 480;
            int bx = (w - bw) / 2;
            int by = 66;
            g2.setColor(new Color(15, 20, 36, 230));
            g2.fillRoundRect(bx, by, bw, 46, 14, 14);
            g2.setColor(bColor);
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawRoundRect(bx, by, bw, 46, 14, 14);

            g2.setFont(new Font("Arial", Font.BOLD, 13));
            g2.setColor(bColor);
            int btw = g2.getFontMetrics().stringWidth(bTitle);
            g2.drawString(bTitle, (w - btw) / 2, by + 20);

            g2.setFont(new Font("Arial", Font.PLAIN, 12));
            g2.setColor(new Color(225, 238, 255));
            int bsw = g2.getFontMetrics().stringWidth(bSub);
            g2.drawString(bSub, (w - bsw) / 2, by + 37);
        }
    }

    private static BufferedImage createSilhouette(BufferedImage src, Color color) {
        BufferedImage sil = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = sil.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.setComposite(AlphaComposite.SrcIn);
        g.setColor(color);
        g.fillRect(0, 0, src.getWidth(), src.getHeight());
        g.dispose();
        return sil;
    }
}
