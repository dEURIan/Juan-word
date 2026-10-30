package juanword;

/**
 * PATTERN 2: FACTORY METHOD sir gudoooo .
 */
public class EnemyFactory {

    /**
     * Factory Method to create the corresponding Enemy subclass.
     *
     */
    public static Enemy createEnemy(int stageNum, BattlePanel.DifficultySetting difficulty) {
        Enemy enemy;
        switch (stageNum) {
            case 2:
                enemy = new AlviolaEnemy();
                break;
            case 3:
                enemy = new RolaBossEnemy();
                break;
            case 1:
            default:
                enemy = new DizonEnemy();
                break;
        }
        enemy.scaleStats(difficulty);
        return enemy;
    }
}
