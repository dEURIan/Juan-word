package juanword.enemy;

import juanword.data.DataManager;
import juanword.data.EnemySpec;
import juanword.model.AttackType;
import juanword.model.BattleContext;
import juanword.model.DifficultySetting;

public abstract class Enemy {
    protected String name;
    protected String enemyId;
    protected DifficultySetting difficulty;
    protected int maxHp;
    protected int currentHp;
    protected int attack;
    protected boolean boss;
    protected String spritePath;
    protected AttackType attackType = AttackType.PUNCH;
    protected int turnCount = 0;

    protected Enemy(String name, DifficultySetting diff) {
        this.name = name;
        this.enemyId = name.toUpperCase().replace(' ', '_');
        this.difficulty = diff != null ? diff : DifficultySetting.SAYON;

        EnemySpec spec = DataManager.getInstance().getEnemySpecById(this.enemyId);
        double scale = this.difficulty.getStatScale();
        if (spec != null) {
            this.maxHp = Math.max(1, (int) Math.round(spec.baseHp() * scale)); //easy 1.0x, medium 1.15x, hard 1.30x, extreme 1.50x
            this.attack = Math.max(1, (int) Math.round(spec.baseAttack() * scale));
            this.boss = spec.boss();
            this.spritePath = spec.spritePath();
        } else {
            this.maxHp = Math.max(1, (int) Math.round(50 * scale));
            this.attack = Math.max(1, (int) Math.round(8 * scale));
            this.boss = false;
            this.spritePath = "assets/sprites/" + this.enemyId.toLowerCase() + ".png";
        }
        this.currentHp = this.maxHp;
    }

    public String getName() { return name; }
    public String getEnemyId() { return enemyId; }
    public String getSpritePath() { return spritePath; }
    public String getAttackSpritePath() {
        if ("GUDO".equalsIgnoreCase(enemyId)) {
            return "assets/sprites/gudoattack.png";
        }
        return null;
    }
    public int getMaxHp() { return maxHp; }
    public int getCurrentHp() { return currentHp; }
    public int getAttack() { return attack; }
    public boolean isBoss() { return boss; }
    public AttackType getAttackType() { return attackType; }
    public int getTurnCount() { return turnCount; }

    public void setCurrentHp(int hp) {
        this.currentHp = Math.clamp(hp, 0, maxHp);
    }

    public void takeDamage(int amount) {
        if (amount > 0) {
            this.currentHp = Math.max(0, this.currentHp - amount);
        }
    }

    public void heal(int amount) {
        if (amount > 0) {
            this.currentHp = Math.min(this.maxHp, this.currentHp + amount);
        }
    }

    public boolean isDefeated() {
        return currentHp <= 0;
    }

    public double getHpFraction() {
        if (maxHp <= 0) return 0.0;
        return (double) currentHp / maxHp;
    }

    public void onEnemyTurn(BattleContext ctx) {
        turnCount++;
        if (ctx != null) {
            ctx.damagePlayer(attack);
        }
    }
}
