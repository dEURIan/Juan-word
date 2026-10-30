import juanword.*;
import javax.swing.*;
import java.util.List;

public class TestDialogPreview {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                JuanWord app = new JuanWord();
                DataManager dm = DataManager.getInstance();
                List<Player> players = dm.getAllPlayers();
                System.out.println("Players count: " + players.size());
                assert players.size() >= 2 : "Must have at least 2 players";
                assert app.hasActiveGame(players.get(0)) : "Player 0 must have active game";
                assert app.hasActiveGame(players.get(1)) : "Player 1 must have active game";
                System.out.println("Both players have distinct active games!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        System.exit(0);
    }
}
