package juanword.model;

import java.awt.Color;
import java.util.function.BiConsumer;
import juanword.enemy.Enemy;
import juanword.tile.TileComponent;
import juanword.tile.TileGridGenerator;

public final class BattleContext {
    private final BattleSession session;
    private final String submittedWord;
    private final BiConsumer<String, Color> notifier;

    public BattleContext(BattleSession s, String word, BiConsumer<String, Color> notifier) {
        this.session = s;
        this.submittedWord = word != null ? word : "";
        this.notifier = notifier;
    }

    public Player getPlayer() {
        return session != null ? session.getPlayer() : null;
    }

    public Enemy getEnemy() {
        return session != null ? session.getEnemy() : null;
    }

    public TileComponent[][] getGrid() {
        return session != null ? session.getGrid() : null;
    }

    public void healPlayer(int amount) {
        if (session != null && amount > 0) {
            int old = session.getPlayerHp();
            session.setPlayerHp(old + amount);
            int gained = session.getPlayerHp() - old;
            if (notifier != null) {
                notifier.accept("+" + gained + " HP", new Color(46, 125, 50));
            }
        }
    }

    public void damagePlayer(int amount) {
        if (session != null && amount > 0) {
            session.setPlayerHp(session.getPlayerHp() - amount);
            if (notifier != null) {
                notifier.accept("-" + amount, new Color(220, 53, 69));
            }
        }
    }

    public void damageEnemy(int amount) {
        if (session != null && session.getEnemy() != null && amount > 0) {
            session.getEnemy().takeDamage(amount);
            if (notifier != null) {
                notifier.accept("-" + amount, new Color(255, 152, 0));
            }
        }
    }

    public void healEnemy(int amount) {
        if (session != null && session.getEnemy() != null && amount > 0) {
            int old = session.getEnemy().getCurrentHp();
            session.getEnemy().heal(amount);
            int gained = session.getEnemy().getCurrentHp() - old;
            if (notifier != null) {
                notifier.accept("+" + gained + " HP", new Color(233, 30, 99));
            }
        }
    }

    public void addTurnSeconds(int delta) {
        if (session != null) {
            session.setTurnSeconds(Math.max(0, session.getTurnSeconds() + delta));
            if (notifier != null) {
                notifier.accept((delta >= 0 ? "+" : "") + delta + "s", Color.YELLOW);
            }
        }
    }

    public void scrambleGrid() {
        if (session != null && session.getGrid() != null) {
            TileComponent[][] grid = session.getGrid();
            for (int r = 0; r < grid.length; r++) {
                for (int c = 0; c < grid[r].length; c++) {
                    grid[r][c] = TileGridGenerator.generateTile();
                }
            }
            if (notifier != null) {
                notifier.accept("Scrambled!", Color.CYAN);
            }
        }
    }

    public String getSubmittedWord() {
        return submittedWord;
    }
}
