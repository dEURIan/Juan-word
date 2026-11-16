package scratch;

public class CalculateMaxScale {
    public static void main(String[] args) {
        int w = 960;
        int h = 540;
        int leftMargin = 32;
        int rightMargin = w - 32;
        int availWidth = rightMargin - leftMargin; // 896 px
        
        double bbAspect = 648.0 / 512.0;
        double dmAspect = 1080.0 / 1620.0;
        double spAspect = 1612.0 / 865.0;
        
        // Base fractions:
        double baseBbFrac = 0.40;
        double baseDmFrac = 0.45;
        double baseSpFrac = 0.36;
        
        double currentTotalW = (540 * baseBbFrac * bbAspect)
                             + (540 * baseDmFrac * dmAspect)
                             + (540 * baseSpFrac * spAspect); // ~796 px
                             
        System.out.printf("Available stage width: %d px\n", availWidth);
        System.out.printf("Current total landmarks width: %.1f px\n", currentTotalW);
        System.out.printf("Current remaining space (sum of both gaps): %.1f px (%.1f px per gap)\n",
            availWidth - currentTotalW, (availWidth - currentTotalW) / 2.0);
            
        double[] scaleMultipliers = {1.00, 1.03, 1.05, 1.08, 1.10, 1.125, 1.15};
        
        System.out.println("\n--- Headroom Analysis with Scale Multiplier ---");
        for (double m : scaleMultipliers) {
            double totalW = currentTotalW * m;
            double remaining = availWidth - totalW;
            double restingGap = remaining / 2.0;
            
            // Hover scale is 1.05x on one landmark
            // Largest expansion is San Pedro (361 * m * 0.05 / 2 = ~9.5 px) + DM (162 * m * 0.05 / 2 = ~4.2 px)
            double maxHoverIncursion = (361 * m * 0.05 / 2.0) + (162 * m * 0.05 / 2.0);
            double hoverGap = restingGap - maxHoverIncursion;
            
            int pct = (int) Math.round((m - 1.0) * 100);
            String label = (pct == 0) ? "Current" : String.format("+%d%%", pct);
            
            String status;
            if (restingGap < 0) {
                status = String.format("OVERLAPS by %.1f px even at rest!", -restingGap);
            } else if (hoverGap < 0) {
                status = String.format("Resting safe (%.1f px gap), but OVERLAPS by %.1f px when hovering!", restingGap, -hoverGap);
            } else if (hoverGap < 10) {
                status = String.format("Very tight (%.1f px gap resting, %.1f px hover)", restingGap, hoverGap);
            } else {
                status = String.format("Clean & safe (%.1f px gap resting, %.1f px hover)", restingGap, hoverGap);
            }
            
            System.out.printf("%-10s (m=%.3f): TotalW=%5.1f px | Gap=%4.1f px | Status: %s\n",
                label, m, totalW, restingGap, status);
        }
    }
}
