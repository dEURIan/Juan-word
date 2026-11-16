package scratch;

import juanword.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

public class TestRobustLayoutScaling {
    public static void main(String[] args) throws Exception {
        int[][] res = {
            {960, 540},
            {1024, 665},
            {1024, 768},
            {1280, 720},
            {1366, 768},
            {1600, 900},
            {1920, 1080},
            {2560, 1440}
        };
        
        double bbAspect = 648.0 / 512.0;
        double dmAspect = 1080.0 / 1620.0;
        double spAspect = 1612.0 / 865.0;
        
        boolean allPass = true;
        for (int[] r : res) {
            int w = r[0];
            int h = r[1];
            
            float S = Math.min((float) w / 960f, (float) h / 540f);
            int stageW = Math.min(w, (int) (h * 16.0 / 9.0));
            int stageOffset = (w - stageW) / 2;
            float refH = 540f * S;
            
            int leftMargin = stageOffset + (int) (32 * S);
            int rightMargin = stageOffset + stageW - (int) (32 * S);
            
            int bbH = (int) (refH * 0.40);
            int bbW = (int) (bbH * bbAspect);
            int bbX = leftMargin;
            int bbY = h - bbH - (int) (18 * S);
            
            int dmH = (int) (refH * 0.45);
            int dmW = (int) (dmH * dmAspect);
            
            int spH = (int) (refH * 0.36);
            int spW = (int) (spH * spAspect);
            int spX = rightMargin - spW;
            int spY = h - spH - (int) (18 * S);
            
            int availWidth = rightMargin - leftMargin;
            int totalLandmarkWidth = bbW + dmW + spW;
            int gap = Math.max((int) (12 * S), (availWidth - totalLandmarkWidth) / 2);
            int dmX = bbX + bbW + gap;
            int dmY = h - dmH - (int) (18 * S);
            
            int gap1 = dmX - (bbX + bbW);
            int gap2 = spX - (dmX + dmW);
            
            System.out.printf("[%dx%d] S=%.3f | BB[%d..%d, w=%d] --(gap=%dpx)--> DM[%d..%d, w=%d] --(gap=%dpx)--> SP[%d..%d, w=%d] | R-Margin=%dpx\n",
                w, h, S, bbX, bbX + bbW, bbW, gap1, dmX, dmX + dmW, dmW, gap2, spX, spX + spW, spW, w - (spX + spW));
            
            if (gap1 <= 0 || gap2 <= 0) {
                System.err.println("FAIL: Overlap detected at " + w + "x" + h);
                allPass = false;
            }
        }
        
        if (allPass) {
            System.out.println("\n✓ SUCCESS: Zero overlap across all tested resolutions!");
        } else {
            System.exit(1);
        }
        System.exit(0);
    }
}
