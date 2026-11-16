package juanword.enemy;

import juanword.model.BattleContext;
import juanword.model.DifficultySetting;

public final class PorterBossEnemy extends Enemy {
    public PorterBossEnemy(String name, DifficultySetting diff) {
        super(name, diff);
    }

    @Override
    public void onEnemyTurn(BattleContext ctx) {
        super.onEnemyTurn(ctx);
        if (ctx != null) {
            if (turnCount % 3 == 0) {
                ctx.scrambleGrid();
            }
            if (turnCount % 4 == 0) {
                ctx.damagePlayer(attack);
            }
        }
    }
}
