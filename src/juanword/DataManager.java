package juanword;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Handles all CSV reading and writing for JUAN Word.
 * All CSV files live in the "data/" directory at the project root.
 *
 * Writable progress files:
 *  - players.csv
 *  - unlocks.csv
 *  - settings.csv
 *  - highscores.csv
 *  - bestiary_progress.csv
 *
 * Read-only game content files:
 *  - stages.csv
 *  - enemies.csv
 *  - bestiary.csv
 *  - letter_values.csv
 *  - cebuano_dictionary_all_entries.csv
 */
public class DataManager {

    public static final Path DATA_DIR = Paths.get("data");

    // File paths
    public static final Path PLAYERS = DATA_DIR.resolve("players.csv");
    public static final Path UNLOCKS = DATA_DIR.resolve("unlocks.csv");
    public static final Path SETTINGS = DATA_DIR.resolve("settings.csv");
    public static final Path HIGHSCORES = DATA_DIR.resolve("highscores.csv");
    public static final Path BESTIARY_PROGRESS = DATA_DIR.resolve("bestiary_progress.csv");

    public static final Path STAGES = DATA_DIR.resolve("stages.csv");
    public static final Path ENEMIES = DATA_DIR.resolve("enemies.csv");
    public static final Path BESTIARY = DATA_DIR.resolve("bestiary.csv");
    public static final Path LETTER_VALUES = DATA_DIR.resolve("letter_values.csv");
    public static final Path DICTIONARY = DATA_DIR.resolve("cebuano_dictionary_all_entries.csv");

    // Standard CSV headers
    private static final String PLAYERS_HEADER = "playerId,playerName,currentStage,currentRound,score,experience,coins,currentHealth,maxHealth,lastSaved";
    private static final String UNLOCKS_HEADER = "playerId,stage1Unlocked,stage2Unlocked,stage3Unlocked,stage4Unlocked,tikbalangDefeated,kapreDefeated,manananggalDefeated,aswangDefeated";
    private static final String SETTINGS_HEADER = "playerId,musicOn,sfxOn,volume";
    private static final String HIGHSCORES_HEADER = "playerName,score,stageReached,date";
    private static final String BESTIARY_PROGRESS_HEADER = "playerId,creatureId,discovered,timesDefeated";

    // In-memory cache for valid dictionary words (O(1) lookup)
    private Set<String> dictionaryWords = null;

    public DataManager() {
        ensureFile(PLAYERS, PLAYERS_HEADER);
        ensureFile(UNLOCKS, UNLOCKS_HEADER);
        ensureFile(SETTINGS, SETTINGS_HEADER);
        ensureFile(HIGHSCORES, HIGHSCORES_HEADER);
        ensureFile(BESTIARY_PROGRESS, BESTIARY_PROGRESS_HEADER);
    }

    private void ensureFile(Path file, String header) {
        try {
            Files.createDirectories(DATA_DIR);
            if (!Files.exists(file)) {
                Files.write(file, Collections.singletonList(header), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            System.err.println("Cannot create data file: " + file.toAbsolutePath() + " - " + e.getMessage());
        }
    }

    private List<String> readLines(Path file) {
        if (!Files.exists(file)) return new ArrayList<>();
        try {
            return new ArrayList<>(Files.readAllLines(file, StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("Cannot read: " + file.toAbsolutePath() + " - " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void writeLines(Path file, List<String> lines) {
        try {
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Cannot write: " + file.toAbsolutePath() + " - " + e.getMessage());
        }
    }

    // ==========================================
    // 1. PLAYERS (Saved Game)
    // ==========================================

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
        for (int i = 1; i < lines.size(); i++) {
            if (lines.get(i).trim().isEmpty()) continue;
            Player p = Player.fromCsv(lines.get(i));
            if (p != null) list.add(p);
        }
        return list;
    }

    public Player loadLatestPlayer() {
        Player latest = null;
        for (Player p : getAllPlayers()) {
            if (latest == null || p.lastSaved.compareTo(latest.lastSaved) >= 0) {
                latest = p;
            }
        }
        return latest;
    }

    public String nextPlayerId() {
        int max = 0;
        for (Player p : getAllPlayers()) {
            try {
                max = Math.max(max, Integer.parseInt(p.playerId.substring(1)));
            } catch (Exception ignored) { }
        }
        return String.format("P%03d", max + 1);
    }

    public void deletePlayer(String playerId) {
        if (playerId == null || playerId.trim().isEmpty()) return;
        removePlayerLines(PLAYERS, playerId);
        removePlayerLines(UNLOCKS, playerId);
        removePlayerLines(SETTINGS, playerId);
        removePlayerLines(BESTIARY_PROGRESS, playerId);
    }

    private void removePlayerLines(Path file, String playerId) {
        List<String> lines = readLines(file);
        if (lines.isEmpty()) return;
        List<String> updated = new ArrayList<>();
        updated.add(lines.get(0)); // Always keep header row intact
        for (int i = 1; i < lines.size(); i++) {
            if (!lines.get(i).startsWith(playerId + ",")) {
                updated.add(lines.get(i));
            }
        }
        writeLines(file, updated);
    }

    // ==========================================
    // 2. SETTINGS
    // ==========================================

    public static class GameSettings {
        public String playerId;
        public boolean musicOn = true;
        public boolean sfxOn = true;
        public int volume = 70;

        public GameSettings(String playerId, boolean musicOn, boolean sfxOn, int volume) {
            this.playerId = playerId;
            this.musicOn = musicOn;
            this.sfxOn = sfxOn;
            this.volume = volume;
        }

        public String toCsv() {
            return playerId + "," + musicOn + "," + sfxOn + "," + volume;
        }

        public static GameSettings fromCsv(String line) {
            String[] f = line.split(",", -1);
            if (f.length < 4) return null;
            try {
                return new GameSettings(f[0].trim(), Boolean.parseBoolean(f[1].trim()),
                        Boolean.parseBoolean(f[2].trim()), Integer.parseInt(f[3].trim()));
            } catch (Exception e) {
                return null;
            }
        }
    }

    public GameSettings loadSettings(String playerId) {
        List<String> lines = readLines(SETTINGS);
        for (int i = 1; i < lines.size(); i++) {
            if (lines.get(i).startsWith(playerId + ",")) {
                GameSettings s = GameSettings.fromCsv(lines.get(i));
                if (s != null) return s;
            }
        }
        // Default settings if not found
        return new GameSettings(playerId, true, true, 70);
    }

    public void saveSettings(GameSettings s) {
        List<String> lines = readLines(SETTINGS);
        boolean found = false;
        for (int i = 1; i < lines.size(); i++) {
            if (lines.get(i).startsWith(s.playerId + ",")) {
                lines.set(i, s.toCsv());
                found = true;
                break;
            }
        }
        if (!found) lines.add(s.toCsv());
        writeLines(SETTINGS, lines);
    }

    // ==========================================
    // 3. UNLOCKS
    // ==========================================

    public static class Unlocks {
        public String playerId;
        public boolean stage1 = true;
        public boolean stage2 = false;
        public boolean stage3 = false;
        public boolean stage4 = false;
        public boolean tikbalangDefeated = false;
        public boolean kapreDefeated = false;
        public boolean manananggalDefeated = false;
        public boolean aswangDefeated = false;

        public Unlocks(String playerId) {
            this.playerId = playerId;
        }

        public String toCsv() {
            return String.join(",", playerId,
                    String.valueOf(stage1), String.valueOf(stage2), String.valueOf(stage3), String.valueOf(stage4),
                    String.valueOf(tikbalangDefeated), String.valueOf(kapreDefeated),
                    String.valueOf(manananggalDefeated), String.valueOf(aswangDefeated));
        }

        public static Unlocks fromCsv(String line) {
            String[] f = line.split(",", -1);
            if (f.length < 9) return null;
            Unlocks u = new Unlocks(f[0].trim());
            u.stage1 = Boolean.parseBoolean(f[1].trim());
            u.stage2 = Boolean.parseBoolean(f[2].trim());
            u.stage3 = Boolean.parseBoolean(f[3].trim());
            u.stage4 = Boolean.parseBoolean(f[4].trim());
            u.tikbalangDefeated = Boolean.parseBoolean(f[5].trim());
            u.kapreDefeated = Boolean.parseBoolean(f[6].trim());
            u.manananggalDefeated = Boolean.parseBoolean(f[7].trim());
            u.aswangDefeated = Boolean.parseBoolean(f[8].trim());
            return u;
        }
    }

    public Unlocks loadUnlocks(String playerId) {
        List<String> lines = readLines(UNLOCKS);
        for (int i = 1; i < lines.size(); i++) {
            if (lines.get(i).startsWith(playerId + ",")) {
                Unlocks u = Unlocks.fromCsv(lines.get(i));
                if (u != null) return u;
            }
        }
        return new Unlocks(playerId);
    }

    public void saveUnlocks(Unlocks u) {
        List<String> lines = readLines(UNLOCKS);
        boolean found = false;
        for (int i = 1; i < lines.size(); i++) {
            if (lines.get(i).startsWith(u.playerId + ",")) {
                lines.set(i, u.toCsv());
                found = true;
                break;
            }
        }
        if (!found) lines.add(u.toCsv());
        writeLines(UNLOCKS, lines);
    }

    // ==========================================
    // 4. HIGH SCORES
    // ==========================================

    public void addHighScore(String name, int score, int stageReached) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        List<String> lines = readLines(HIGHSCORES);
        lines.add(name.replace(",", " ") + "," + score + "," + stageReached + "," + date);
        writeLines(HIGHSCORES, lines);
    }

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

    // ==========================================
    // 5. BESTIARY PROGRESS
    // ==========================================

    public static class BestiaryProgress {
        public String playerId;
        public String creatureId;
        public boolean discovered = false;
        public int timesDefeated = 0;

        public BestiaryProgress(String playerId, String creatureId, boolean discovered, int timesDefeated) {
            this.playerId = playerId;
            this.creatureId = creatureId;
            this.discovered = discovered;
            this.timesDefeated = timesDefeated;
        }

        public String toCsv() {
            return playerId + "," + creatureId + "," + discovered + "," + timesDefeated;
        }
    }

    public List<BestiaryProgress> loadBestiaryProgress(String playerId) {
        List<BestiaryProgress> list = new ArrayList<>();
        List<String> lines = readLines(BESTIARY_PROGRESS);
        for (int i = 1; i < lines.size(); i++) {
            String[] f = lines.get(i).split(",", -1);
            if (f.length >= 4 && f[0].trim().equals(playerId)) {
                list.add(new BestiaryProgress(f[0].trim(), f[1].trim(),
                        Boolean.parseBoolean(f[2].trim()), parse(f[3])));
            }
        }
        return list;
    }

    public void recordCreatureDefeated(String playerId, String creatureId) {
        List<String> lines = readLines(BESTIARY_PROGRESS);
        boolean found = false;
        for (int i = 1; i < lines.size(); i++) {
            String[] f = lines.get(i).split(",", -1);
            if (f.length >= 4 && f[0].trim().equals(playerId) && f[1].trim().equals(creatureId)) {
                int count = parse(f[3]) + 1;
                lines.set(i, playerId + "," + creatureId + ",true," + count);
                found = true;
                break;
            }
        }
        if (!found) {
            lines.add(playerId + "," + creatureId + ",true,1");
        }
        writeLines(BESTIARY_PROGRESS, lines);
    }

    // ==========================================
    // 6. READ-ONLY GAME CONTENT
    // ==========================================

    public static class Stage {
        public int stageId;
        public String name;
        public String location;
        public String difficulty;
        public int regularRounds;
        public String bossId;
    }

    public List<Stage> loadStages() {
        List<Stage> list = new ArrayList<>();
        List<String> lines = readLines(STAGES);
        for (int i = 1; i < lines.size(); i++) {
            String[] f = lines.get(i).split(",", -1);
            if (f.length >= 6) {
                Stage s = new Stage();
                s.stageId = parse(f[0]);
                s.name = f[1].trim();
                s.location = f[2].trim();
                s.difficulty = f[3].trim();
                s.regularRounds = parse(f[4]);
                s.bossId = f[5].trim();
                list.add(s);
            }
        }
        return list;
    }

    public static class Enemy {
        public String enemyId;
        public String name;
        public int stage;
        public String type;
        public int maxHealth;
        public int attack;
        public String ability;
    }

    public List<Enemy> loadEnemies() {
        List<Enemy> list = new ArrayList<>();
        List<String> lines = readLines(ENEMIES);
        for (int i = 1; i < lines.size(); i++) {
            String[] f = lines.get(i).split(",", -1);
            if (f.length >= 7) {
                Enemy e = new Enemy();
                e.enemyId = f[0].trim();
                e.name = f[1].trim();
                e.stage = parse(f[2]);
                e.type = f[3].trim();
                e.maxHealth = parse(f[4]);
                e.attack = parse(f[5]);
                e.ability = f[6].trim();
                list.add(e);
            }
        }
        return list;
    }

    public static class BestiaryEntry {
        public String creatureId;
        public String name;
        public int stage;
        public String description;
    }

    public List<BestiaryEntry> loadBestiary() {
        List<BestiaryEntry> list = new ArrayList<>();
        List<String> lines = readLines(BESTIARY);
        for (int i = 1; i < lines.size(); i++) {
            String[] f = lines.get(i).split(",", -1);
            if (f.length >= 4) {
                BestiaryEntry b = new BestiaryEntry();
                b.creatureId = f[0].trim();
                b.name = f[1].trim();
                b.stage = parse(f[2]);
                b.description = f[3].trim();
                list.add(b);
            }
        }
        return list;
    }

    public Map<Character, Integer> loadLetterPoints() {
        Map<Character, Integer> map = new HashMap<>();
        for (String line : readLines(LETTER_VALUES)) {
            String[] f = line.split(",", -1);
            if (f.length >= 2 && !f[0].trim().equalsIgnoreCase("letter")) {
                String letter = f[0].trim().toLowerCase();
                if (!letter.isEmpty()) {
                    map.put(letter.charAt(0), parse(f[1]));
                }
            }
        }
        return map;
    }

    public Map<Character, Integer> loadLetterWeights() {
        Map<Character, Integer> map = new HashMap<>();
        for (String line : readLines(LETTER_VALUES)) {
            String[] f = line.split(",", -1);
            if (f.length >= 3 && !f[0].trim().equalsIgnoreCase("letter")) {
                String letter = f[0].trim().toLowerCase();
                if (!letter.isEmpty()) {
                    map.put(letter.charAt(0), parse(f[2]));
                }
            }
        }
        return map;
    }

    // ==========================================
    // 7. DICTIONARY (Word Validation)
    // ==========================================

    /**
     * Loads the 16,000+ word Cebuano dictionary into memory for O(1) word validation.
     */
    public synchronized Set<String> getDictionaryWords() {
        if (dictionaryWords == null) {
            dictionaryWords = new HashSet<>();
            List<String> lines = readLines(DICTIONARY);
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.isEmpty()) continue;
                String[] f = line.split(",", -1);
                if (f.length >= 2) {
                    String word = f[1].trim().toLowerCase();
                    if (!word.isEmpty()) {
                        dictionaryWords.add(word);
                        // Also index without hyphens (e.g. "ab-ab" -> "abab")
                        if (word.contains("-")) {
                            dictionaryWords.add(word.replace("-", ""));
                        }
                    }
                }
            }
        }
        return dictionaryWords;
    }

    /**
     * Checks if a word exists in the Cebuano dictionary.
     */
    public boolean isValidWord(String word) {
        if (word == null || word.trim().isEmpty()) return false;
        String clean = word.trim().toLowerCase();
        return getDictionaryWords().contains(clean) || getDictionaryWords().contains(clean.replace("-", ""));
    }

    private static int parse(String s) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return 0; }
    }
}
