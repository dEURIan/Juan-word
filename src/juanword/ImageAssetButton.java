package juanword;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

/**
 * High-performance graphical asset button for JuanWord.
 * Renders custom artist-crafted PNG button sprites with:
 * - High-DPI bilinear interpolation
 * - Tactile 2px press-down animation on click
 * - Smooth hover brightness enhancement
 * - High-visibility 38% translucent disabled state
 */
public class ImageAssetButton extends JButton {
    private Image buttonImage;
    private String currentImagePath;
    private boolean isHovered = false;
    private boolean isPressed = false;
    private int targetWidth = 192;
    private int targetHeight = 48;

    public ImageAssetButton(String imagePath) {
        this(imagePath, 192, 48);
    }

    public ImageAssetButton(String imagePath, int width, int height) {
        this.targetWidth = width;
        this.targetHeight = height;
        setImage(imagePath);

        Dimension size = new Dimension(width, height);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        setSize(size);

        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    isHovered = true;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                isPressed = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled() && SwingUtilities.isLeftMouseButton(e)) {
                    isPressed = true;
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (isPressed) {
                    isPressed = false;
                    repaint();
                }
            }
        });
    }

    public void setTargetSize(int width, int height) {
        if (this.targetWidth == width && this.targetHeight == height) return;
        this.targetWidth = width;
        this.targetHeight = height;
        Dimension size = new Dimension(width, height);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        setSize(size);
        revalidate();
        repaint();
    }

    public int getTargetWidth() {
        return targetWidth;
    }

    public int getTargetHeight() {
        return targetHeight;
    }

    public void setImage(String imagePath) {
        if (imagePath == null) return;
        this.currentImagePath = imagePath;
        this.buttonImage = loadGameImage(imagePath);
        repaint();
    }

    public String getCurrentImagePath() {
        return currentImagePath;
    }

    private Image loadGameImage(String path) {
        // 1. Direct file lookup
        File f = new File(path);
        if (f.exists()) {
            return new ImageIcon(f.getAbsolutePath()).getImage();
        }
        // 2. Lookup in src/juanword
        File fSrc = new File("src/juanword", path);
        if (fSrc.exists()) {
            return new ImageIcon(fSrc.getAbsolutePath()).getImage();
        }
        // 3. ClassLoader resource lookup (for JAR distribution)
        try {
            java.net.URL url = getClass().getResource("/juanword/" + path);
            if (url == null) url = getClass().getResource("/" + path);
            if (url == null) url = getClass().getResource(path);
            if (url != null) {
                return new ImageIcon(url).getImage();
            }
        } catch (Exception ignored) {}

        // 4. Fallback
        return new ImageIcon(path).getImage();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth();
        int h = getHeight();

        int drawW = targetWidth > 0 ? Math.min(w, targetWidth) : w;
        int drawH = targetHeight > 0 ? Math.min(h, targetHeight) : h;
        if (drawW <= 0) drawW = w;
        if (drawH <= 0) drawH = h;

        int drawX = (w - drawW) / 2;
        int drawY = (h - drawH) / 2;

        if (isPressed && isEnabled()) {
            // Tactile Physical Button Press: Shift down by 2px on click
            drawY += 2;
        }

        if (!isEnabled()) {
            // Disabled State: Smoothly dimmed with 38% opacity for clean game UI look
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.38f));
            if (buttonImage != null) {
                g2.drawImage(buttonImage, drawX, drawY, drawW, drawH, this);
            }
        } else if (isHovered) {
            // Hover State: Draw button sprite + subtle golden-white highlight overlay
            if (buttonImage != null) {
                g2.drawImage(buttonImage, drawX, drawY, drawW, drawH, this);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.14f));
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(drawX, drawY, drawW, drawH, 28, 28);
            }
        } else {
            // Normal Default State
            if (buttonImage != null) {
                g2.drawImage(buttonImage, drawX, drawY, drawW, drawH, this);
            }
        }

        g2.dispose();
    }
}
