package juanword.enemy;

import juanword.model.AttackType;
import juanword.model.DifficultySetting;

public final class EnemyFactory {
    private EnemyFactory() {}

    public static Enemy createEnemy(int mapIndex, int stageIndex, DifficultySetting diff) {
        return switch (mapIndex) {
            case 1 -> switch (stageIndex) {
                case 1 -> new GuardEnemy("Guard", diff);
                case 2 -> new MaleStudentEnemy("Male Student", diff);
                case 3 -> new DizonBossEnemy("Boss Dizon", diff);
                default -> throw new IllegalArgumentException("Invalid stage: " + stageIndex + " for map 1");
            };
            case 2 -> switch (stageIndex) {
                case 1 -> new PaintingEnemy("Painting 1", diff);
                case 2 -> new PaintingEnemy("Painting 2", diff);
                case 3 -> new JanitorBossEnemy("Boss Janitor", diff);
                default -> throw new IllegalArgumentException("Invalid stage: " + stageIndex + " for map 2");
            };
            case 3 -> switch (stageIndex) {
                case 1 -> new LolaEnemy("Lola", diff);
                case 2 -> new AltarBoyEnemy("Altar Boy", diff);
                case 3 -> new AlviolaBossEnemy("Boss Alviola", diff);
                default -> throw new IllegalArgumentException("Invalid stage: " + stageIndex + " for map 3");
            };
            case 4 -> switch (stageIndex) {
                case 1 -> new MonkeyEnemy("Monkey", diff);
                case 2 -> new KarabawEnemy("Karabaw", diff);
                case 3 -> new PorterBossEnemy("Boss Porter", diff);
                case 4 -> new GudoBossEnemy("Gudo", diff);
                default -> throw new IllegalArgumentException("Invalid stage: " + stageIndex + " for map 4");
            };
            default -> throw new IllegalArgumentException("Invalid map: " + mapIndex);
        };
    }

    public static int mapCount() {
        return 4;
    }

    public static int stagesPerMap() {
        return 3;
    }

    public static final class GudoBossEnemy extends Enemy {
        public GudoBossEnemy(String name, DifficultySetting diff) {
            super(name != null ? name : "Gudo", diff);
            this.boss = true;
            this.attackType = AttackType.PUNCH;
            this.spritePath = "assets/sprites/gudoidle.png";
        }

        @Override
        public String getAttackSpritePath() {
            return "assets/sprites/gudoattack.png";
        }
    }
}
