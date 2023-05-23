package benchmark;

import algorithm.AlgorithmExecutor;
import gui.OutStream;
import gui.VPRListener;
import gui.ZFTGui;

import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Benchmark {
    private static final String ASSETS = "./assets";
    private static final String OUT = ASSETS + "/out/";
    private static final String NET = ASSETS + "/net/";
    private static final String PLACE = ASSETS + "/place/";
    private static final String ARCH = ASSETS + "/arch/4lut_sanitized.arch";

    public static void main(String[] args) {
        Map<String, File> netlist = searchFiles(NET, "net");
        Map<String, File> bestPlace = searchFiles(PLACE, "place");
        File arch = new File(ARCH);
        StatRecorder recorder = new StatRecorder();
        System.setOut(new PrintStream(recorder));
        AlgorithmExecutor exec = new AlgorithmExecutor(6);

        for (File file : netlist.values()) {
            exec.executeZFT(file, arch, 600, 8, false, false);
        }
        exec.shutdown(true);

        exec = new AlgorithmExecutor(6);
        for (Map.Entry<String, File> entry : netlist.entrySet()) {
            exec.executeVPRRouting(entry.getValue(), arch);
        }
        exec.shutdown(true);

        recorder.endRecording();
        recorder.printStat(OUT + "recordedStats.json");
    }

    private static Map<String, File> searchFiles(String folder, String fileEnding) {
        Map<String, File> fileMap = new HashMap<>();
        File fileFolder = new File(folder);
        for (final File fileEntry : Objects.requireNonNull(fileFolder.listFiles())) {
            int dotIndex = fileEntry.getName().lastIndexOf('.');
            if (fileEntry.getName().substring(dotIndex + 1).equals(fileEnding)) {
                fileMap.put(fileEntry.getName().substring(0, dotIndex), fileEntry);
            }
        }
        return fileMap;
    }
}
