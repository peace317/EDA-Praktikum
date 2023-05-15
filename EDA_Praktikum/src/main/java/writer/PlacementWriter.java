package writer;

import types.CircuitElement;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PlacementWriter {

    public void write(String destinationFileName, String netlistFileName, String architectureFileName, CircuitElement[][] placements) {
        write(destinationFileName, new File(netlistFileName), new File(architectureFileName), placements);
    }

    public void write(String destinationFileName, File netlistFile, File architectureFile, CircuitElement[][] placements) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(destinationFileName))) {
            writer.write("Netlist file: " + netlistFile.getAbsolutePath() + "  Architecture file: " + architectureFile.getAbsolutePath() + "\n");
            writer.write("Array size: " + placements.length + " x " + placements[0].length + " logic blocks\n\n");
            writer.write(formatLine("#block name", "x", "y", "subblk", "block number\n"));
            writer.write(formatLine("#----------", "--", "--", "------", "------------\n"));
            List<CircuitElement> netlist = listNet(placements);
            for (CircuitElement elem : netlist) {
                writer.write(formatLine(elem.getBlockName(), String.valueOf(elem.getX()), String.valueOf(elem.getY()), String.valueOf(elem.getSubblock().size()), "#" + elem.getBlockNumber()) + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String formatLine(String blockName, String x, String y, String subblk, String blockNumber) {
        return String.format("%-15s %-7s %-7s %-7s %-7s",blockName, x, y, subblk, blockNumber);
    }

    private List<CircuitElement> listNet(CircuitElement[][] placements) {
        List<CircuitElement> res = new ArrayList<>();
        for (CircuitElement[] placementRow : placements) {
            for (CircuitElement placement : placementRow) {
                if (placement != null) res.add(placement);
            }

        }
        res.sort(Comparator.comparing(CircuitElement::getBlockNumber));
        return res;
    }
}
