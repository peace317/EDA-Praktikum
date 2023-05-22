package benchmark;

import com.google.gson.*;
import types.CircuitElement;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Records stats printed out by vpr or zft. Stats are recognized via the thread name, thus
 * running different algorithms in different threads is thread-safe. Although a reoccurring
 * netlist (the thread-name is made of), the stats might get overwritten. Either store the
 * stats preemptively or reset the stats.
 */
public class StatRecorder extends OutputStream {

    private StringBuilder buffer = new StringBuilder();
    private boolean recording = true;
    private final PrintStream out;
    private final Map<String, Stats> statsMap = new HashMap<>();

    public StatRecorder() {
        super();
        this.out = System.out;
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
        if (recording) {
            buffer.append((char) b);
            if (b == '\n') {
                String line = buffer.toString();
                buffer = new StringBuilder();
                accept(line);
            }
        }
    }

    /**
     * Ends the recording and processing for data in the out stream
     */
    public void endRecording() {
        recording = false;
    }

    /**
     * Processes a data line. This method can never print on System.out or an endless-loop occurs, because
     * write() of the recorder will be called again.
     */
    private void accept(String line) {
        Stats stats;
        if (statsMap.containsKey(Thread.currentThread().getName())) {
            stats = statsMap.get(Thread.currentThread().getName());
        } else {
            stats = new Stats();
            statsMap.put(Thread.currentThread().getName(), stats);
        }
        if (line.contains("Placement runtime took:")) {
            stats.setPlaceRuntime(getNextWord(line, "took:"));
        } else if (line.contains("VPR runtime took:")) {
            stats.setVprRuntime(getNextWord(line, "took:"));
        } else if (line.contains("Placing ended after")) {
            stats.setSwitchAmount(getNextWord(line, "iterations."));
        } else if (line.contains("channel width factor")) {
            stats.setMinChanelWidth(getNextWord(line, "factor of"));
        } else if (line.contains("bb_cost recomputed from scratch")) {
            stats.setCosts(getNextWord(line, "scratch is"));
        } else if (line.contains("Placement Estimated Crit Path Delay:")) {
            stats.setCritPath(getNextWord(line, "Delay:"));
        }
    }

    private static String getNextWord(String str, String word) {
        String[] words = str.split(" "), data = word.split(" ");
        int index = Arrays.asList(words).indexOf((data.length > 1) ? data[data.length - 1] : data[0]);
        if (index == -1 || ((index + 1) == words.length)) return null;
        String next = words[index + 1];
        if (next.lastIndexOf("\n") == next.length() - 1) {
            next = next.substring(0, next.length() - 1);
        }
        if (next.lastIndexOf("\r") == next.length() - 1) {
            next = next.substring(0, next.length() - 1);
        }
        if (next.lastIndexOf(".") == next.length() - 1) {
            next = next.substring(0, next.length() - 1);
        }
        return next;
    }

    public void resetStats() {
        statsMap.clear();
    }

    public void printStat(String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonArray jsonElements = new JsonArray();
            for (Map.Entry<String, Stats> entry : statsMap.entrySet()) {
                JsonObject obj = new JsonObject();
                obj.add(entry.getKey(), gson.toJsonTree(entry.getValue()));
                jsonElements.add(obj);
            }
            writer.write(gson.toJson(jsonElements));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printStat() {
        for (Map.Entry<String, Stats> entry : statsMap.entrySet()) {
            System.out.println(entry.getKey() + " stats " + entry.getValue());
        }
    }

}
