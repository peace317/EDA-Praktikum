package gui;

import algorithm.ZFTAlgorithm;
import parser.ArchitectureParser;
import parser.NetlistParser;
import types.CircuitElement;
import writer.PlacementWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.*;

public class AlgorithmExecutor {
    private static final String ASSETS = "./assets";
    private static final String OUT = ASSETS + "/out/";
    private static final String VPR = ASSETS + "/vpr.exe";

    private Future<?> currentTask;
    private Process currentProcess;
    private final BlockEvent blockEvent;
    private final PlacementEvent placementEvent;
    private final ExecutorService executorService = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>());

    public AlgorithmExecutor(BlockEvent blockEvent, PlacementEvent placementEvent) {
        this.blockEvent = blockEvent;
        this.placementEvent = placementEvent;
    }

    public void executeZFT(File netlist, File architecture, int iterations, int areaSwapSize, boolean verbose) {
        Runnable runnableTask = () -> {
            try {
                blockEvent.blockUI();
                NetlistParser parser = new NetlistParser();
                List<CircuitElement> nets = parser.parse(netlist);
                ArchitectureParser archParser = new ArchitectureParser();

                ZFTAlgorithm algorithm = new ZFTAlgorithm(nets, archParser.parse(architecture), verbose);
                algorithm.run(iterations, areaSwapSize);
                System.out.println("fertig");
                PlacementWriter writer = new PlacementWriter();
                writer.write(OUT + getSimpleName(netlist, ".place"), netlist, architecture,
                        algorithm.getPlacementsAsList(), algorithm.getXDimensionRespectively(),
                        algorithm.getYDimensionRespectively());
                placementEvent.generating(true);
            } catch (Exception e) {
                placementEvent.generating(false);
                e.printStackTrace();
            } finally {
                blockEvent.freeUI();
            }
        };
        currentTask = executorService.submit(runnableTask);
    }

    public void executeBoundingBox(File netlist, File architecture) {
        executeVPRAlgorithm(netlist, architecture, "-place_only", "bounding_box");
    }

    public void executeNetTiming(File netlist, File architecture) {
        executeVPRAlgorithm(netlist, architecture, "-place_only", "net_timing_driven");
    }

    public void executePathTiming(File netlist, File architecture) {
        executeVPRAlgorithm(netlist, architecture, "-place_only", "path_timing_driven");
    }

    public void executeVPRRouting(File netlist, File architecture) {
        executeVPRAlgorithm(netlist, architecture, "-route_only", "path_timing_driven");
    }

    private void executeVPRAlgorithm(File netlist, File architecture, String method, String algorithm) {
        Runnable runnableTask = () -> {
            blockEvent.blockUI();
            try {
                String[] cmd = {VPR, netlist.getAbsolutePath(), architecture.getAbsolutePath(),
                        OUT + getSimpleName(netlist, ".place"), OUT + getSimpleName(netlist, ".route"), method,
                        "-place_algorithm", algorithm};
                currentProcess = Runtime.getRuntime().exec(cmd);
                BufferedReader reader = new BufferedReader(new InputStreamReader(currentProcess.getInputStream()));
                String line = reader.readLine();
                while (line != null && !Thread.currentThread().isInterrupted()) {
                    System.out.println(line);
                    line = reader.readLine();
                }
                placementEvent.generating(true);
            } catch (Exception e) {
                placementEvent.generating(false);
                e.printStackTrace();
            } finally {
                currentProcess = null;
                blockEvent.freeUI();
            }
        };
        currentTask = executorService.submit(runnableTask);
    }

    public void stopExecution() {

        // canceling and destroy process
        currentTask.cancel(true);
        if (currentProcess != null) currentProcess.destroy();

        // check, whether the execution has stopped
        int trys = 10;
        while (!currentTask.isDone() && trys-- > 0) {
            try {
                currentTask.wait(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // free utility if the process has not stopped, may lead to stuck threads
        if (!currentTask.isDone()) {
            System.out.println("Termination not finished in time");
            blockEvent.freeUI();
        }
    }

    private String getSimpleName(File file, String ending) {
        int dotIndex = file.getName().lastIndexOf('.');
        return file.getName().substring(0, dotIndex) + ending;
    }


}
