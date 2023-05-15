package parser;

import types.CircuitElement;
import types.ElementType;
import types.Net;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetlistParser {

    private List<CircuitElement> netlist = new ArrayList<>();

    private List<String> knownBlockNames = new ArrayList<>();

    private Map<String, Net> knownNets = new HashMap<>();

    private long currentLine = 1;

    public List<CircuitElement> parse(String fileName) {
        return parse(new File(fileName));
    }

    public List<CircuitElement> parse(File file) {
        netlist = new ArrayList<>();
        knownBlockNames = new ArrayList<>();
        knownNets = new HashMap<>();
        currentLine = 1;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = "";
            while (line != null) {
                List<String> block = new ArrayList<>();
                while ((line = br.readLine()) != null && !line.isEmpty()) {
                    block.add(line);
                    currentLine++;
                }
                readBlock(block);
                currentLine++;
            }
            return netlist;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void readBlock(List<String> block) {
        if (block.isEmpty()) return;

        List<String[]> blockParts = new ArrayList<>();
        for (String line : block) {
            blockParts.add(line.trim().split("\\s+"));
        }
        switch (blockParts.get(0)[0]) {
            case ".input" -> readInput(blockParts);
            case ".output" -> readOutput(blockParts);
            case ".clb" -> readCLB(blockParts);
            default ->
                    throw new IllegalStateException("Unknown element type '" + blockParts.get(0)[0] + "'. (line: " + currentLine + ")");
        }
    }

    private void readInput(List<String[]> blockParts) {
        if (blockParts.size() != 2)
            throw new IllegalStateException("Unexpected number of lines for input block. (line: " + currentLine + ")");

        CircuitElement elem = new CircuitElement(ElementType.INPUT, readTypeName(blockParts.get(0)), netlist.size(),
                readPinList(blockParts.get(1)));
        netlist.add(elem);
    }

    private void readOutput(List<String[]> blockParts) {
        if (blockParts.size() != 2)
            throw new IllegalStateException("Unexpected number of lines for output block. (line: " + currentLine + ")");

        CircuitElement elem = new CircuitElement(ElementType.OUTPUT, readTypeName(blockParts.get(0)), netlist.size(),
                readPinList(blockParts.get(1)));
        netlist.add(elem);
    }

    private void readCLB(List<String[]> blockParts) {
        if (blockParts.size() != 3)
            throw new IllegalStateException("Unexpected number of lines for cbl block. (line: " + currentLine + ")");

        CircuitElement elem = new CircuitElement(ElementType.CLB, readTypeName(blockParts.get(0)), netlist.size(),
                readPinList(blockParts.get(1)), readSubBlockName(blockParts.get(2)),
                readSubBlockList(blockParts.get(2)));
        netlist.add(elem);
    }

    private String readTypeName(String[] line) {
        if (line.length < 2) throw new IllegalStateException("No blockname specified! (line: " + currentLine + ")");

        return line[1];
    }

    private String readSubBlockName(String[] line) {
        if (line.length < 3)
            throw new IllegalStateException("Missing blockname or at least one pin! (line: " + (currentLine + 2) + ")");

        String blockName = line[1];

        if (knownBlockNames.contains(blockName))
            throw new IllegalStateException("Blockname '" + blockName + "' already given! (line: " + (currentLine + 2) + ")");

        knownBlockNames.add(blockName);
        return blockName;
    }

    private List<Net> readPinList(String[] line) {
        if (line.length < 2)
            throw new IllegalStateException("At least one pin must be given! (line: " + (currentLine + 1) + ")");
        if (!line[0].equalsIgnoreCase("pinlist:"))
            throw new IllegalStateException("Unknown keyword '" + line[0] + "'. Expected pinlist instead. (line: " + (currentLine + 1) + ")");

        List<Net> pins = new ArrayList<>();
        for (int i = 1; i < line.length; i++) {
            if (line[i].startsWith("#")) {
                break;
            }
            if (!line[i].equals("open")) {
                if (knownNets.containsKey(line[i])) {
                    pins.add(knownNets.get(line[i]));
                } else {
                    Net net = new Net(line[i]);
                    pins.add(net);
                    knownNets.put(line[i], net);
                }
            }
        }

        return pins;
    }

    private List<String> readSubBlockList(String[] line) {
        if (!line[0].equalsIgnoreCase("subblock:"))
            throw new IllegalStateException("Unknown keyword '" + line[0] + "'. Expected subblock instead. (line: " + (currentLine + 2) + ")");

        List<String> pins = new ArrayList<>();
        for (int i = 2; i < line.length; i++) {
            if (!line[i].startsWith("#")) {
                pins.add(line[i]);
            }
        }

        return pins;
    }

}

