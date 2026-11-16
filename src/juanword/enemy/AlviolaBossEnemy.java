package juanword.enemy;

import juanword.model.BattleContext;
import juanword.model.DifficultySetting;

public final class AlviolaBossEnemy extends Enemy {
    public AlviolaBossEnemy(String name, DifficultySetting diff) {
        super(name, diff);
    }

    @Override
    public void onEnemyTurn(BattleContext ctx) {
        super.onEnemyTurn(ctx);
        if (turnCount % 2 == 0 && ctx != null) {
            ctx.damagePlayer(3);
        }
    }
}
