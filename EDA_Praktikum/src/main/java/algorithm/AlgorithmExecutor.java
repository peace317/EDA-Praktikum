package algorithm;

import algorithm.ZFTAlgorithm;
import gui.BlockEvent;
import gui.EmptyGUIEvent;
import gui.PlacementEvent;
import parser.ArchitectureParser;
import parser.NetlistParser;
import types.Architecture;
import types.CircuitElement;
import writer.PlacementWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

/**
 * Service class to execute common algorithms of vpr or own zft. Execution will take place in a separate thread.
 * Algorithms can be executed multithreaded. The usage of the same netlist will result in only one
 * placement file, therefore multithreaded execution should use different netlists.
 */
public class AlgorithmExecutor {
    private static final String ASSETS = "./assets";
    private static final String OUT = ASSETS + "/out/";
    private static final String VPR = ASSETS + "/vpr.exe";

    private final ExecutorService executorService;

    // Mainly used for stopping execution out of the ui. Not capable of using in multithreading
    private Future<?> currentTask;
    private Process currentProcess;
    // UI events
    private final BlockEvent blockEvent;
    private final PlacementEvent placementEvent;

    public AlgorithmExecutor(int corePoolSize) {
        this(corePoolSize, new EmptyGUIEvent(), new EmptyGUIEvent());
    }

    public AlgorithmExecutor(int corePoolSize, BlockEvent blockEvent, PlacementEvent placementEvent) {
        this.blockEvent = blockEvent;
        this.placementEvent = placementEvent;
        executorService = new ThreadPoolExecutor(corePoolSize, corePoolSize, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());
    }

    /**
     * Executes placement with the zero force target (zft) algorithm. IO-Block will always be randomly placed.
     *
     * @param netlist         netlist
     * @param architecture    architecture
     * @param iterations      number of iterations for placing clb's
     * @param areaSwapSize    size of an area, a block can be placed, if the zft-position is blocked
     * @param randomInitPlace whether to place the clb's randomly or place them in order of the net-crossing-values
     * @param verbose         verbose mode
     */
    public void executeZFT(File netlist, File architecture, int iterations, int areaSwapSize, boolean randomInitPlace
            , boolean verbose) {
        Runnable runnableTask = () -> {
            try {
                Thread.currentThread().setName(netlist.getName());
                blockEvent.blockUI();
                final long startTime = System.currentTimeMillis();
                ArchitectureParser archParser = new ArchitectureParser();
                NetlistParser parser = new NetlistParser();
                Architecture arch = archParser.parse(architecture);
                List<CircuitElement> nets = parser.parse(netlist, arch);

                ZFTAlgorithm algorithm = new ZFTAlgorithm(nets, parser.getNets(), arch, randomInitPlace, verbose);
                algorithm.run(iterations, areaSwapSize);
                PlacementWriter writer = new PlacementWriter();
                writer.write(OUT + getSimpleName(netlist, ".place"), netlist, architecture,
                        algorithm.getPlacementsAsList(), algorithm.getXDimensionRespectively(),
                        algorithm.getYDimensionRespectively());

                System.out.println("Placement runtime took: " + new SimpleDateFormat("ss,SSS").format(new Date((System.currentTimeMillis() - startTime))) + "s.");
                System.out.println("Finished.\n");
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

    /**
     * Executes the vpr bounding box algorithm. Placement only mode.
     *
     * @param netlist      netlist
     * @param architecture architecture
     */
    public void executeBoundingBox(File netlist, File architecture) {
        executeVPRAlgorithm(netlist, architecture, "-place_only", "bounding_box");
    }

    /**
     * @param netlist      netlist
     * @param architecture architecture
     */
    public void executeNetTiming(File netlist, File architecture) {
        executeVPRAlgorithm(netlist, architecture, "-place_only", "net_timing_driven");
    }

    /**
     * @param netlist      netlist
     * @param architecture architecture
     */
    public void executePathTiming(File netlist, File architecture) {
        executeVPRAlgorithm(netlist, architecture, "-place_only", "path_timing_driven");
    }

    /**
     * @param netlist      netlist
     * @param architecture architecture
     */
    public void executeVPRRouting(File netlist, File architecture) {
        executeVPRAlgorithm(netlist, architecture, "-route_only", "path_timing_driven");
    }

    /**
     * @param netlist      netlist
     * @param architecture architecture
     */
    private void executeVPRAlgorithm(File netlist, File architecture, String method, String algorithm) {
        Runnable runnableTask = () -> {
            Thread.currentThread().setName(netlist.getName());
            blockEvent.blockUI();
            final long startTime = System.currentTimeMillis();
            try {
                String[] cmd = {VPR, netlist.getAbsolutePath(), architecture.getAbsolutePath(),
                        OUT + getSimpleName(netlist, ".place"), OUT + getSimpleName(netlist, ".route"), method,
                        "-place_algorithm", algorithm, "-fix_pins", "random"};
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
                System.out.println("VPR runtime took: " + new SimpleDateFormat("ss,SSS").format(new Date((System.currentTimeMillis() - startTime))) + "s.");
                System.out.println("Finished.\n");
                currentProcess = null;
                blockEvent.freeUI();
            }
        };
        currentTask = executorService.submit(runnableTask);
    }

    /**
     * Stops the execution of the current running algorithm. Only in single-thread-mode. The execution
     * of following tasks is still possible.
     */
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

    /**
     * Shuts down the executor, no tasks will be accepted after that. Can be used to await termination of multiple
     * tasks.
     *
     * @param waitForShutdown wait for termination/shutdown of all running tasks
     */
    public void shutdown(boolean waitForShutdown) {
        executorService.shutdown();
        try {
            while (waitForShutdown && !executorService.awaitTermination(100, TimeUnit.MILLISECONDS)) {
                // wait
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private String getSimpleName(File file, String ending) {
        int dotIndex = file.getName().lastIndexOf('.');
        return file.getName().substring(0, dotIndex) + ending;
    }


}