package juanword.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class Player {
    private int playerId;
    private String playerName;
    private int score;
    private int maxHp;
    private int unlockedMap;
    private final Set<String> defeatedEnemies = new LinkedHashSet<>();
    private boolean hasActiveBattle;
    private int activeMap;
    private int activeStage;
    private int activePlayerHp;
    private int activeEnemyHp;
    private int activeTimerSec;
    private String serializedGrid;

    public Player(int id, String name) {
        this.playerId = id;
        this.playerName = name != null ? name.trim() : "Juan";
        this.score = 0;
        this.maxHp = 100;
        this.unlockedMap = 1;
        this.hasActiveBattle = false;
        this.activeMap = 1;
        this.activeStage = 1;
        this.activePlayerHp = 100;
        this.activeEnemyHp = 0;
        this.activeTimerSec = 0;
        this.serializedGrid = "";
    }

    public void addScore(int points) {
        if (points > 0) {
            this.score += points;
        }
    }

    public void unlockMap(int mapIndex) {
        if (mapIndex > this.unlockedMap && mapIndex <= 4) {
            this.unlockedMap = mapIndex;
        }
    }

    public void recordDefeat(String enemyId) {
        if (enemyId != null && !enemyId.isBlank()) {
            defeatedEnemies.add(enemyId.trim());
        }
    }

    public boolean hasDefeated(String enemyId) {
        return enemyId != null && defeatedEnemies.contains(enemyId.trim());
    }

    public void setActiveBattle(int map, int stage, int pHp, int eHp, int timerSec, String grid) {
        this.hasActiveBattle = true;
        this.activeMap = map;
        this.activeStage = stage;
        this.activePlayerHp = pHp;
        this.activeEnemyHp = eHp;
        this.activeTimerSec = timerSec;
        this.serializedGrid = grid != null ? grid : "";
    }

    public void clearActiveBattle() {
        this.hasActiveBattle = false;
        this.activeMap = 1;
        this.activeStage = 1;
        this.activePlayerHp = maxHp;
        this.activeEnemyHp = 0;
        this.activeTimerSec = 0;
        this.serializedGrid = "";
    }

    public String[] toCsvRow() {
        return new String[] {
            String.valueOf(playerId),
            playerName,
            String.valueOf(score),
            String.valueOf(maxHp),
            String.valueOf(unlockedMap),
            String.join(";", defeatedEnemies),
            String.valueOf(hasActiveBattle),
            String.valueOf(activeMap),
            String.valueOf(activeStage),
            String.valueOf(activePlayerHp),
            String.valueOf(activeEnemyHp),
            String.valueOf(activeTimerSec),
            serializedGrid != null ? serializedGrid : ""
        };
    }

    public static Player fromCsvRow(String[] row) {
        if (row == null || row.length < 2) {
            return new Player(1, "Juan");
        }
        int id = 1;
        try {
            id = Integer.parseInt(row[0].trim().replaceAll("[^0-9]", ""));
            if (id <= 0) id = 1;
        } catch (Exception ignored) {}

        String name = row[1].trim();
        Player player = new Player(id, name);

        if (row.length >= 13) {
            try { player.score = Integer.parseInt(row[2].trim()); } catch (Exception ignored) {}
            try { player.maxHp = Integer.parseInt(row[3].trim()); } catch (Exception ignored) {}
            try { player.unlockedMap = Integer.parseInt(row[4].trim()); } catch (Exception ignored) {}
            if (!row[5].isBlank()) {
                for (String eId : row[5].split(";")) {
                    if (!eId.isBlank()) player.defeatedEnemies.add(eId.trim());
                }
            }
            player.hasActiveBattle = Boolean.parseBoolean(row[6].trim());
            try { player.activeMap = Integer.parseInt(row[7].trim()); } catch (Exception ignored) {}
            try { player.activeStage = Integer.parseInt(row[8].trim()); } catch (Exception ignored) {}
            try { player.activePlayerHp = Integer.parseInt(row[9].trim()); } catch (Exception ignored) {}
            try { player.activeEnemyHp = Integer.parseInt(row[10].trim()); } catch (Exception ignored) {}
            try { player.activeTimerSec = Integer.parseInt(row[11].trim()); } catch (Exception ignored) {}
            player.serializedGrid = row[12].trim();
        } else if (row.length >= 9) {
            // Support legacy row format gracefully if encountered
            try { player.score = Integer.parseInt(row[4].trim()); } catch (Exception ignored) {}
            try { player.maxHp = Integer.parseInt(row[8].trim()); } catch (Exception ignored) {}
            if (row.length >= 18) {
                try { player.unlockedMap = Integer.parseInt(row[17].trim()); } catch (Exception ignored) {}
            }
        }
        if (player.unlockedMap < 1) player.unlockedMap = 1;
        if (player.unlockedMap > 4) player.unlockedMap = 4;
        if (player.maxHp <= 0) player.maxHp = 100;
        return player;
    }

    public int getPlayerId() { return playerId; }
    public void setPlayerId(int id) { this.playerId = id; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String name) { this.playerName = name; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getMaxHp() { return maxHp; }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }

    public int getUnlockedMap() { return unlockedMap; }
    public void setUnlockedMap(int unlockedMap) { this.unlockedMap = unlockedMap; }

    public Set<String> getDefeatedEnemies() {
        return Collections.unmodifiableSet(defeatedEnemies);
    }

    public boolean isHasActiveBattle() { return hasActiveBattle; }
    public boolean hasActiveBattle() { return hasActiveBattle; }
    public void setHasActiveBattle(boolean active) { this.hasActiveBattle = active; }

    public int getActiveMap() { return activeMap; }
    public void setActiveMap(int activeMap) { this.activeMap = activeMap; }

    public int getActiveStage() { return activeStage; }
    public void setActiveStage(int activeStage) { this.activeStage = activeStage; }

    public int getActivePlayerHp() { return activePlayerHp; }
    public void setActivePlayerHp(int activePlayerHp) { this.activePlayerHp = activePlayerHp; }

    public int getActiveEnemyHp() { return activeEnemyHp; }
    public void setActiveEnemyHp(int activeEnemyHp) { this.activeEnemyHp = activeEnemyHp; }

    public int getActiveTimerSec() { return activeTimerSec; }
    public void setActiveTimerSec(int activeTimerSec) { this.activeTimerSec = activeTimerSec; }

    public String getSerializedGrid() { return serializedGrid; }
    public void setSerializedGrid(String serializedGrid) { this.serializedGrid = serializedGrid; }

    @Override
    public String toString() {
        return playerName + " (Map " + unlockedMap + ", Score: " + score + ")";
    }
}
