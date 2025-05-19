package engine;

import java.io.*;
import java.util.*;

public class Leaderboard {
    private static final String FILE = "leaderboard.txt";
    private static final int MAX_ENTRIES = 3;

    public static class Entry {
        public final String name;
        public final int score;
        public Entry(String name, int score) {
            this.name = name;
            this.score = score;
        }
    }

    public static List<Entry> load() {
        List<Entry> entries = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    entries.add(new Entry(parts[0], Integer.parseInt(parts[1])));
                }
            }
        } catch (IOException ignored) {}
        entries.sort((a, b) -> Integer.compare(b.score, a.score));
        if (entries.size() > MAX_ENTRIES) entries = entries.subList(0, MAX_ENTRIES);
        return entries;
    }

    public static void save(List<Entry> entries) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            for (Entry e : entries) {
                pw.println(e.name + "," + e.score);
            }
        } catch (IOException ignored) {}
    }

    public static boolean isTopScore(int score) {
        List<Entry> entries = load();
        if (entries.size() < MAX_ENTRIES) return true;
        return score > entries.get(entries.size() - 1).score;
    }

    public static void addScore(String name, int score) {
        List<Entry> entries = load();
        entries.add(new Entry(name, score));
        entries.sort((a, b) -> Integer.compare(b.score, a.score));
        if (entries.size() > MAX_ENTRIES) entries = entries.subList(0, MAX_ENTRIES);
        save(entries);
    }
}