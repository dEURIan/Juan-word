package juanword;

/**
 * Model class for player save data.
 * Matches the schema of data/players.csv:
 * playerId,playerName,currentStage,currentRound,score,experience,coins,currentHealth,maxHealth,lastSaved
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

    public Player(String playerId, String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.currentStage = 1;
        this.currentRound = 0;
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
                lastSaved
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
                + ", Saved: " + lastSaved;
    }
}
