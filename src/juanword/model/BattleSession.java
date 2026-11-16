package juanword.model;

import juanword.enemy.Enemy;
import juanword.tile.TileComponent;

public final class BattleSession {
    private final Player player;
    private final int mapIndex;
    private final int stageIndex;
    private final DifficultySetting difficulty;
    private final Enemy enemy;
    private final TileComponent[][] grid;
    private int playerHp;
    private int turnSeconds;

    BattleSession(Player p, int map, int stage, DifficultySetting d, Enemy e,
                  TileComponent[][] grid, int hp, int timer) {
        this.player = p;
        this.mapIndex = map;
        this.stageIndex = stage;
        this.difficulty = d;
        this.enemy = e;
        this.grid = grid;
        this.playerHp = Math.clamp(hp, 0, p != null ? p.getMaxHp() : 100);
        this.turnSeconds = timer;
    }

    public int getPlayerHp() {
        return playerHp;
    }

    public void setPlayerHp(int hp) {
        int max = player != null ? player.getMaxHp() : 100;
        this.playerHp = Math.clamp(hp, 0, max);
    }

    public int getTurnSeconds() {
        return turnSeconds;
    }

    public void setTurnSeconds(int s) {
        this.turnSeconds = Math.max(0, s);
    }

    public boolean isBossStage() {
        return (mapIndex == 4) ? stageIndex >= 3 : stageIndex == 3;
    }

    public boolean isFinalBoss() {
        return mapIndex == 4 && stageIndex >= 4;
    }

    public Player getPlayer() { return player; }
    public int getMapIndex() { return mapIndex; }
    public int getStageIndex() { return stageIndex; }
    public DifficultySetting getDifficulty() { return difficulty; }
    public Enemy getEnemy() { return enemy; }
    public TileComponent[][] getGrid() { return grid; }
}
