package juanword.enemy;

import juanword.model.BattleContext;
import juanword.model.DifficultySetting;

public final class LolaEnemy extends Enemy {
    public LolaEnemy(String name, DifficultySetting diff) {
        super(name, diff);
    }

    @Override
    public void onEnemyTurn(BattleContext ctx) {
        super.onEnemyTurn(ctx);
    }
}
