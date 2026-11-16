package scratch;

import juanword.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class RenderNewLayoutPreview {
    public static void main(String[] args) throws Exception {
        JuanWord app = new JuanWord();
        
        BufferedImage skyBg = ImageIO.read(new File("assets/maps/bg.png"));
        BufferedImage mtApo = ImageIO.read(new File("assets/maps/mtapo.png"));
        BufferedImage indigFrame = ImageIO.read(new File("assets/maps/indigframe.png"));
        
        BufferedImage bbRaw = ImageIO.read(new File("assets/maps/bluebuilding.png"));
        BufferedImage bbImg = bbRaw.getSubimage(0, 0, Math.min(648, bbRaw.getWidth()), Math.min(512, bbRaw.getHeight()));
        
        BufferedImage dmRaw = ImageIO.read(new File("assets/maps/durianmuseum.png"));
        BufferedImage dmImg = dmRaw.getSubimage(0, 300, dmRaw.getWidth(), Math.min(1620, dmRaw.getHeight() - 300));
        
        BufferedImage spRaw = ImageIO.read(new File("assets/maps/sanpedro.png"));
        BufferedImage spImg = spRaw.getSubimage(0, 201, Math.min(1612, spRaw.getWidth()), Math.min(865, spRaw.getHeight() - 201));
        
        int[] widths = {960, 1280, 1920};
        int[] heights = {540, 720, 1080};
        
        for (int i = 0; i < widths.length; i++) {
            int w = widths[i];
            int h = heights[i];
            float S = Math.min((float) w / 960f, (float) h / 540f);
            int stageW = Math.min(w, (int) (h * 16.0 / 9.0));
            int stageOffset = (w - stageW) / 2;
            
            BufferedImage canvas = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = canvas.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            
            // 1. Sky & Mt Apo
            g2.drawImage(skyBg, 0, 0, w, h, null);
            g2.drawImage(mtApo, 0, 0, w, h, null);
            
            // Building dimensions
            int bbH = (int) (h * 0.40);
            int bbW = (int) (bbH * (bbImg.getWidth() / (double) bbImg.getHeight()));
            
            int dmH = (int) (h * 0.45);
            int dmW = (int) (dmH * (dmImg.getWidth() / (double) dmImg.getHeight()));
            
            int spH = (int) (h * 0.36);
            int spW = (int) (spH * (spImg.getWidth() / (double) spImg.getHeight()));
            
            int leftMargin = stageOffset + (int) (32 * S);
            int rightMargin = stageOffset + stageW - (int) (32 * S);
            int availW = rightMargin - leftMargin;
            int totalBldW = bbW + dmW + spW;
            int gap = Math.max((int) (12 * S), (availW - totalBldW) / 2);
            
            int bbX = leftMargin;
            int bbY = h - bbH - (int) (18 * S);
            
            int dmX = bbX + bbW + gap;
            int dmY = h - dmH - (int) (18 * S);
            
            int spX = rightMargin - spW;
            int spY = h - spH - (int) (18 * S);
            
            // Draw buildings
            g2.drawImage(bbImg, bbX, bbY, bbW, bbH, null);
            g2.drawImage(dmImg, dmX, dmY, dmW, dmH, null);
            g2.drawImage(spImg, spX, spY, spW, spH, null);
            
            // Indigenous frame
            g2.drawImage(indigFrame, 0, 0, w, h, null);
            
            // Draw debug measurements
            int actualGap1 = dmX - (bbX + bbW);
            int actualGap2 = spX - (dmX + dmW);
            
            System.out.printf("Resolution %dx%d: bbW=%d, dmW=%d, spW=%d | Gap1(BB->DM)=%d px, Gap2(DM->SP)=%d px\n",
                w, h, bbW, dmW, spW, actualGap1, actualGap2);
            
            g2.dispose();
            
            ImageIO.write(canvas, "png", new File("scratch/new_layout_" + w + "x" + h + ".png"));
            if (i == 0) {
                ImageIO.write(canvas, "png", new File("/Users/euri/.gemini/antigravity/brain/5deb8e07-6ce9-4ceb-b4ca-64220e91baa5/new_map_layout_preview.png"));
            }
        }
        System.exit(0);
    }
}
