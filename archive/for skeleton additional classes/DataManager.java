import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Handles all CSV reading and writing for JUAN Word. */
public class DataManager {

    // Folder next to the project (NOT inside src). Change this if your CSVs live elsewhere.
    public static final Path DATA_DIR = Paths.get("data");
    public static final Path PLAYERS = DATA_DIR.resolve("players.csv");
    public static final Path HIGHSCORES = DATA_DIR.resolve("highscores.csv");

    private static final String PLAYERS_HEADER =
            "playerId,playerName,currentStage,currentRound,score,experience,coins,currentHealth,maxHealth,lastSaved";
    private static final String HIGHSCORES_HEADER = "playerName,score,stageReached,date";

    public DataManager() {
        ensureFile(PLAYERS, PLAYERS_HEADER);
        ensureFile(HIGHSCORES, HIGHSCORES_HEADER);
    }

    /** Creates the data folder and file (with header) if they do not exist yet. */
    private void ensureFile(Path file, String header) {
        try {
            Files.createDirectories(DATA_DIR);
            if (!Files.exists(file)) {
                Files.write(file, Arrays.asList(header), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot create " + file.toAbsolutePath(), e);
        }
    }

    private List<String> readLines(Path file) {
        try {
            return new ArrayList<>(Files.readAllLines(file, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Cannot read " + file.toAbsolutePath(), e);
        }
    }

    private void writeLines(Path file, List<String> lines) {
        try {
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Cannot write " + file.toAbsolutePath(), e);
        }
    }

    // ---------- PLAYERS ----------

    /** Saves a player: updates the row if the id exists, otherwise adds a new row. */
    public void savePlayer(Player p) {
        p.lastSaved = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        List<String> lines = readLines(PLAYERS);
        boolean found = false;
        for (int i = 1; i < lines.size(); i++) {
            if (lines.get(i).startsWith(p.playerId + ",")) {
                lines.set(i, p.toCsv());
                found = true;
                break;
            }
        }
        if (!found) lines.add(p.toCsv());
        writeLines(PLAYERS, lines);
    }

    public Player loadPlayer(String playerId) {
        for (Player p : getAllPlayers()) {
            if (p.playerId.equals(playerId)) return p;
        }
        return null;
    }

    public List<Player> getAllPlayers() {
        List<Player> list = new ArrayList<>();
        List<String> lines = readLines(PLAYERS);
        for (int i = 1; i < lines.size(); i++) {      // skip header
            if (lines.get(i).trim().isEmpty()) continue;
            Player p = Player.fromCsv(lines.get(i));
            if (p != null) list.add(p);
        }
        return list;
    }

    /** Returns the most recently saved player (used by the Continue button). */
    public Player loadLatestPlayer() {
        Player latest = null;
        for (Player p : getAllPlayers()) {
            if (latest == null || p.lastSaved.compareTo(latest.lastSaved) >= 0) latest = p;
        }
        return latest;
    }

    /** Makes the next free id: P001, P002, ... */
    public String nextPlayerId() {
        int max = 0;
        for (Player p : getAllPlayers()) {
            try {
                max = Math.max(max, Integer.parseInt(p.playerId.substring(1)));
            } catch (Exception ignored) { }
        }
        return String.format("P%03d", max + 1);
    }

    // ---------- HIGH SCORES ----------

    public void addHighScore(String name, int score, int stageReached) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        List<String> lines = readLines(HIGHSCORES);
        lines.add(name.replace(",", " ") + "," + score + "," + stageReached + "," + date);
        writeLines(HIGHSCORES, lines);
    }

    /** Returns the top scores as display strings, highest first. */
    public List<String> getTopScores(int limit) {
        List<String[]> rows = new ArrayList<>();
        List<String> lines = readLines(HIGHSCORES);
        for (int i = 1; i < lines.size(); i++) {
            String[] f = lines.get(i).split(",", -1);
            if (f.length >= 4) rows.add(f);
        }
        rows.sort((a, b) -> Integer.compare(parse(b[1]), parse(a[1])));
        List<String> out = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, rows.size()); i++) {
            String[] r = rows.get(i);
            out.add((i + 1) + ". " + r[0] + " - " + r[1] + " pts (Stage " + r[2] + ", " + r[3] + ")");
        }
        return out;
    }

    private int parse(String s) {
        try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return 0; }
    }
}
