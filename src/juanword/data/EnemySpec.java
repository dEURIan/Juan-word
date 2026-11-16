package juanword.data;

public record EnemySpec(
    int mapIndex,
    int stageIndex,
    String enemyId,
    String displayName,
    boolean boss,
    int baseHp,
    int baseAttack,
    String spritePath
) {
}
