package juanword;

/**
 * PATTERN 3: BUILDER - Product
 * Immutable battle session configuration created via BattleSessionBuilder.
 */
public class BattleSession {
    private final Player player;
    private final String mapName;
    private final int stageNum;
    private final BattlePanel.DifficultySetting difficulty;
    private final boolean isResume;
    private final String savedTiles;
    private final int savedEnemyHealth;

    BattleSession(BattleSessionBuilder builder) {
        this.player = builder.player;
        this.mapName = builder.mapName;
        this.stageNum = builder.stageNum;
        this.difficulty = builder.difficulty;
        this.isResume = builder.isResume;
        this.savedTiles = builder.savedTiles;
        this.savedEnemyHealth = builder.savedEnemyHealth;
    }

    public Player getPlayer() { return player; }
    public String getMapName() { return mapName; }
    public int getStageNum() { return stageNum; }
    public BattlePanel.DifficultySetting getDifficulty() { return difficulty; }
    public boolean isResume() { return isResume; }
    public String getSavedTiles() { return savedTiles; }
    public int getSavedEnemyHealth() { return savedEnemyHealth; }

    @Override
    public String toString() {
        return "BattleSession[player=" + (player != null ? player.playerName : "null")
                + ", map=" + mapName
                + ", stage=" + stageNum
                + ", diff=" + difficulty.label
                + ", isResume=" + isResume
                + "]";
    }
}
