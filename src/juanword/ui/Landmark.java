package juanword.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public final class Landmark {
    private final int mapIndex;
    private final String name;
    private Rectangle bounds;
    private final BufferedImage sprite;
    private BufferedImage silhouette;
    private float scale = 1.0f;
    private boolean locked;

    public Landmark(int mapIndex, String name, String spriteFile, Rectangle bounds) {
        this.mapIndex = mapIndex;
        this.name = name != null ? name : "Landmark " + mapIndex;
        this.bounds = bounds != null ? new Rectangle(bounds) : new Rectangle(0, 0, 100, 100);
        BufferedImage raw = ImageLoader.load(spriteFile);
        if (raw != null) {
            if (mapIndex == 1) {
                int cw = Math.min(648, raw.getWidth());
                int ch = Math.min(512, raw.getHeight());
                this.sprite = raw.getSubimage(0, 0, cw, ch);
            } else if (mapIndex == 2) {
                int subY = Math.min(300, raw.getHeight() - 1);
                int subH = Math.min(1620, raw.getHeight() - subY);
                this.sprite = raw.getSubimage(0, subY, raw.getWidth(), subH);
            } else if (mapIndex == 3) {
                int subY = Math.min(201, raw.getHeight() - 1);
                int subH = Math.min(865, raw.getHeight() - subY);
                int subW = Math.min(1612, raw.getWidth());
                this.sprite = raw.getSubimage(0, subY, subW, subH);
            } else {
                this.sprite = raw;
            }
            this.silhouette = ImageLoader.createSilhouette(this.sprite, new Color(14, 18, 28, 252));
        } else {
            this.sprite = null;
            this.silhouette = null;
        }
    }

    public boolean contains(Point p) {
        if (bounds == null || p == null) return false;
        if (mapIndex == 4) {
            int top = (int) (bounds.height * 0.08);
            int bottom = (int) (bounds.height * 0.72);
            if (p.y < top || p.y > bottom || p.x < 0 || p.x > bounds.width) {
                return false;
            }
            if (sprite != null && bounds.width > 0 && bounds.height > 0) {
                int imgX = (int) ((double) p.x / bounds.width * sprite.getWidth());
                int imgY = (int) ((double) p.y / bounds.height * sprite.getHeight());
                if (imgX >= 0 && imgX < sprite.getWidth() && imgY >= 0 && imgY < sprite.getHeight()) {
                    int pixel = sprite.getRGB(imgX, imgY);
                    int alpha = (pixel >> 24) & 0xff;
                    return alpha > 30;
                }
            }
            return true;
        }
        return bounds.contains(p);
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
        if (this.locked && this.silhouette == null && this.sprite != null) {
            this.silhouette = ImageLoader.createSilhouette(this.sprite, new Color(18, 20, 28, 225));
        }
    }

    /** Eases toward the hover scale; returns true while the scale is still changing. */
    public boolean updateScale(boolean hovered) {
        float target = hovered ? 1.06f : 1.0f;
        if (scale == target) return false;
        scale += (target - scale) * 0.2f;
        if (Math.abs(target - scale) < 0.001f) scale = target;
        return true;
    }

    public void paint(Graphics2D g2) {
        if (bounds == null) return;
        BufferedImage img = (locked && silhouette != null) ? silhouette : sprite;
        if (img == null) return;

        Object oldHint = g2.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int bw = bounds.width;
        int bh = bounds.height;

        if (mapIndex == 4) {
            int sw = (int) (bw * scale);
            int sh = (int) (bh * scale);
            int sx = bounds.x + (bw - sw) / 2;
            int sy = bounds.y + (bh - sh) / 2;
            g2.drawImage(img, sx, sy, sw, sh, null);
        } else {
            // Preserve native aspect ratio so icons are never stretched or distorted
            int imgW = img.getWidth();
            int imgH = img.getHeight();
            double scaleRatio = Math.min((double) bw / imgW, (double) bh / imgH);
            int drawW = Math.max(1, (int) Math.round(imgW * scaleRatio * scale));
            int drawH = Math.max(1, (int) Math.round(imgH * scaleRatio * scale));
            int drawX = bounds.x + (bw - drawW) / 2;
            int drawY = bounds.y + (bh - drawH); // Anchor to bottom of bounds (ground baseline)

            g2.drawImage(img, drawX, drawY, drawW, drawH, null);
        }

        if (locked && mapIndex != 4) {
            // Draw clean padlock emblem
            int cx = bounds.x + bw / 2;
            int cy = bounds.y + bh / 2;

            // Semi-translucent dark pill behind lock
            g2.setColor(new Color(0, 0, 0, 140));
            g2.fillRoundRect(cx - 16, cy - 16, 32, 32, 10, 10);

            // Shackle (Silver-white)
            g2.setColor(new Color(230, 235, 245));
            g2.setStroke(new BasicStroke(2.8f));
            g2.drawArc(cx - 6, cy - 11, 12, 14, 0, 180);

            // Body (Gold / Brass)
            g2.setColor(new Color(235, 185, 50));
            g2.fillRoundRect(cx - 9, cy - 2, 18, 15, 3, 3);
            g2.setColor(new Color(175, 125, 20));
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawRoundRect(cx - 9, cy - 2, 18, 15, 3, 3);

            // Keyhole
            g2.setColor(new Color(40, 30, 20));
            g2.fillOval(cx - 2, cy + 2, 4, 4);
            g2.fillRect(cx - 1, cy + 4, 2, 4);
        }

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldHint != null ? oldHint : RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    }

    public int getMapIndex() { return mapIndex; }
    public String getName() { return name; }
    public Rectangle getBounds() { return bounds; }
    public void setBounds(Rectangle r) { this.bounds = r; }
    public boolean isLocked() { return locked; }
    public float getScale() { return scale; }
}
