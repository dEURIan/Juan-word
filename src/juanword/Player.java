package juanword;

/**
 * Model class for player save data.
 * Matches the schema of data/players.csv:
 * playerId,playerName,currentStage,currentRound,score,experience,coins,currentHealth,maxHealth,lastSaved,savedTiles,savedDifficulty,savedEnemyHealth,savedMap,unlockedSayonStage,unlockedSaktoStage,unlockedLisodStage
 */
public class Player {
    public String playerId;
    public String playerName;
    public int currentStage = 1;
    public int currentRound = 0; // 0 = no active game started yet; >= 1 = active run
    public int score = 0;
    public int experience = 0;
    public int coins = 0;
    public int currentHealth = 100;
    public int maxHealth = 100;
    public String lastSaved = "";

    // Saved in-progress battle state for CONTINUE
    public String savedTiles = "";
    public String savedDifficulty = "Sakto";
    public int savedEnemyHealth = -1;
    public String savedMap = "San Pedro Cathedral";

    // Per-difficulty unlocked stages (San Pedro Cathedral: Stage 1 = Dizon, Stage 2 = Alviola, Stage 3 = Rola Boss)
    public int unlockedSayonStage = 1;
    public int unlockedSaktoStage = 1;
    public int unlockedLisodStage = 1;

    public Player(String playerId, String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.currentStage = 1;
        this.currentRound = 0;
        this.unlockedSayonStage = 1;
        this.unlockedSaktoStage = 1;
        this.unlockedLisodStage = 1;
    }

    /**
     * Returns the maximum stage unlocked for a specific difficulty.
     * Stage 1 (Dizon) is always unlocked (>= 1).
     * Stage 2 (Alviola) is unlocked if return >= 2.
     * Stage 3 (Rola the Boss) is unlocked if return >= 3.
     */
    public int getMaxUnlockedStage(String difficulty) {
        if (difficulty == null) return Math.max(1, currentStage);
        switch (difficulty.trim().toLowerCase()) {
            case "sayon":
            case "easy":
                return Math.max(1, unlockedSayonStage);
            case "sakto":
            case "medium":
            case "normal":
                return Math.max(1, unlockedSaktoStage);
            case "lisod":
            case "hard":
                return Math.max(1, unlockedLisodStage);
            default:
                return Math.max(1, currentStage);
        }
    }

    /**
     * Records a stage unlock for a specific difficulty.
     * Guaranteed never to decrease existing unlocked stage progress.
     */
    public void recordStageUnlock(String difficulty, int stage) {
        if (difficulty == null) return;
        switch (difficulty.trim().toLowerCase()) {
            case "sayon":
            case "easy":
                unlockedSayonStage = Math.max(unlockedSayonStage, stage);
                break;
            case "sakto":
            case "medium":
            case "normal":
                unlockedSaktoStage = Math.max(unlockedSaktoStage, stage);
                break;
            case "lisod":
            case "hard":
                unlockedLisodStage = Math.max(unlockedLisodStage, stage);
                break;
        }
        currentStage = Math.max(currentStage, Math.min(stage, 3));
    }

    /**
     * Convenience helper: returns true if the boss (Stage 3 Rola) is unlocked for the specified difficulty.
     */
    public boolean isBossUnlocked(String difficulty) {
        return getMaxUnlockedStage(difficulty) >= 3;
    }

    /** Converts this player into a single CSV line. */
    public String toCsv() {
        return String.join(",",
                playerId,
                playerName.replace(",", " "),
                String.valueOf(currentStage),
                String.valueOf(currentRound),
                String.valueOf(score),
                String.valueOf(experience),
                String.valueOf(coins),
                String.valueOf(currentHealth),
                String.valueOf(maxHealth),
                lastSaved,
                (savedTiles != null ? savedTiles : ""),
                (savedDifficulty != null ? savedDifficulty : "Sakto"),
                String.valueOf(savedEnemyHealth),
                (savedMap != null ? savedMap : "San Pedro Cathedral"),
                String.valueOf(unlockedSayonStage),
                String.valueOf(unlockedSaktoStage),
                String.valueOf(unlockedLisodStage)
        );
    }

    /** Parses one CSV line into a Player instance (returns null if invalid). */
    public static Player fromCsv(String line) {
        if (line == null || line.trim().isEmpty()) return null;
        String[] f = line.split(",", -1);
        if (f.length < 10) return null;
        try {
            Player p = new Player(f[0].trim(), f[1].trim());
            p.currentStage = Integer.parseInt(f[2].trim());
            p.currentRound = Integer.parseInt(f[3].trim());
            p.score = Integer.parseInt(f[4].trim());
            p.experience = Integer.parseInt(f[5].trim());
            p.coins = Integer.parseInt(f[6].trim());
            p.currentHealth = Integer.parseInt(f[7].trim());
            p.maxHealth = Integer.parseInt(f[8].trim());
            p.lastSaved = f[9].trim();
            if (f.length >= 11) {
                p.savedTiles = f[10].trim();
            }
            if (f.length >= 12) {
                p.savedDifficulty = f[11].trim();
            }
            if (f.length >= 13) {
                try { p.savedEnemyHealth = Integer.parseInt(f[12].trim()); } catch (Exception ignored) {}
            }
            if (f.length >= 14) {
                p.savedMap = f[13].trim();
            }
            if (f.length >= 15 && !f[14].trim().isEmpty()) {
                try { p.unlockedSayonStage = Integer.parseInt(f[14].trim()); } catch (Exception ignored) {}
            } else {
                p.unlockedSayonStage = Math.max(1, p.currentStage);
            }
            if (f.length >= 16 && !f[15].trim().isEmpty()) {
                try { p.unlockedSaktoStage = Integer.parseInt(f[15].trim()); } catch (Exception ignored) {}
            } else {
                p.unlockedSaktoStage = Math.max(1, p.currentStage);
            }
            if (f.length >= 17 && !f[16].trim().isEmpty()) {
                try { p.unlockedLisodStage = Integer.parseInt(f[16].trim()); } catch (Exception ignored) {}
            } else {
                p.unlockedLisodStage = Math.max(1, p.currentStage);
            }
            return p;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return playerName + " (" + playerId + ") - Stage " + currentStage
                + ", Round " + currentRound + ", Score " + score
                + ", XP " + experience + ", Coins " + coins
                + ", HP " + currentHealth + "/" + maxHealth
                + ", Saved: " + lastSaved
                + ", Unlocks: [Sayon: " + unlockedSayonStage + ", Sakto: " + unlockedSaktoStage + ", Lisod: " + unlockedLisodStage + "]"
                + ", Tiles: " + (savedTiles.isEmpty() ? "None" : savedTiles);
    }
}
