package gui;

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

public class ZFTGui implements BlockEvent, RouteEvent {

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
    private int iterations = 200;
    JSlider iterationsS = new JSlider(0, 500, iterations);

    // Console-Output
    private final JTextArea output = new JTextArea();
    // Status tools
    JPanel statusP = new JPanel();
    JPanel placementConsistencyCheckP;
    JPanel netDelayValueCrossCheckP;
    JPanel routingConsistencyCheckP;

    // Image loading icon
    ImageIcon loadingRaw = new ImageIcon(IMAGES + "loading.gif");
    ImageIcon loadingGif = new ImageIcon(loadingRaw.getImage().getScaledInstance(16, 16, Image.SCALE_FAST));
    ImageIcon checkIcon = new ImageIcon(IMAGES + "icon-check.png");
    ImageIcon noCheckIcon = new ImageIcon(IMAGES + "icon-nocheck.png");

    private final AlgorithmExecutor executor = new AlgorithmExecutor(this);

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
            switch (selectedAlgorithm) {
                case 0 ->
                        executor.executeZFT(netlistFileMap.get(selectedNetlist),
                                architectureFileMap.get(selectedArchitecture), iterations);
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
            showLoading();
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
        });
        selectedAlgorithm = netListSB.getSelectedIndex();
        toolP.add(algorithmSB);

        // iterations slider
        JLabel iterationsL = new JLabel("Iterations: ");
        JLabel iterationAmountL = new JLabel(String.valueOf(iterations));
        iterationsL.setBounds(TOOL_POS, space, 100, BUTTON_HEIGHT);
        iterationAmountL.setBounds(97, space, 40, BUTTON_HEIGHT);
        toolP.add(iterationAmountL);
        space += 25;
        toolP.add(iterationsL);
        iterationsS.setMajorTickSpacing(1);
        iterationsS.setBounds(TOOL_POS, space, BUTTON_WIDTH, BUTTON_HEIGHT);
        iterationsS.addChangeListener(e -> {
            iterations = iterationsS.getValue();
            iterationAmountL.setText(String.valueOf(iterations));
        });
        toolP.add(iterationsS);


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
        scrollPaneOutput.setBounds(230, 390, 945, 368);
        scrollPaneOutput.getVerticalScrollBar().setUnitIncrement(16);
        scrollPaneOutput.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        frame.getContentPane().add(scrollPaneOutput);
    }


    private void initToolBar() {

        JToolBar tbar = new JToolBar();
        tbar.setBounds(230, 360, 50, 30);
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
        statusP.setBounds(230, 10, 750, 325);
        placementConsistencyCheckP = createLoadingPanel("placement consistency check:", 25, 200);
        netDelayValueCrossCheckP = createLoadingPanel("net delay value cross check:", 65, 195);
        routingConsistencyCheckP = createLoadingPanel("routing consistency check:", 105, 185);
        statusP.add(placementConsistencyCheckP);
        statusP.add(netDelayValueCrossCheckP);
        statusP.add(routingConsistencyCheckP);
        frame.getContentPane().add(statusP);
    }

    private JPanel createLoadingPanel(String s, int yPos, int width) {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel(s, JLabel.LEFT), BorderLayout.WEST);
        p.add(new JLabel(loadingGif, JLabel.RIGHT), BorderLayout.EAST);
        p.setBounds(TOOL_POS, yPos, width, 32);
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

    public void showLoading() {
        placementConsistencyCheckP.setVisible(true);
        netDelayValueCrossCheckP.setVisible(true);
        routingConsistencyCheckP.setVisible(true);
        showLoading(placementConsistencyCheckP);
        showLoading(netDelayValueCrossCheckP);
        showLoading(routingConsistencyCheckP);
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
        if (successful) {
            JLabel label = new JLabel(checkIcon, JLabel.RIGHT);
            label.setName("check");
            comp.add(label, BorderLayout.EAST);
        } else {
            JLabel label = new JLabel(noCheckIcon, JLabel.RIGHT);
            label.setName("noCheck");
            comp.add(label, BorderLayout.EAST);
        }
        comp.repaint();
        comp.revalidate();
    }

    private void hideStates() {
        placementConsistencyCheckP.setVisible(false);
        netDelayValueCrossCheckP.setVisible(false);
        routingConsistencyCheckP.setVisible(false);
    }

    private boolean getState(JPanel comp) {
        Component[] c = comp.getComponents();
        return c[1].getName() != null && c[1].getName().equals("check");
    }

    private void showLoading(JPanel comp) {
        Component[] c = comp.getComponents();
        comp.remove(c[1]);
        comp.add(new JLabel(loadingGif, JLabel.RIGHT), BorderLayout.EAST);
        comp.repaint();
        comp.revalidate();
    }

    @Override
    public void reset() {
        placementConsistencyCheckP.setVisible(false);
    }
}

