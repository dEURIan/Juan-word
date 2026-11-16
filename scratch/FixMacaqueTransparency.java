import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class FixMacaqueTransparency {
    public static void main(String[] args) throws Exception {
        BufferedImage img = ImageIO.read(new File("assets/sprites/maacaque.png"));
        int w = img.getWidth();
        int h = img.getHeight();
        System.out.println("Macaque size: " + w + "x" + h);
        int cornerRGB = img.getRGB(0, 0);
        int r = (cornerRGB >> 16) & 0xff;
        int g = (cornerRGB >> 8) & 0xff;
        int b = cornerRGB & 0xff;
        int a = (cornerRGB >> 24) & 0xff;
        System.out.printf("Corner pixel at (0,0): R=%d, G=%d, B=%d, A=%d%n", r, g, b, a);

        if (r > 240 && g > 240 && b > 240) {
            System.out.println("Solid white/near-white background detected! Making white background transparent via flood-fill/threshold...");
            BufferedImage transparent = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            // Flood-fill from borders or flood-fill outside
            boolean[][] visited = new boolean[w][h];
            java.util.Queue<java.awt.Point> queue = new java.util.ArrayDeque<>();

            for (int x = 0; x < w; x++) {
                queue.add(new java.awt.Point(x, 0));
                queue.add(new java.awt.Point(x, h - 1));
            }
            for (int y = 0; y < h; y++) {
                queue.add(new java.awt.Point(0, y));
                queue.add(new java.awt.Point(w - 1, y));
            }

            while (!queue.isEmpty()) {
                java.awt.Point p = queue.poll();
                if (p.x < 0 || p.x >= w || p.y < 0 || p.y >= h || visited[p.x][p.y]) continue;
                visited[p.x][p.y] = true;
                int pixel = img.getRGB(p.x, p.y);
                int pr = (pixel >> 16) & 0xff;
                int pg = (pixel >> 8) & 0xff;
                int pb = pixel & 0xff;
                if (pr > 235 && pg > 235 && pb > 235) {
                    // It is background
                    queue.add(new java.awt.Point(p.x + 1, p.y));
                    queue.add(new java.awt.Point(p.x - 1, p.y));
                    queue.add(new java.awt.Point(p.x, p.y + 1));
                    queue.add(new java.awt.Point(p.x, p.y - 1));
                }
            }

            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    if (visited[x][y]) {
                        transparent.setRGB(x, y, 0x00000000); // transparent
                    } else {
                        transparent.setRGB(x, y, img.getRGB(x, y));
                    }
                }
            }

            ImageIO.write(transparent, "PNG", new File("assets/sprites/maacaque.png"));
            ImageIO.write(transparent, "PNG", new File("assets/sprites/monkey.png"));
            ImageIO.write(transparent, "PNG", new File("assets/maacaque.png"));
            ImageIO.write(transparent, "PNG", new File("assets/monkey.png"));
            System.out.println("Saved transparent monkey sprite!");
        }
    }
}
