package scratch;

import juanword.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class TestLayoutOptions {
    public static void main(String[] args) throws Exception {
        JuanWord app = new JuanWord();
        MapSelectPanel panel = new MapSelectPanel(app);
        
        // Let's test different height/width formulas for (960, 540)
        int w = 960;
        int h = 540;
        float S = 1.0f;
        int stageW = 960;
        int stageOffset = 0;
        
        // Sprite aspect ratios:
        // Blue Building: 648 / 512 = 1.2656
        double bbAspect = 648.0 / 512.0;
        // Davao Museum: 1080 / 1620 = 0.6667
        double dmAspect = 1080.0 / 1620.0;
        // San Pedro Cathedral: 1612 / 865 = 1.8636
        double spAspect = 1612.0 / 865.0;
        
        // Variation A:
        // bbH = 0.40 * h (216), dmH = 0.46 * h (248), spH = 0.38 * h (205)
        testVariation("Variation A (Natural Aspects)", w, h, bbAspect, dmAspect, spAspect, 0.40, 0.46, 0.38);
        
        // Variation B: Slightly more compact San Pedro:
        // bbH = 0.40 * h (216), dmH = 0.48 * h (259), spH = 0.36 * h (194)
        testVariation("Variation B (Taller DM, Sleeker SP)", w, h, bbAspect, dmAspect, spAspect, 0.40, 0.48, 0.36);
        
        // Variation C: Balanced Trio:
        // bbH = 0.39 * h (210), dmH = 0.45 * h (243), spH = 0.35 * h (189)
        testVariation("Variation C (Balanced Trio)", w, h, bbAspect, dmAspect, spAspect, 0.39, 0.45, 0.35);
        
        System.exit(0);
    }
    
    private static void testVariation(String name, int w, int h, double bbAspect, double dmAspect, double spAspect, double bbHFrac, double dmHFrac, double spHFrac) {
        int bbH = (int) (h * bbHFrac);
        int bbW = (int) (bbH * bbAspect);
        
        int dmH = (int) (h * dmHFrac);
        int dmW = (int) (dmH * dmAspect);
        
        int spH = (int) (h * spHFrac);
        int spW = (int) (spH * spAspect);
        
        int leftMargin = 32;
        int rightMargin = 32;
        int bbX = leftMargin;
        
        // Center DM in the middle of the screen
        int dmX = (w - dmW) / 2;
        
        // Place SP on the right
        int spX = w - rightMargin - spW;
        
        int gap1 = dmX - (bbX + bbW);
        int gap2 = spX - (dmX + dmW);
        
        System.out.printf("--- %s ---\n", name);
        System.out.printf("  BB: x=%d..%d (w=%d, h=%d)\n", bbX, bbX + bbW, bbW, bbH);
        System.out.printf("  DM: x=%d..%d (w=%d, h=%d)\n", dmX, dmX + dmW, dmW, dmH);
        System.out.printf("  SP: x=%d..%d (w=%d, h=%d)\n", spX, spX + spW, spW, spH);
        System.out.printf("  Gap 1 (BB -> DM): %d px (%s)\n", gap1, gap1 > 0 ? "NO OVERLAP" : "OVERLAP!");
        System.out.printf("  Gap 2 (DM -> SP): %d px (%s)\n", gap2, gap2 > 0 ? "NO OVERLAP" : "OVERLAP!");
        System.out.printf("  Right margin: %d px\n", w - (spX + spW));
    }
}
