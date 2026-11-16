package scratch;

import juanword.JuanWord;
import juanword.model.Player;
import juanword.ui.StartPanel;

import java.awt.Rectangle;

public class TestStartMenuFullscreenScaling {
    public static void main(String[] args) {
        System.out.println("=== Testing StartMenu Fullscreen Scaling & Alignment ===");

        JuanWord app = new JuanWord();
        StartPanel panel = new StartPanel(app);
        Player p = new Player(1, "Juan");
        p.setScore(310);
        p.setUnlockedMap(1);
        app.setActivePlayer(p);
        panel.refreshProfiles();

        // Test 1: Windowed Mode (960x540)
        verifyResolution(panel, 960, 540, "Windowed 960x540");

        // Test 2: HD Mode (1280x720)
        verifyResolution(panel, 1280, 720, "HD 1280x720");

        // Test 3: Fullscreen Full HD Mode (1920x1080)
        verifyResolution(panel, 1920, 1080, "Fullscreen 1920x1080");

        System.out.println("=== ALL START MENU FULLSCREEN TESTS PASSED SUCCESSFULLY! ===");
        System.exit(0);
    }

    private static void verifyResolution(StartPanel panel, int w, int h, String label) {
        System.out.println("\n--- Testing Resolution: " + label + " ---");
        panel.setSize(w, h);
        panel.recalculateLayout();

        Rectangle title = panel.getTitleBounds();
        Rectangle continueBtn = panel.getContinueBtnBounds();
        Rectangle newGameBtn = panel.getNewGameBtnBounds();
        Rectangle settingBtn = panel.getSettingBtnBounds();
        Rectangle exitBtn = panel.getExitBtnBounds();
        Rectangle card = panel.getPlayerCardBounds();

        System.out.println("  Title Bounds:       " + title);
        System.out.println("  ContinueBtn Bounds: " + continueBtn);
        System.out.println("  ExitBtn Bounds:     " + exitBtn);
        System.out.println("  PlayerCard Bounds:  " + card);

        // 1. Title must be horizontally centered
        int expectedTitleX = (w - title.width) / 2;
        assert Math.abs(title.x - expectedTitleX) <= 1 : label + ": Title must be horizontally centered. Expected=" + expectedTitleX + ", Actual=" + title.x;
        System.out.println("  ✓ Title is centered horizontally (" + title.x + " == " + expectedTitleX + ")");

        // 2. Buttons must be horizontally centered
        int expectedBtnX = (w - continueBtn.width) / 2;
        assert Math.abs(continueBtn.x - expectedBtnX) <= 1 : label + ": Continue button must be centered. Expected=" + expectedBtnX + ", Actual=" + continueBtn.x;
        assert continueBtn.x == newGameBtn.x && newGameBtn.x == settingBtn.x && settingBtn.x == exitBtn.x : label + ": All buttons must have matching X position";
        System.out.println("  ✓ Buttons are centered horizontally (" + continueBtn.x + " == " + expectedBtnX + ")");

        // 3. Title must not overlap Continue button
        int titleBottom = title.y + title.height;
        int continueTop = continueBtn.y;
        assert titleBottom < continueTop : label + ": Title must end strictly above Continue button! TitleBottom=" + titleBottom + ", ContinueTop=" + continueTop;
        int gap = continueTop - titleBottom;
        System.out.println("  ✓ Title does not overlap Continue button (Clearance gap=" + gap + "px)");

        // 4. Exit button must stay comfortably inside the bottom edge of screen
        int exitBottom = exitBtn.y + exitBtn.height;
        assert exitBottom <= h : label + ": Exit button exceeds screen height: " + exitBottom + " > " + h;
        System.out.println("  ✓ Exit button is inside screen bounds (Margin=" + (h - exitBottom) + "px)");

        // 5. Button dimensions scale proportionally with resolution
        if (w >= 1920) {
            assert continueBtn.width >= 380 : label + ": Fullscreen button width should be >= 380, was " + continueBtn.width;
            assert continueBtn.height >= 90 : label + ": Fullscreen button height should be >= 90, was " + continueBtn.height;
            assert card.width >= 400 : label + ": Fullscreen card width should be >= 400, was " + card.width;
            System.out.println("  ✓ Fullscreen button and player card scaling verified (Btn: " + continueBtn.width + "x" + continueBtn.height + ", Card: " + card.width + "x" + card.height + ")");
        }
    }
}
