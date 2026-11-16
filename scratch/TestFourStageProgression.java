package scratch;

import juanword.enemy.AltarBoyEnemy;
import juanword.enemy.AlviolaBossEnemy;
import juanword.enemy.Enemy;
import juanword.enemy.EnemyFactory;
import juanword.enemy.LolaEnemy;
import juanword.model.DifficultySetting;

public class TestFourStageProgression {
    public static void main(String[] args) throws Exception {
        System.out.println("=== Starting San Pedro Cathedral Stage Progression Verification Suite ===");

        // Stage 1: Lola
        Enemy s1 = EnemyFactory.createEnemy(3, 1, DifficultySetting.LISOD);
        assert s1 instanceof LolaEnemy : "Map 3 Stage 1 must be LolaEnemy";
        assert !s1.isBoss() : "Lola is not a boss";
        System.out.println("  ✓ Stage 1 (Lola) created: " + s1.getName() + ", HP=" + s1.getMaxHp() + ", ATK=" + s1.getAttack());

        // Stage 2: Altar Boy
        Enemy s2 = EnemyFactory.createEnemy(3, 2, DifficultySetting.LISOD);
        assert s2 instanceof AltarBoyEnemy : "Map 3 Stage 2 must be AltarBoyEnemy";
        assert !s2.isBoss() : "Altar Boy is not a boss";
        System.out.println("  ✓ Stage 2 (Altar Boy) created: " + s2.getName() + ", HP=" + s2.getMaxHp() + ", ATK=" + s2.getAttack());

        // Stage 3: Boss Alviola
        Enemy s3 = EnemyFactory.createEnemy(3, 3, DifficultySetting.LISOD);
        assert s3 instanceof AlviolaBossEnemy : "Map 3 Stage 3 must be AlviolaBossEnemy";
        assert s3.isBoss() : "Alviola is the Map 3 boss";
        System.out.println("  ✓ Stage 3 (Boss Alviola) created: " + s3.getName() + ", HP=" + s3.getMaxHp() + ", ATK=" + s3.getAttack());

        System.out.println("\n=== All San Pedro Cathedral Progression Tests Passed Successfully! ===");
        System.exit(0);
    }
}
