package juanword;

/**
 * PATTERN 3: BUILDER SIR GUDOOOOO
 *
 */
public class BattleSessionBuilder {
    Player player;
    String mapName = "San Pedro Cathedral";
    int stageNum = 1;
    BattlePanel.DifficultySetting difficulty = BattlePanel.DifficultySetting.SAKTO;
    boolean isResume = false;
    String savedTiles = "";
    int savedEnemyHealth = -1;

    public BattleSessionBuilder() {}

    public BattleSessionBuilder player(Player player) {
        this.player = player;
        return this;
    }

    public BattleSessionBuilder mapName(String mapName) {
        if (mapName != null && !mapName.trim().isEmpty()) {
            this.mapName = mapName.trim();
        }
        return this;
    }

    public BattleSessionBuilder stageNum(int stageNum) {
        this.stageNum = Math.max(1, stageNum);
        return this;
    }

    public BattleSessionBuilder difficulty(BattlePanel.DifficultySetting difficulty) {
        if (difficulty != null) {
            this.difficulty = difficulty;
        }
        return this;
    }

    public BattleSessionBuilder difficulty(String diffName) {
        if (diffName != null) {
            this.difficulty = BattlePanel.DifficultySetting.fromString(diffName);
        }
        return this;
    }

    public BattleSessionBuilder asResume(boolean isResume) {
        this.isResume = isResume;
        return this;
    }

    public BattleSessionBuilder savedTiles(String savedTiles) {
        this.savedTiles = (savedTiles != null) ? savedTiles : "";
        return this;
    }

    public BattleSessionBuilder savedEnemyHealth(int enemyHealth) {
        this.savedEnemyHealth = enemyHealth;
        return this;
    }

    public BattleSession build() {
        if (player == null) {
            // Default Juan fallback
            player = new Player("P001", "Juan");
        }
        return new BattleSession(this);
    }
}
