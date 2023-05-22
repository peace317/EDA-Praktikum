package gui;

import algorithm.AlgorithmExecutor;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ZFTGui implements BlockEvent, RouteEvent, PlacementEvent {

    private static final String ASSETS = "./assets";
    private static final String IMAGES = ASSETS + "/images/";
    private static final String NET = ASSETS + "/net/";
    private static final String ARCH = ASSETS + "/arch/";
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 800;

    private static final int SPACE_BUFFER = 45;

    private static final int BUTTON_HEIGHT = 25;
    private static final int BUTTON_WIDTH = 135;
    private static final int TOOL_POS = 35;
    // Main-Frame
    JFrame frame;

    JButton generatePlaceB = new JButton("Generate place");
    JButton generateRouteB = new JButton("Generate route");
    JButton stopGeneratingB = new JButton("Stop");

    // Algorithm selection
    JComboBox<String> algorithmSB = new JComboBox<>(new String[]{"ZFT", "VPR - bounding box", "VPR - net timing",
            "VPR - path timing"});
    private int selectedAlgorithm;

    // Netlist selection
    JComboBox<String> netListSB = new JComboBox<>(searchNetlistFiles());
    private String selectedNetlist;
    private Map<String, File> netlistFileMap;

    // Architecture selection
    JComboBox<String> architectureSB = new JComboBox<>(searchArchitectureFiles());
    private String selectedArchitecture;
    private Map<String, File> architectureFileMap;

    // Iteration slider
    private int iterations = 600;
    JSlider iterationsS = new JSlider(0, 5000, iterations);

    // Area swap size slider
    private int areaSwapSize = 8;
    JSlider areaSwapSizeS = new JSlider(0, 80, areaSwapSize);

    // verbose output
    JCheckBox verboseCB = new JCheckBox();

    // random initialisation of ZFT
    JCheckBox randomInitCB = new JCheckBox();

    // Console-Output
    private final JTextArea output = new JTextArea();

    // Status tools for routing
    JPanel statusP = new JPanel();
    JPanel placementConsistencyCheckP;
    JPanel netDelayValueCrossCheckP;
    JPanel routingConsistencyCheckP;

    // Status tools for placement
    JPanel generatingP;

    // Image loading icon
    ImageIcon loadingRaw = new ImageIcon(IMAGES + "loading.gif");
    ImageIcon loadingGif = new ImageIcon(loadingRaw.getImage().getScaledInstance(16, 16, Image.SCALE_FAST));
    ImageIcon checkIcon = new ImageIcon(IMAGES + "icon-check.png");
    ImageIcon noCheckIcon = new ImageIcon(IMAGES + "icon-nocheck.png");

    private final AlgorithmExecutor executor = new AlgorithmExecutor(1, this, this);

    public ZFTGui() {    //CONSTRUCTOR
        initialize();
    }


    private void initialize() {    //INITIALIZE THE GUI ELEMENTS
        frame = new JFrame();
        frame.setVisible(true);
        frame.setResizable(false);
        frame.setSize(WIDTH, HEIGHT);
        frame.setTitle("EDA");
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);
        frame.setMinimumSize(new Dimension(750, 650));

        initStatusPane();
        initToolPane();

        initToolBar();
        initOutput();
        frame.repaint();
    }


    private void initToolPane() {
        int space = 25;
        JPanel toolP = new JPanel();
        toolP.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                "Controls"));
        toolP.setLayout(null);
        toolP.setBounds(10, 10, 210, 750);

        // generate place button
        generatePlaceB.setBounds(TOOL_POS, space, BUTTON_WIDTH, BUTTON_HEIGHT);
        generatePlaceB.addActionListener(e -> {
            hideStates();
            showPlacementLoading();
            switch (selectedAlgorithm) {
                case 0 ->
                        executor.executeZFT(netlistFileMap.get(selectedNetlist),
                                architectureFileMap.get(selectedArchitecture), iterations, areaSwapSize, randomInitCB.isSelected(),
                                verboseCB.isSelected());
                case 1 ->
                        executor.executeBoundingBox(netlistFileMap.get(selectedNetlist),
                                architectureFileMap.get(selectedArchitecture));
                case 2 ->
                        executor.executeNetTiming(netlistFileMap.get(selectedNetlist),
                                architectureFileMap.get(selectedArchitecture));
                case 3 ->
                        executor.executePathTiming(netlistFileMap.get(selectedNetlist),
                                architectureFileMap.get(selectedArchitecture));
            }
        });
        space += SPACE_BUFFER;
        toolP.add(generatePlaceB);

        // generate route button
        generateRouteB.setBounds(TOOL_POS, space, BUTTON_WIDTH, BUTTON_HEIGHT);
        generateRouteB.addActionListener(e -> {
            hideStates();
            showRouteLoading();
            executor.executeVPRRouting(netlistFileMap.get(selectedNetlist),
                    architectureFileMap.get(selectedArchitecture));
        });
        space += SPACE_BUFFER;
        toolP.add(generateRouteB);

        // stop generate button
        stopGeneratingB.setBounds(TOOL_POS, space, BUTTON_WIDTH, BUTTON_HEIGHT);
        stopGeneratingB.addActionListener(e -> {
            executor.stopExecution();
            onStopLoading();
        });
        stopGeneratingB.setEnabled(false);
        space += SPACE_BUFFER;
        toolP.add(stopGeneratingB);

        // architecture select box
        JLabel archL = new JLabel("Architecture");
        archL.setBounds(TOOL_POS, space, BUTTON_WIDTH, BUTTON_HEIGHT);
        space += 25;
        toolP.add(archL);

        architectureSB.setBounds(TOOL_POS, space, BUTTON_WIDTH, BUTTON_HEIGHT);
        space += SPACE_BUFFER;
        architectureSB.addItemListener(e -> selectedArchitecture = e.getItem().toString());
        selectedArchitecture = Objects.requireNonNull(architectureSB.getSelectedItem()).toString();
        toolP.add(architectureSB);

        // netlist select box
        JLabel netlistL = new JLabel("Netlist");
        netlistL.setBounds(TOOL_POS, space, BUTTON_WIDTH, BUTTON_HEIGHT);
        space += 25;
        toolP.add(netlistL);

        netListSB.setBounds(TOOL_POS, space, BUTTON_WIDTH, BUTTON_HEIGHT);
        space += SPACE_BUFFER;
        netListSB.addItemListener(e -> selectedNetlist = e.getItem().toString());
        selectedNetlist = Objects.requireNonNull(netListSB.getSelectedItem()).toString();
        toolP.add(netListSB);

        // algorithm select box
        JLabel algorithmL = new JLabel("Algorithm");
        algorithmL.setBounds(TOOL_POS, space, BUTTON_WIDTH, BUTTON_HEIGHT);
        space += 25;
        toolP.add(algorithmL);

        algorithmSB.setBounds(TOOL_POS, space, BUTTON_WIDTH, BUTTON_HEIGHT);
        space += SPACE_BUFFER;
        algorithmSB.addItemListener(e -> {
            selectedAlgorithm = algorithmSB.getSelectedIndex();
            iterationsS.setEnabled(selectedAlgorithm == 0);
            areaSwapSizeS.setEnabled(selectedAlgorithm == 0);
            verboseCB.setEnabled(selectedAlgorithm == 0);
            randomInitCB.setEnabled(selectedAlgorithm == 0);
        });
        selectedAlgorithm = netListSB.getSelectedIndex();
        toolP.add(algorithmSB);

        // iterations slider
        JLabel iterationsL = new JLabel("Iterations: ");
        iterationsL.setBounds(TOOL_POS, space, 100, BUTTON_HEIGHT);
        toolP.add(iterationsL);
        JLabel iterationAmountL = new JLabel(String.valueOf(iterations));
        iterationAmountL.setBounds(97, space, 40, BUTTON_HEIGHT);
        toolP.add(iterationAmountL);
        space += 25;
        iterationsS.setMajorTickSpacing(1);
        iterationsS.setBounds(TOOL_POS, space, BUTTON_WIDTH, BUTTON_HEIGHT);
        space += SPACE_BUFFER;
        iterationsS.addChangeListener(e -> {
            iterations = iterationsS.getValue();
            iterationAmountL.setText(String.valueOf(iterations));
        });
        toolP.add(iterationsS);

        // iterations slider
        JLabel areaSwapL = new JLabel("Area swap size: ");
        areaSwapL.setBounds(TOOL_POS, space, 100, BUTTON_HEIGHT);
        toolP.add(areaSwapL);
        JLabel areaSwapSizeL = new JLabel(String.valueOf(areaSwapSize));
        areaSwapSizeL.setBounds(130, space, 40, BUTTON_HEIGHT);
        toolP.add(areaSwapSizeL);
        space += 25;
        areaSwapSizeS.setMajorTickSpacing(1);
        areaSwapSizeS.setBounds(TOOL_POS, space, BUTTON_WIDTH, BUTTON_HEIGHT);
        space += SPACE_BUFFER;
        areaSwapSizeS.addChangeListener(e -> {
            areaSwapSize = areaSwapSizeS.getValue();
            areaSwapSizeL.setText(String.valueOf(areaSwapSize));
        });
        toolP.add(areaSwapSizeS);

        // check verbose
        JLabel verboseL = new JLabel("Verbose: ");
        verboseL.setBounds(TOOL_POS, space, BUTTON_WIDTH, BUTTON_HEIGHT);
        toolP.add(verboseL);
        verboseCB.setBounds(TOOL_POS + 55, space, 20, BUTTON_HEIGHT);
        space += SPACE_BUFFER;
        toolP.add(verboseCB);

        // check random initialisation
        JLabel randomInitL = new JLabel("Random initialization: ");
        randomInitL.setBounds(TOOL_POS, space, BUTTON_WIDTH, BUTTON_HEIGHT);
        toolP.add(randomInitL);
        randomInitCB.setBounds(TOOL_POS + 123, space, 20, BUTTON_HEIGHT);
        toolP.add(randomInitCB);

        frame.getContentPane().add(toolP);
    }

    private void initOutput() {
        Border border = BorderFactory.createLineBorder(Color.BLACK);

        output.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        output.setBackground(new Color(240, 240, 240));
        output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        output.setEditable(false);
        DefaultCaret caret = (DefaultCaret) output.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        output.setCaret(caret);
        System.setOut(new PrintStream(new OutStream(output, System.out, new VPRListener(this))));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(output);

        JScrollPane scrollPaneOutput = new JScrollPane(panel);
        scrollPaneOutput.setBounds(230, 290, 945, 468);
        scrollPaneOutput.getVerticalScrollBar().setUnitIncrement(16);
        scrollPaneOutput.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        frame.getContentPane().add(scrollPaneOutput);
    }


    private void initToolBar() {

        JToolBar tbar = new JToolBar();
        tbar.setBounds(230, 260, 50, 30);
        tbar.setMargin(new Insets(2, 2, 0, 2));
        tbar.setFloatable(false);
        JButton clear = new JButton("Clear");
        clear.addActionListener(e -> output.setText(""));
        tbar.add(clear);
        frame.getContentPane().add(tbar);
    }

    private void initStatusPane() {

        statusP.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
                "Status"));

        statusP.setLayout(null);
        statusP.setBounds(230, 10, 750, 225);
        placementConsistencyCheckP = createLoadingPanel("placement consistency check:", 25, 200);
        generatingP = createLoadingPanel("generating placement:", 25, 155);
        netDelayValueCrossCheckP = createLoadingPanel("net delay value cross check:", 65, 195);
        routingConsistencyCheckP = createLoadingPanel("routing consistency check:", 105, 185);
        statusP.add(placementConsistencyCheckP);
        statusP.add(generatingP);
        statusP.add(netDelayValueCrossCheckP);
        statusP.add(routingConsistencyCheckP);
        frame.getContentPane().add(statusP);
    }

    private JPanel createLoadingPanel(String s, int panelYPos, int width) {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel(s, JLabel.LEFT), BorderLayout.WEST);
        p.add(new JLabel(loadingGif, JLabel.RIGHT), BorderLayout.EAST);
        p.setBounds(TOOL_POS, panelYPos, width, 32);
        p.setVisible(false);
        return p;
    }

    private String[] searchNetlistFiles() {
        netlistFileMap = new HashMap<>();
        File netFolder = new File(NET);
        for (final File fileEntry : Objects.requireNonNull(netFolder.listFiles())) {
            int dotIndex = fileEntry.getName().lastIndexOf('.');
            if (fileEntry.getName().substring(dotIndex + 1).equals("net")) {
                netlistFileMap.put(fileEntry.getName().substring(0, dotIndex), fileEntry);
            }
        }
        return netlistFileMap.keySet().stream().sorted().toArray(String[]::new);
    }

    private String[] searchArchitectureFiles() {
        architectureFileMap = new HashMap<>();
        File netFolder = new File(ARCH);
        for (final File fileEntry : Objects.requireNonNull(netFolder.listFiles())) {
            int dotIndex = fileEntry.getName().lastIndexOf('.');
            if (fileEntry.getName().substring(dotIndex + 1).equals("arch")) {
                architectureFileMap.put(fileEntry.getName().substring(0, dotIndex), fileEntry);
            }
        }
        return architectureFileMap.keySet().stream().sorted().toArray(String[]::new);
    }

    @Override
    public void blockUI() {
        setButtonsEnabled(false);
    }

    @Override
    public void freeUI() {
        setButtonsEnabled(true);
    }

    private void setButtonsEnabled(boolean enabled) {
        generatePlaceB.setEnabled(enabled);
        generateRouteB.setEnabled(enabled);
        stopGeneratingB.setEnabled(!enabled);

    }

    public void showPlacementLoading() {
        generatingP.setVisible(true);
        showPanelLoading(generatingP);
    }

    public void showRouteLoading() {
        placementConsistencyCheckP.setVisible(true);
        netDelayValueCrossCheckP.setVisible(true);
        routingConsistencyCheckP.setVisible(true);
        showPanelLoading(placementConsistencyCheckP);
        showPanelLoading(netDelayValueCrossCheckP);
        showPanelLoading(routingConsistencyCheckP);
    }

    @Override
    public void placementConsistencyCheck(boolean successful) {
        checkState(placementConsistencyCheckP, successful);
    }

    @Override
    public void netDelayValueCrossCheck(boolean successful) {
        checkState(netDelayValueCrossCheckP, successful);
    }

    @Override
    public void routingConsistencyCheck(boolean successful) {
        checkState(routingConsistencyCheckP, successful);
    }

    private void onStopLoading() {
        if (!getState(placementConsistencyCheckP)) {
            checkState(placementConsistencyCheckP, false);
        }
        if (!getState(netDelayValueCrossCheckP)) {
            checkState(netDelayValueCrossCheckP, false);
        }
        if (!getState(routingConsistencyCheckP)) {
            checkState(routingConsistencyCheckP, false);
        }
    }

    private void checkState(JPanel comp, boolean successful) {
        Component[] c = comp.getComponents();
        comp.remove(c[1]);
        JLabel label;
        if (successful) {
            label = new JLabel(checkIcon, JLabel.RIGHT);
            label.setName("check");
        } else {
            label = new JLabel(noCheckIcon, JLabel.RIGHT);
            label.setName("noCheck");
        }
        comp.add(label, BorderLayout.EAST);
        comp.repaint();
        comp.revalidate();
    }

    private void hideStates() {
        placementConsistencyCheckP.setVisible(false);
        netDelayValueCrossCheckP.setVisible(false);
        routingConsistencyCheckP.setVisible(false);
        generatingP.setVisible(false);
    }

    private boolean getState(JPanel comp) {
        Component[] c = comp.getComponents();
        return c[1].getName() != null && c[1].getName().equals("check");
    }

    private void showPanelLoading(JPanel comp) {
        Component[] c = comp.getComponents();
        comp.remove(c[1]);
        comp.add(new JLabel(loadingGif, JLabel.RIGHT), BorderLayout.EAST);
        comp.repaint();
        comp.revalidate();
    }

    @Override
    public void generating(boolean successful) {
        checkState(generatingP, successful);
    }
}

