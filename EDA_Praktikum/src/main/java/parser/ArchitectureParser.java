package parser;

import types.Architecture;
import types.CircuitElement;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ArchitectureParser {
    private long currentLine;

    private Architecture arch;

    public Architecture parse(String fileName) {
        return parse(new File(fileName));
    }

    public Architecture parse(File file) {
        currentLine = 1;
        arch = new Architecture();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = "";
            while ((line = br.readLine()) != null) {
                readBlock(line);
                currentLine++;
            }
            return arch;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void readBlock(String line) {
        String[] parts = line.trim().split("\\s+");
        switch (parts[0]) {
            case "subblocks_per_clb" -> arch.setSubblocksPerClb(readNumber(parts[1]));
            case "subblock_lut_size" -> arch.setSubblockLutSize(readNumber(parts[1]));
            case "io_rat" -> arch.setIoRate(readNumber(parts[1]));
            default -> {
                // do nothing, read comment or unimportant config
            }
        }
    }

    private Integer readNumber(String part) {
        try {
            return Integer.parseInt(part);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Unable to parse architecture definition in line " + currentLine + ". " +
                    "Expected a number but received '" + part + "'.", e);
        }
    }
}
