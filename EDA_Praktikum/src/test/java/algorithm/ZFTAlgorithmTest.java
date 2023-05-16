package algorithm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import parser.ArchitectureParser;
import parser.NetlistParser;
import types.Architecture;
import types.CircuitElement;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ZFTAlgorithmTest {
    private final static String TEST_PATH = "./src/test/java/algorithm/test.net";
    private Architecture arch;
    private NetlistParser parser;

    @BeforeEach
    void setUp() {
        arch = new Architecture();
        parser = new NetlistParser();

        arch.setIoRate(2);
    }

    @Test
    void Test_Init() {
        List<CircuitElement> elems =  parser.parse(new File(TEST_PATH));
        ZFTAlgorithm algorithm = new ZFTAlgorithm(elems, arch, 1);
        assertEquals(3, algorithm.getXDimensionRespectively());
        assertEquals(3, algorithm.getYDimensionRespectively());
        assertEquals(6, algorithm.getIoElements().size());
        assertEquals(8, algorithm.getLogicElements().size());
        assertEquals(14, algorithm.getPlacementsAsList().size());
    }

    @Test
    void Test_RunIteration() {
        List<CircuitElement> elems =  parser.parse(new File(TEST_PATH));
        ZFTAlgorithm algorithm = new ZFTAlgorithm(elems, arch, 1);
        for (int i = 0; i < 4; i++) {
            algorithm.run();
            assertEquals(6, algorithm.getIoElements().size());
            assertEquals(8, algorithm.getLogicElements().size());
            assertEquals(14, algorithm.getPlacementsAsList().size());
        }
    }
}