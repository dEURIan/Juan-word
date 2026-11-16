package juanword.model;

public enum AttackType {
    SLASH,
    PUNCH;

    public String getDisplayName() {
        return name().substring(0, 1) + name().substring(1).toLowerCase();
    }
}
