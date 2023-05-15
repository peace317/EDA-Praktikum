package writer;

import types.CircuitElement;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PlacementWriter {

    public void write(String destinationFileName, File netlistFile, File architectureFile,
                      List<CircuitElement> placements, Integer xDimension, Integer yDimension) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(destinationFileName))) {
            writer.write("Netlist file: " + netlistFile.getAbsolutePath() + "  Architecture file: " + architectureFile.getAbsolutePath() + "\n");
            writer.write("Array size: " + xDimension + " x " + yDimension + " logic blocks\n\n");
            writer.write(formatLine("#block name", "x", "y", "subblk", "block number\n"));
            writer.write(formatLine("#----------", "--", "--", "------", "------------\n"));
            for (CircuitElement block : placements) {
                if (block != null) {
                    writer.write(formatLine(block.getBlockName(), String.valueOf(block.getX()),
                            String.valueOf(block.getY()), String.valueOf(block.getSubblockNumber()),
                            "#" + block.getBlockNumber()) + "\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String formatLine(String blockName, String x, String y, String subblk, String blockNumber) {
        return String.format("%-15s %-7s %-7s %-7s %-7s", blockName, x, y, subblk, blockNumber);
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
