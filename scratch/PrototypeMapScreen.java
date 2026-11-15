import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class PrototypeMapScreen {
    public static void main(String[] args) throws Exception {
        BufferedImage bg = ImageIO.read(new File("bg.png"));
        BufferedImage mtapo = ImageIO.read(new File("mtapo.png"));
        BufferedImage frame = ImageIO.read(new File("indigframe.png"));
        BufferedImage sanpedro = ImageIO.read(new File("sanpedro.png"));
        BufferedImage clocktower = ImageIO.read(new File("clocktower.png"));
        BufferedImage durianmuseum = ImageIO.read(new File("durianmuseum.png"));

        // Tight crops of clock tower and durian museum
        BufferedImage ctTight = clocktower.getSubimage(269, 96, 616, 1744);
        BufferedImage dmTight = durianmuseum.getSubimage(0, 300, 1080, 1620);
        BufferedImage spTight = sanpedro.getSubimage(0, 201, 1612, 865);

        int w = 960;
        int h = 540;

        // Render resting frame (t=1.0)
        BufferedImage imgRest = renderScene(w, h, bg, mtapo, frame, spTight, ctTight, dmTight, 1.0f, -1);
        ImageIO.write(imgRest, "png", new File("scratch/prototype_map_resting.png"));

        // Render hover on San Pedro
        BufferedImage imgHoverSP = renderScene(w, h, bg, mtapo, frame, spTight, ctTight, dmTight, 1.0f, 0);
        ImageIO.write(imgHoverSP, "png", new File("scratch/prototype_map_hover_sp.png"));

        // Render hover on Clock Tower
        BufferedImage imgHoverCT = renderScene(w, h, bg, mtapo, frame, spTight, ctTight, dmTight, 1.0f, 1);
        ImageIO.write(imgHoverCT, "png", new File("scratch/prototype_map_hover_ct.png"));

        // Render hover on Durian Museum
        BufferedImage imgHoverDM = renderScene(w, h, bg, mtapo, frame, spTight, ctTight, dmTight, 1.0f, 2);
        ImageIO.write(imgHoverDM, "png", new File("scratch/prototype_map_hover_dm.png"));

        // Render animation mid-point (t=0.5)
        BufferedImage imgAnimMid = renderScene(w, h, bg, mtapo, frame, spTight, ctTight, dmTight, 0.5f, -1);
        ImageIO.write(imgAnimMid, "png", new File("scratch/prototype_map_anim_mid.png"));

        System.out.println("Rendered all prototype states successfully!");
    }

    public static BufferedImage renderScene(int w, int h, Image bg, Image mtapo, Image frame,
                                            BufferedImage spTight, BufferedImage ctTight, BufferedImage dmTight,
                                            float animT, int hoveredIndex) {
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = out.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Sky & Mt Apo
        g2.drawImage(bg, 0, 0, w, h, null);
        g2.drawImage(mtapo, 0, 0, w, h, null);

        // Animation offsets: slide up from below screen (e.g. initial offset +350px)
        float maxOffset = 380f;
        // Cubic ease out
        float ease = (float)(1.0 - Math.pow(1.0 - animT, 3));
        int animYOffset = (int)(maxOffset * (1.0f - ease));

        // Landmark 0: San Pedro (Left)
        // Resting target:
        int spH = (int)(h * 0.54);
        int spW = (int)(spH * (spTight.getWidth() / (double)spTight.getHeight()));
        int spX = 18;
        int spY = h - spH - 18 + animYOffset;
        drawLandmark(g2, spTight, spX, spY, spW, spH, (hoveredIndex == 0 ? 1.07f : 1.0f));

        // Landmark 2: Durian Museum (Right)
        int dmW = (int)(w * 0.35);
        int dmH = (int)(h * 0.50);
        int dmX = w - dmW - 18;
        int dmY = h - dmH - 18 + animYOffset;
        drawLandmark(g2, dmTight, dmX, dmY, dmW, dmH, (hoveredIndex == 2 ? 1.07f : 1.0f));

        // Landmark 1: Clock Tower (Center, in front of San Pedro and Mt Apo)
        int ctH = (int)(h * 0.76);
        int ctW = (int)(ctH * (ctTight.getWidth() / (double)ctTight.getHeight()));
        int ctX = (w - ctW) / 2;
        int ctY = h - ctH - 18 + animYOffset;
        drawLandmark(g2, ctTight, ctX, ctY, ctW, ctH, (hoveredIndex == 1 ? 1.07f : 1.0f));

        // 4. Indigenous Frame on top
        g2.drawImage(frame, 0, 0, w, h, null);

        // 5. Header UI Bar (Pill with Title and Balik button)
        drawHeaderBar(g2, w, hoveredIndex);

        g2.dispose();
        return out;
    }

    private static void drawLandmark(Graphics2D g2, BufferedImage img, int x, int y, int w, int h, float scale) {
        if (scale > 1.001f) {
            // Anchor scale to bottom center
            int scaledW = (int)(w * scale);
            int scaledH = (int)(h * scale);
            int drawX = x - (scaledW - w) / 2;
            int drawY = y - (scaledH - h); // grows upward!

            // Subtle golden highlight bloom on hover
            Graphics2D gGlow = (Graphics2D) g2.create();
            gGlow.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
            // Slight offset shadow/glow
            gGlow.drawImage(img, drawX, drawY, scaledW, scaledH, null);
            gGlow.dispose();

            g2.drawImage(img, drawX, drawY, scaledW, scaledH, null);
        } else {
            g2.drawImage(img, x, y, w, h, null);
        }
    }

    private static void drawHeaderBar(Graphics2D g2, int w, int hoveredIndex) {
        // Draw top navigation bar
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
        int titleW = 260;
        int titleX = (w - titleW) / 2;
        g2.setColor(new Color(24, 32, 54, 210));
        g2.fillRoundRect(titleX, barY, titleW, 36, 12, 12);
        g2.setColor(new Color(242, 190, 75));
        g2.drawRoundRect(titleX, barY, titleW, 36, 12, 12);
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.setColor(new Color(254, 240, 200));
        String title = "PILIA ANG MAPA (MAP SELECT)";
        int tw = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (w - tw) / 2, barY + 23);

        // Hover badge for hovered landmark
        if (hoveredIndex >= 0) {
            String badgeTitle = "";
            String badgeSub = "";
            Color badgeColor = Color.WHITE;
            if (hoveredIndex == 0) {
                badgeTitle = "📍 San Pedro Cathedral";
                badgeSub = "Katung daan nga simbahan • [ BUKAS / UNLOCKED ]";
                badgeColor = new Color(110, 230, 130);
            } else if (hoveredIndex == 1) {
                badgeTitle = "🔒 Davao City Clock Tower";
                badgeSub = "Tungod sa City Hall • [ NAKA-LOCK ]";
                badgeColor = new Color(245, 170, 70);
            } else if (hoveredIndex == 2) {
                badgeTitle = "🔒 Davao City Museum";
                badgeSub = "Durian Architectural Design • [ NAKA-LOCK ]";
                badgeColor = new Color(245, 170, 70);
            }

            int bw = 380;
            int bx = (w - bw) / 2;
            int by = 68;
            g2.setColor(new Color(15, 20, 35, 220));
            g2.fillRoundRect(bx, by, bw, 46, 14, 14);
            g2.setColor(badgeColor);
            g2.setStroke(new BasicStroke(1.8f));
            g2.drawRoundRect(bx, by, bw, 46, 14, 14);

            g2.setFont(new Font("Arial", Font.BOLD, 14));
            g2.setColor(badgeColor);
            int btw = g2.getFontMetrics().stringWidth(badgeTitle);
            g2.drawString(badgeTitle, (w - btw) / 2, by + 20);

            g2.setFont(new Font("Arial", Font.PLAIN, 12));
            g2.setColor(new Color(230, 240, 255));
            int bsw = g2.getFontMetrics().stringWidth(badgeSub);
            g2.drawString(badgeSub, (w - bsw) / 2, by + 37);
        }
    }
}
