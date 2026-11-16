package juanword.model;

public enum DifficultySetting {
    SAYON(1, new double[]{0.80, 1.00, 1.20, 1.45, 1.80, 1.80}, 0, 0, 1.0, "Sayon"),
    SAKTO(2, new double[]{0.40, 0.65, 1.00, 1.45, 1.90, 2.40}, 0, 0, 1.15, "Sakto"),
    LISOD(3, new double[]{0.20, 0.35, 0.85, 1.50, 2.20, 3.00}, 25, 8, 1.30, "Lisod"),
    EXTREME(4, new double[]{0.15, 0.35, 0.85, 1.60, 2.40, 3.20}, 20, 0, 1.50, "Extreme");

    private final int mapIndex;
    private final double[] lengthMultipliers;
    private final int turnTimeSeconds;
    private final int invalidCounterDamage;
    private final double statScale;
    private final String displayName;

    DifficultySetting(int mapIndex, double[] lengthMultipliers, int turnTimeSeconds,
                     int invalidCounterDamage, double statScale, String displayName) {
        this.mapIndex = mapIndex;
        this.lengthMultipliers = lengthMultipliers;
        this.turnTimeSeconds = turnTimeSeconds;
        this.invalidCounterDamage = invalidCounterDamage;
        this.statScale = statScale;
        this.displayName = displayName;
    }

    public static DifficultySetting forMap(int mapIndex) {
        for (DifficultySetting ds : values()) {
            if (ds.mapIndex == mapIndex) return ds;
        }
        throw new IllegalArgumentException("Unknown mapIndex: " + mapIndex);
    }

    public double getLengthMultiplier(int wordLength) {
        if (wordLength <= 2) return lengthMultipliers[0];
        if (wordLength == 3) return lengthMultipliers[1];
        if (wordLength == 4) return lengthMultipliers[2];
        if (wordLength == 5) return lengthMultipliers[3];
        if (wordLength == 6) return lengthMultipliers[4];
        return lengthMultipliers[5];
    }

    public int getTurnTimeSeconds() {
        return turnTimeSeconds;
    }

    public boolean isTimed() {
        return turnTimeSeconds > 0;
    }

    public int getInvalidCounterDamage() {
        return invalidCounterDamage;
    }

    public double getStatScale() {
        return statScale;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMapIndex() {
        return mapIndex;
    }
}
