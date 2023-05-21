package algorithm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import parser.NetlistParser;
import types.Architecture;
import types.CircuitElement;
import types.ClassType;

import java.io.File;
import java.util.Arrays;
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
        arch.setIoClasses(Arrays.asList(ClassType.INPUT, ClassType.INPUT, ClassType.INPUT, ClassType.INPUT,
                ClassType.OUTPUT, ClassType.GLOBAL));
    }

    @Test
    void Test_Init() {
        List<CircuitElement> elems = parser.parse(new File(TEST_PATH), arch);
        ZFTAlgorithm algorithm = new ZFTAlgorithm(elems, parser.getNets(), arch, false, false);
        assertEquals(3, algorithm.getXDimensionRespectively());
        assertEquals(3, algorithm.getYDimensionRespectively());
        assertEquals(6, algorithm.getIoElements().size());
        assertEquals(8, algorithm.getLogicElements().size());
        assertEquals(14, algorithm.getPlacementsAsList().size());
    }

    @Test
    void Test_RunIteration() {
        List<CircuitElement> elems = parser.parse(new File(TEST_PATH), arch);
        ZFTAlgorithm algorithm = new ZFTAlgorithm(elems, parser.getNets(), arch, false, false);
        for (int i = 0; i < 4; i++) {
            try {
                algorithm.run(1, 4);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            assertEquals(6, algorithm.getIoElements().size());
            assertEquals(8, algorithm.getLogicElements().size());
            assertEquals(14, algorithm.getPlacementsAsList().size());
        }
    }
}