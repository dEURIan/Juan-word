package juanword.enemy;

import juanword.model.BattleContext;
import juanword.model.DifficultySetting;

public final class MonkeyEnemy extends Enemy {
    public MonkeyEnemy(String name, DifficultySetting diff) {
        super(name, diff);
    }

    @Override
    public void onEnemyTurn(BattleContext ctx) {
        super.onEnemyTurn(ctx);
        if (difficulty.isTimed() && ctx != null) {
            ctx.addTurnSeconds(-3);
        }
    }
}
