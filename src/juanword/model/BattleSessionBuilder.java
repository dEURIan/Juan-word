package juanword.model;

import juanword.data.DataManager;
import juanword.enemy.Enemy;
import juanword.enemy.EnemyFactory;
import juanword.tile.TileComponent;
import juanword.tile.TileGridGenerator;

public final class BattleSessionBuilder {
    private Player player;
    private int mapIndex = 1;
    private int stageIndex = 1;
    private DifficultySetting difficulty = DifficultySetting.SAYON;
    private TileComponent[][] savedGrid;
    private Integer savedPlayerHp;
    private Integer savedEnemyHp;
    private int savedTurnSeconds = -1;

    public BattleSessionBuilder withPlayer(Player p) {
        this.player = p;
        return this;
    }

    public BattleSessionBuilder onMap(int m) {
        this.mapIndex = m;
        this.difficulty = DifficultySetting.forMap(m);
        return this;
    }

    public BattleSessionBuilder atStage(int s) {
        this.stageIndex = s;
        return this;
    }

    public BattleSessionBuilder withSavedGrid(TileComponent[][] g) {
        this.savedGrid = g;
        return this;
    }

    public BattleSessionBuilder withSavedHealth(Integer pHp, Integer eHp) {
        this.savedPlayerHp = (pHp != null && pHp > 0) ? pHp : null;
        this.savedEnemyHp = (eHp != null && eHp > 0) ? eHp : null;
        return this;
    }

    public BattleSessionBuilder withRemainingTimer(int sec) {
        this.savedTurnSeconds = sec;
        return this;
    }

    public BattleSession build() {
        if (player == null) {
            throw new NullPointerException("player profile required");
        }
        Enemy enemy = EnemyFactory.createEnemy(mapIndex, stageIndex, difficulty);
        if (savedEnemyHp != null) {
            enemy.setCurrentHp(Math.min(savedEnemyHp, enemy.getMaxHp()));
        }
        TileComponent[][] grid = (savedGrid != null) ? savedGrid : TileGridGenerator.generateFresh4x4();
        int hp = (savedPlayerHp != null) ? savedPlayerHp : player.getMaxHp();
        int timer = (savedTurnSeconds >= 0) ? savedTurnSeconds : difficulty.getTurnTimeSeconds();
        return new BattleSession(player, mapIndex, stageIndex, difficulty, enemy, grid, hp, timer);
    }

    public static BattleSession fromSavedPlayer(Player p) {
        if (p == null) {
            throw new NullPointerException("player profile required");
        }
        BattleSessionBuilder builder = new BattleSessionBuilder()
            .withPlayer(p)
            .onMap(p.getActiveMap())
            .atStage(p.getActiveStage())
            .withSavedHealth(p.getActivePlayerHp(), p.getActiveEnemyHp())
            .withRemainingTimer(p.getActiveTimerSec());

        if (p.getSerializedGrid() != null && !p.getSerializedGrid().isBlank()) {
            builder.withSavedGrid(DataManager.getInstance().deserializeGrid(p.getSerializedGrid()));
        }
        return builder.build();
    }
}
