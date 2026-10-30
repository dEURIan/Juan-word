package juanword;

import java.awt.Graphics2D;

/**
 * PATTERN 2: FACTORY METHOD - Abstract Product
 * Base class for all combat opponents in JuanWord.
 */
public abstract class Enemy {
    protected final String name;
    protected final String title;
    protected final String subtitle;
    protected final int stage;
    protected final int baseHp;
    protected final int baseAtk;

    protected int maxHp;
    protected int curHp;
    protected int atk;

    public Enemy(String name, String title, String subtitle, int stage, int baseHp, int baseAtk) {
        this.name = name;
        this.title = title;
        this.subtitle = subtitle;
        this.stage = stage;
        this.baseHp = baseHp;
        this.baseAtk = baseAtk;
        this.maxHp = baseHp;
        this.curHp = baseHp;
        this.atk = baseAtk;
    }

    public String getName() { return name; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public int getStage() { return stage; }
    public int getBaseHp() { return baseHp; }
    public int getBaseAtk() { return baseAtk; }
    public int getMaxHp() { return maxHp; }
    public int getCurHp() { return curHp; }
    public int getAtk() { return atk; }

    public void setCurHp(int hp) { this.curHp = Math.max(0, Math.min(maxHp, hp)); }
    public void setMaxHp(int maxHp) { this.maxHp = maxHp; }
    public void setAtk(int atk) { this.atk = atk; }

    public void takeDamage(int dmg) {
        this.curHp = Math.max(0, this.curHp - dmg);
    }

    public void scaleStats(BattlePanel.DifficultySetting difficulty) {
        if (difficulty == null) difficulty = BattlePanel.DifficultySetting.SAKTO;
        switch (difficulty) {
            case SAYON:
                this.maxHp = Math.max(30, (int) Math.round(baseHp * 0.75));
                this.atk = Math.max(4, (int) Math.round(baseAtk * 0.70));
                break;
            case LISOD:
                this.maxHp = (int) Math.round(baseHp * 1.30);
                this.atk = (int) Math.round(baseAtk * 1.40);
                break;
            case SAKTO:
            default:
                this.maxHp = baseHp;
                this.atk = baseAtk;
                break;
        }
        this.curHp = this.maxHp;
    }

    /**
     * Renders this enemy's unique visual box sprite into the arena.
     */
    public abstract void render(Graphics2D g2, int x, int y, int w, int h, boolean isHurt, int lungeOffset);
}
