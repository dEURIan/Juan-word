package juanword;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Clean, solid, flat game button that uses the official GamePalette
 * and renders consistently across all platforms (bypassing OS LookAndFeel overrides).
 */
public class FlatGameButton extends JButton {
    private Color normalBg;
    private Color hoverBg;
    private Color normalFg;
    private Color borderColor;
    private int cornerRadius = 14;
    private boolean isHovered = false;

    public FlatGameButton(String text, Color bg, Color fg, Color border) {
        super(text);
        this.normalBg = bg;
        this.hoverBg = (bg != null) ? bg.brighter() : null;
        this.normalFg = fg;
        this.borderColor = border;

        setFont(new Font("Arial", Font.BOLD, 14));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
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
                repaint();
            }
        });
    }

    public void setColors(Color bg, Color fg, Color border) {
        this.normalBg = bg;
        this.hoverBg = (bg != null) ? bg.brighter() : null;
        this.normalFg = fg;
        this.borderColor = border;
        repaint();
    }

    public void setBorderColor(Color border) {
        this.borderColor = border;
        repaint();
    }

    public void setCornerRadius(int r) {
        this.cornerRadius = r;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth() - 4;
        int h = getHeight() - 4;
        int x = 2;
        int y = 2;

        if (isEnabled()) {
            // Drop shadow
            g2.setColor(new Color(0, 0, 0, 35));
            g2.fillRoundRect(x + 1, y + 2, w, h, cornerRadius, cornerRadius);

            // Background
            g2.setColor(isHovered ? hoverBg : normalBg);
            g2.fillRoundRect(x, y, w, h, cornerRadius, cornerRadius);

            // Border
            if (borderColor != null) {
                g2.setColor(isHovered ? borderColor.brighter() : borderColor);
                g2.setStroke(new BasicStroke(isHovered ? 2.0f : 1.2f));
                g2.drawRoundRect(x, y, w, h, cornerRadius, cornerRadius);
            }

            // Foreground text
            g2.setFont(getFont());
            g2.setColor(normalFg);
        } else {
            // Disabled appearance
            g2.setColor(GamePalette.DUSK_NAVY);
            g2.fillRoundRect(x, y, w, h, cornerRadius, cornerRadius);

            g2.setColor(GamePalette.DEEP_INDIGO);
            g2.setStroke(new BasicStroke(1.0f));
            g2.drawRoundRect(x, y, w, h, cornerRadius, cornerRadius);

            g2.setFont(getFont());
            g2.setColor(new Color(GamePalette.SKY.getRed(), GamePalette.SKY.getGreen(), GamePalette.SKY.getBlue(), 100));
        }

        String text = getText();
        if (text != null && !text.isEmpty()) {
            FontMetrics fm = g2.getFontMetrics();
            int tx = x + (w - fm.stringWidth(text)) / 2;
            int ty = y + (h + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(text, tx, ty);
        }

        g2.dispose();
    }
}
