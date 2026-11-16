package juanword.enemy;

import juanword.model.BattleContext;
import juanword.model.DifficultySetting;

public final class PaintingEnemy extends Enemy {
    public PaintingEnemy(String name, DifficultySetting diff) {
        super(name, diff);
    }

    @Override
    public void onEnemyTurn(BattleContext ctx) {
        super.onEnemyTurn(ctx);
    }
}
