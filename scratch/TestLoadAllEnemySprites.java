import java.awt.image.BufferedImage;
import juanword.enemy.Enemy;
import juanword.enemy.EnemyFactory;
import juanword.model.DifficultySetting;
import juanword.ui.ImageLoader;

public class TestLoadAllEnemySprites {
    public static void main(String[] args) {
        System.out.println("=== Verifying All Map Stage Enemy Sprites ===");

        int mapCount = 4;
        int stagesPerMap = 3;

        for (int map = 1; map <= mapCount; map++) {
            System.out.println("\n--- Map " + map + " ---");
            for (int stage = 1; stage <= stagesPerMap; stage++) {
                Enemy enemy = EnemyFactory.createEnemy(map, stage, DifficultySetting.SAYON);
                String spritePath = enemy.getSpritePath();
                BufferedImage img = ImageLoader.load(spritePath);

                assert img != null : "Sprite image must not be null for " + enemy.getName();
                assert !ImageLoader.isPlaceholder(img) : "Sprite image must NOT be a placeholder box for " + enemy.getName() + " (" + spritePath + ")";

                System.out.println("  Stage " + stage + " [" + enemy.getName() + "]: " + spritePath + " -> " + img.getWidth() + "x" + img.getHeight() + "px (Real Sprite)");
            }
        }

        // Also check Final Boss Gudo sprites
        BufferedImage gudoIdle = ImageLoader.load("gudoidle.png");
        assert gudoIdle != null && !ImageLoader.isPlaceholder(gudoIdle) : "Gudo idle must be real sprite";
        System.out.println("\nFinal Boss Gudo Idle: " + gudoIdle.getWidth() + "x" + gudoIdle.getHeight() + "px");

        BufferedImage gudoAttack = ImageLoader.load("gudoattack.png");
        assert gudoAttack != null && !ImageLoader.isPlaceholder(gudoAttack) : "Gudo attack must be real sprite";
        System.out.println("Final Boss Gudo Attack: " + gudoAttack.getWidth() + "x" + gudoAttack.getHeight() + "px");

        System.out.println("\n=== All 12 Enemy Sprites & Final Boss Sprites Loaded Successfully with Zero Placeholders! ===");
    }
}
