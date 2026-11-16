package juanword.enemy;

import juanword.model.BattleContext;
import juanword.model.DifficultySetting;

public final class JanitorBossEnemy extends Enemy {
    public JanitorBossEnemy(String name, DifficultySetting diff) {
        super(name, diff);
    }

    @Override
    public void onEnemyTurn(BattleContext ctx) {
        super.onEnemyTurn(ctx);
        if (turnCount % 3 == 0 && ctx != null) {
            ctx.healEnemy(6);
        }
    }
}
