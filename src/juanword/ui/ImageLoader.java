package juanword.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;

public final class ImageLoader {
    private static final Map<String, BufferedImage> CACHE = new ConcurrentHashMap<>();

    private ImageLoader() {}

    public static BufferedImage load(String path) {
        if (path == null || path.isBlank()) {
            return createPlaceholder("Image");
        }
        BufferedImage cached = CACHE.get(path);
        if (cached != null) {
            return cached;
        }

        String[] searchPaths = new String[] {
            path,
            "assets/" + path,
            "assets/sprites/" + path,
            "assets/maps/" + path,
            "assets/ui/" + path,
            "assets/letters/" + path,
            new File(path).getName(),
            "assets/sprites/" + new File(path).getName(),
            "assets/maps/" + new File(path).getName(),
            "assets/ui/" + new File(path).getName(),
            "assets/letters/" + new File(path).getName()
        };

        for (String p : searchPaths) {
            File f = new File(p);
            if (f.exists() && f.isFile()) {
                try {
                    BufferedImage img = ImageIO.read(f);
                    if (img != null) {
                        CACHE.put(path, img);
                        return img;
                    }
                } catch (Exception ignored) {}
            }
        }

        // Try classpath resource
        String resPath = path.startsWith("/") ? path : "/" + path;
        try (InputStream in = ImageLoader.class.getResourceAsStream(resPath)) {
            if (in != null) {
                BufferedImage img = ImageIO.read(in);
                if (img != null) {
                    CACHE.put(path, img);
                    return img;
                }
            }
        } catch (Exception ignored) {}

        String altRes = "/assets/" + (path.startsWith("/") ? path.substring(1) : path);
        try (InputStream in = ImageLoader.class.getResourceAsStream(altRes)) {
            if (in != null) {
                BufferedImage img = ImageIO.read(in);
                if (img != null) {
                    CACHE.put(path, img);
                    return img;
                }
            }
        } catch (Exception ignored) {}

        BufferedImage placeholder = createPlaceholder(new File(path).getName());
        CACHE.put(path, placeholder);
        return placeholder;
    }

    public static BufferedImage loadLetterTile(char letter) {
        char lower = Character.toLowerCase(letter);
        char upper = Character.toUpperCase(letter);
        BufferedImage img = load("letters/" + lower + ".png");
        if (img == null || isPlaceholder(img)) {
            img = load("letters/" + upper + ".png");
        }
        if (img == null || isPlaceholder(img)) {
            int num = (upper - 'A') + 1;
            img = load("letters/" + num + ".png");
        }
        return (img != null && !isPlaceholder(img)) ? img : null;
    }

    public static boolean isPlaceholder(BufferedImage img) {
        return img != null && img.getWidth() == 120 && img.getHeight() == 120;
    }

    public static BufferedImage createSilhouette(BufferedImage src, Color tint) {
        if (src == null) return null;
        BufferedImage silhouette = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = silhouette.createGraphics();
        g2.drawImage(src, 0, 0, null);
        g2.setComposite(AlphaComposite.SrcIn);
        g2.setColor(tint != null ? tint : new Color(18, 20, 28, 225));
        g2.fillRect(0, 0, src.getWidth(), src.getHeight());
        g2.dispose();
        return silhouette;
    }

    private static final Map<BufferedImage, Map<Integer, BufferedImage>> SCALED = new ConcurrentHashMap<>();

    /** Returns a cached, smoothly downscaled copy so per-frame painting is a cheap 1:1 blit. */
    public static BufferedImage scaledCached(BufferedImage src, int w, int h) {
        if (src == null || w <= 0 || h <= 0 || (src.getWidth() == w && src.getHeight() == h)) return src;
        Map<Integer, BufferedImage> bySize = SCALED.computeIfAbsent(src, k -> new ConcurrentHashMap<>());
        if (bySize.size() > 12) bySize.clear();
        return bySize.computeIfAbsent(w * 65536 + h, k -> {
            BufferedImage cur = src;
            int cw = src.getWidth();
            int ch = src.getHeight();
            while (cw / 2 >= w && ch / 2 >= h) {
                cw /= 2;
                ch /= 2;
                cur = scale(cur, cw, ch);
            }
            return scale(cur, w, h);
        });
    }

    public static BufferedImage scale(BufferedImage src, int w, int h) {
        if (src == null || w <= 0 || h <= 0) return src;
        BufferedImage scaled = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scaled.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.drawImage(src, 0, 0, w, h, null);
        g2.dispose();
        return scaled;
    }

    private static BufferedImage createPlaceholder(String label) {
        int w = 120;
        int h = 120;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(40, 50, 70, 200));
        g2.fillRoundRect(4, 4, w - 8, h - 8, 16, 16);
        g2.setColor(new Color(200, 210, 230));
        g2.drawRoundRect(4, 4, w - 8, h - 8, 16, 16);
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g2.drawString(label != null && label.length() > 14 ? label.substring(0, 14) : (label != null ? label : "?"), 10, h / 2);
        g2.dispose();
        return img;
    }
}
