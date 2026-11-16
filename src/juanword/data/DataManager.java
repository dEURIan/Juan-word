package juanword.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import juanword.model.BattleSession;
import juanword.model.Player;
import juanword.tile.StandardTileComponent;
import juanword.tile.TileComponent;
import juanword.tile.TileGridGenerator;

public final class DataManager {
    private static volatile DataManager instance;

    private final Set<String> cebuanoDictionary = Collections.synchronizedSet(new HashSet<>(20000));
    private final Map<Character, LetterSpec> letterSpecs = new HashMap<>();
    private final List<Character> weightedLetterPool = new ArrayList<>();
    private final Map<String, EnemySpec> enemySpecs = new LinkedHashMap<>();
    private final List<Player> players = new ArrayList<>();
    private final Random rng = new Random();
    private Path dataDir;

    public static DataManager getInstance() {
        if (instance == null) {
            synchronized (DataManager.class) {
                if (instance == null) {
                    instance = new DataManager();
                }
            }
        }
        return instance;
    }

    private DataManager() {
        dataDir = Paths.get("data");
        if (!Files.exists(dataDir)) {
            Path alt = Paths.get("..", "data");
            if (Files.exists(alt)) {
                dataDir = alt;
            } else {
                try {
                    Files.createDirectories(dataDir);
                } catch (IOException ignored) {}
            }
        }
        loadAllCsvResources();
    }

    private void loadAllCsvResources() {
        loadDictionary();
        loadLetterSpecs();
        loadEnemySpecs();
        loadPlayers();
    }

    private void loadDictionary() {
        cebuanoDictionary.clear();
        Path dictFile = dataDir.resolve("cebuano_dictionary_all_entries.csv");
        if (Files.exists(dictFile)) {
            try {
                List<String[]> rows = CsvUtil.readAll(dictFile);
                for (String[] row : rows) {
                    if (row != null && row.length > 0) {
                        String rawWord = (row.length > 1) ? row[1].trim().toUpperCase() : row[0].trim().toUpperCase();
                        if (!rawWord.equals("WORD") && !rawWord.isEmpty()) {
                            String clean = rawWord.replaceAll("[^A-Z]", "");
                            if (clean.length() >= 2) {
                                cebuanoDictionary.add(clean);
                            }
                        }
                    }
                }
            } catch (IOException ignored) {}
        }
    }

    private void loadLetterSpecs() {
        letterSpecs.clear();
        weightedLetterPool.clear();
        Path letterFile = dataDir.resolve("letter_values.csv");
        boolean loaded = false;
        if (Files.exists(letterFile)) {
            try {
                List<String[]> rows = CsvUtil.readAll(letterFile);
                for (String[] row : rows) {
                    if (row.length >= 3 && !row[0].equalsIgnoreCase("letter")) {
                        String letStr = row[0].trim().toUpperCase();
                        if (!letStr.isEmpty()) {
                            char letter = letStr.charAt(0);
                            int points = Integer.parseInt(row[1].trim());
                            int weight = Integer.parseInt(row[2].trim());
                            letterSpecs.put(letter, new LetterSpec(letter, points, weight));
                        }
                    }
                }
                if (!letterSpecs.isEmpty()) {
                    buildWeightedPool();
                    loaded = true;
                }
            } catch (Exception ignored) {}
        }

        if (!loaded) {
            String fallback = "AAAAAAIIIIIUUUUBDGGHKKLLLMNNNPRSSTTWY";
            for (char c : fallback.toCharArray()) {
                weightedLetterPool.add(c);
                letterSpecs.putIfAbsent(c, new LetterSpec(c, 1, 1));
            }
        }
    }

    private void buildWeightedPool() {
        weightedLetterPool.clear();
        for (LetterSpec spec : letterSpecs.values()) {
            for (int i = 0; i < spec.weight(); i++) {
                weightedLetterPool.add(spec.letter());
            }
        }
        if (weightedLetterPool.isEmpty()) {
            weightedLetterPool.add('A');
        }
    }

    private void loadEnemySpecs() {
        enemySpecs.clear();
        Path enemyFile = dataDir.resolve("enemies.csv");
        if (Files.exists(enemyFile)) {
            try {
                List<String[]> rows = CsvUtil.readAll(enemyFile);
                for (String[] row : rows) {
                    if (row.length >= 8 && !row[0].equalsIgnoreCase("map_index")) {
                        int map = Integer.parseInt(row[0].trim());
                        int stage = Integer.parseInt(row[1].trim());
                        String enemyId = row[2].trim().toUpperCase();
                        String name = row[3].trim();
                        boolean boss = Boolean.parseBoolean(row[4].trim());
                        int hp = Integer.parseInt(row[5].trim());
                        int atk = Integer.parseInt(row[6].trim());
                        String sprite = row[7].trim();
                        enemySpecs.put(enemyId, new EnemySpec(map, stage, enemyId, name, boss, hp, atk, sprite));
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    private void loadPlayers() {
        players.clear();
        Path playersFile = dataDir.resolve("players.csv");
        if (Files.exists(playersFile)) {
            try {
                List<String[]> rows = CsvUtil.readAll(playersFile);
                for (String[] row : rows) {
                    if (row.length > 0 && !row[0].equalsIgnoreCase("player_id") && !row[0].equalsIgnoreCase("playerId")) {
                        players.add(Player.fromCsvRow(row));
                    }
                }
            } catch (Exception ignored) {}
        }
        if (players.isEmpty()) {
            Player defaultPlayer = new Player(1, "Juan");
            players.add(defaultPlayer);
            savePlayers();
        }
    }

    public boolean isValidCebuanoWord(String candidate) {
        if (candidate == null) return false;
        String trimmed = candidate.trim().toUpperCase();
        if (trimmed.length() < 2) return false;
        return cebuanoDictionary.contains(trimmed);
    }

    public int getDictionarySize() {
        return cebuanoDictionary.size();
    }

    public LetterSpec getLetterSpec(char c) {
        return letterSpecs.get(Character.toUpperCase(c));
    }

    public int getPointsFor(char c) {
        LetterSpec spec = getLetterSpec(c);
        return spec != null ? spec.points() : 1;
    }

    public char drawRandomLetter() {
        if (weightedLetterPool.isEmpty()) return 'A';
        return weightedLetterPool.get(rng.nextInt(weightedLetterPool.size()));
    }

    public EnemySpec getEnemySpecById(String enemyId) {
        if (enemyId == null) return null;
        return enemySpecs.get(enemyId.trim().toUpperCase());
    }

    public List<Player> getAllPlayers() {
        return Collections.unmodifiableList(players);
    }

    public Player createPlayer(String name) {
        int maxId = 0;
        for (Player p : players) {
            if (p.getPlayerId() > maxId) maxId = p.getPlayerId();
        }
        Player p = new Player(maxId + 1, name);
        players.add(p);
        savePlayers();
        return p;
    }

    public void deletePlayer(int playerId) {
        players.removeIf(p -> p.getPlayerId() == playerId);
        if (players.isEmpty()) {
            players.add(new Player(1, "Juan"));
        }
        savePlayers();
    }

    public synchronized void savePlayers() {
        Path playersFile = dataDir.resolve("players.csv");
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[] {
            "player_id", "player_name", "score", "max_hp", "unlocked_map",
            "defeated_enemies", "has_active_battle", "active_map", "active_stage",
            "active_player_hp", "active_enemy_hp", "active_timer_sec", "serialized_grid"
        });
        for (Player p : players) {
            rows.add(p.toCsvRow());
        }
        try {
            CsvUtil.writeAtomic(playersFile, rows);
        } catch (IOException ignored) {}
    }

    public void savePlayer(Player player) {
        savePlayers();
    }

    public void saveActiveBattle(Player p, BattleSession s) {
        if (p == null || s == null) return;
        p.setActiveBattle(
            s.getMapIndex(),
            s.getStageIndex(),
            s.getPlayerHp(),
            s.getEnemy().getCurrentHp(),
            s.getTurnSeconds(),
            serializeGrid(s.getGrid())
        );
        savePlayers();
    }

    public void clearActiveBattle(Player p) {
        if (p == null) return;
        p.clearActiveBattle();
        savePlayers();
    }

    public String serializeGrid(TileComponent[][] grid) {
        if (grid == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                if (sb.length() > 0) sb.append(';');
                if (r < grid.length && c < grid[r].length && grid[r][c] != null) {
                    sb.append(grid[r][c].getLetter()).append(':').append(grid[r][c].getModifierCode());
                } else {
                    sb.append("A:STD");
                }
            }
        }
        return sb.toString();
    }

    public TileComponent[][] deserializeGrid(String text) {
        TileComponent[][] grid = new TileComponent[4][4];
        if (text == null || text.isBlank()) {
            return TileGridGenerator.generateFresh4x4();
        }
        String[] tokens = text.split(";");
        int idx = 0;
        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 4; c++) {
                if (idx < tokens.length) {
                    grid[r][c] = deserializeTile(tokens[idx++]);
                } else {
                    grid[r][c] = TileGridGenerator.generateTile();
                }
            }
        }
        return grid;
    }

    public TileComponent deserializeTile(String token) {
        if (token == null || token.isBlank()) {
            return TileGridGenerator.generateTile();
        }
        String[] parts = token.split(":");
        char letter = parts[0].trim().isEmpty() ? 'A' : parts[0].trim().toUpperCase().charAt(0);
        TileComponent base = new StandardTileComponent(letter);
        if (parts.length > 1) {
            return TileGridGenerator.applyModifier(base, parts[1].trim());
        }
        return base;
    }
}
