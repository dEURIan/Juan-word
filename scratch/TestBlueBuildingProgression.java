package scratch;

import juanword.enemy.DizonBossEnemy;
import juanword.enemy.Enemy;
import juanword.enemy.EnemyFactory;
import juanword.enemy.GuardEnemy;
import juanword.enemy.MaleStudentEnemy;
import juanword.model.DifficultySetting;

public class TestBlueBuildingProgression {
    public static void main(String[] args) throws Exception {
        System.out.println("=== Starting Blue Building Progression Verification Suite ===");

        // Stage 1: Guard
        Enemy s1 = EnemyFactory.createEnemy(1, 1, DifficultySetting.SAYON);
        assert s1 instanceof GuardEnemy : "Stage 1 must be GuardEnemy, got: " + s1.getClass().getName();
        assert !s1.isBoss() : "Stage 1 Guard is not boss";
        System.out.println("  ✓ Stage 1 (Guard) created: " + s1.getName() + ", HP=" + s1.getMaxHp() + ", ATK=" + s1.getAttack());

        // Stage 2: Male Student
        Enemy s2 = EnemyFactory.createEnemy(1, 2, DifficultySetting.SAYON);
        assert s2 instanceof MaleStudentEnemy : "Stage 2 must be MaleStudentEnemy, got: " + s2.getClass().getName();
        assert !s2.isBoss() : "Stage 2 Student is not boss";
        System.out.println("  ✓ Stage 2 (Male Student) created: " + s2.getName() + ", HP=" + s2.getMaxHp() + ", ATK=" + s2.getAttack());

        // Stage 3: Dizon Boss
        Enemy s3 = EnemyFactory.createEnemy(1, 3, DifficultySetting.SAYON);
        assert s3 instanceof DizonBossEnemy : "Stage 3 must be DizonBossEnemy, got: " + s3.getClass().getName();
        assert s3.isBoss() : "Stage 3 Dizon must be marked as boss";
        System.out.println("  ✓ Stage 3 (Dizon Boss) created: " + s3.getName() + ", HP=" + s3.getMaxHp() + ", ATK=" + s3.getAttack());

        System.out.println("\n=== All Blue Building Progression Tests Passed Successfully! ===");
        System.exit(0);
    }
}
