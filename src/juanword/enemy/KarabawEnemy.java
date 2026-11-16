package juanword.enemy;

import juanword.model.BattleContext;
import juanword.model.DifficultySetting;

public final class KarabawEnemy extends Enemy {
    public KarabawEnemy(String name, DifficultySetting diff) {
        super(name, diff);
    }

    @Override
    public void onEnemyTurn(BattleContext ctx) {
        turnCount++;
        if (ctx != null) {
            if (turnCount % 3 == 0) {
                ctx.damagePlayer((int) Math.round(attack * 1.5));
            } else {
                ctx.damagePlayer(attack);
            }
        }
    }
}
