package algorithm;

import types.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ZFTAlgorithm {

    private CircuitElement[][][] placements;
    private final Integer iterations;
    private final Architecture architecture;

    private final List<CircuitElement> ioElements = new ArrayList<>();
    private final List<CircuitElement> logicElements = new ArrayList<>();

    public ZFTAlgorithm(List<CircuitElement> netlist, Architecture architecture, Integer iterations) {
        this.iterations = iterations;
        this.architecture = architecture;
        initPlacement(netlist);
    }

    private void initPlacement(List<CircuitElement> netlist) {
        List<CircuitElement> netlistCopy = new ArrayList<>(netlist);
        for (CircuitElement elem : netlistCopy) {
            if (elem.getType() == ElementType.CLB) {
                logicElements.add(elem);
            } else {
                ioElements.add(elem);
            }
        }

        int size = (int) Math.ceil(Math.sqrt(logicElements.size())) + 2;
        placements = new CircuitElement[size][size][architecture.getIoRate()];

        initPadPosition(size);
        initLogicPosition(size);
    }

    private void initPadPosition(int size) {
        List<Position> freePositions = new ArrayList<>();
        for (int i = 0; i < architecture.getIoRate(); i++) {
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y = x == 0 || x == size - 1 ? y + 1 : y + size - 1) {
                    // excluding corner pads
                    if (x != y && !(y == size - 1 && x == 0) && !(y == 0 && x == size - 1)) {
                        freePositions.add(new Position(x, y));
                    }
                }
            }
        }
        placeRandom(ioElements, freePositions);
    }

    private void initLogicPosition(int size) {
        List<Position> freePositions = new ArrayList<>();
        for (int x = 1; x < size - 1; x++) {
            for (int y = 1; y < size - 1; y++) {
                freePositions.add(new Position(x, y));
            }
        }
        placeRandom(logicElements, freePositions);
    }

    private void placeRandom(List<CircuitElement> netlist, List<Position> freePositions) {
        for (CircuitElement elem : netlist) {
            int rand = Math.toIntExact(Math.round(Math.random() * (freePositions.size() - 1)));
            Position randPos = freePositions.get(rand);
            setPosition(elem, randPos);
            freePositions.remove(rand);
        }
    }

    private void setPosition(CircuitElement elem, Position pos) {
        if (elem.getType() == ElementType.CLB) {
            if (pos.getX() == 0 || pos.getX() == placements.length - 1 || pos.getY() == 0 || pos.getY() == placements.length - 1)
                throw new IllegalStateException("Logic block can not be positioned in pad location");
            elem.setPosition(pos);
            placements[pos.getX()][pos.getY()][0] = elem;
        } else {
            int i = 0;
            while (placements[pos.getX()][pos.getY()][i] != null && i < placements[pos.getX()][pos.getY()].length) i++;
            if (i >= placements[pos.getX()][pos.getY()].length) throw new IllegalStateException("No free Positions");
            elem.setPosition(pos);
            placements[pos.getX()][pos.getY()][i] = elem;
        }
    }

    public List<CircuitElement> getIoElements() {
        return ioElements;
    }

    public List<CircuitElement> getLogicElements() {
        return logicElements;
    }

    public List<CircuitElement> getPlacementsAsList() {
        List<CircuitElement> net = new ArrayList<>();
        for (CircuitElement[][] placementRow : placements) {
            for (CircuitElement[] placement : placementRow) {
                int subblk = 0;
                for (CircuitElement block : placement) {
                    if (block != null) {
                        block.setSubblockNumber(subblk);
                        net.add(block);
                        subblk++;
                    }
                }
            }
        }
        net.sort(Comparator.comparing(CircuitElement::getBlockNumber));
        return net;
    }

    public Integer getXDimensionRespectively() {

        return placements.length - 2;
    }

    public Integer getYDimensionRespectively() {

        return placements[0].length - 2;
    }

    public void run() {
        int noSwitchCountAdjacent = 0;
        int totalIterations = 0;
        int totalSwitches = 0;
        int timeOutCount = 5;

        // iterations for switches
        for (int iter = 0; iter < iterations; iter++) {
            int switches = 0;
            // loop all logical components (only one change per iteration per component)
            for (CircuitElement component : logicElements) {
                Position idealPos = calculateZFTPos(component);
                boolean switchedPos = false;
                // ideal pos is free
                if (placements[idealPos.getX()][idealPos.getY()][0] == null) {
                    switchedPos = switchLogicBlockPosition(component, idealPos);
                }
                // pos in area is free
                if (!switchedPos && placements[idealPos.getX()][idealPos.getY()][0] != null) {
                    Position posInArea = searchPosInArea(idealPos, 6);
                    if (posInArea != null) {
                        switchedPos = switchLogicBlockPosition(component, posInArea);
                    }
                }
                // check costs and switch
                if (!switchedPos && placements[idealPos.getX()][idealPos.getY()][0] != null) {
                    double targetCost = placements[idealPos.getX()][idealPos.getY()][0].getCosts();
                    double currentCost = component.getCosts();
                    if (currentCost > targetCost) {
                        switchedPos = switchLogicBlocks(component, placements[idealPos.getX()][idealPos.getY()][0]);
                    }
                }
                if (switchedPos) {
                    switches++;
                }
            }
            totalIterations++;
            totalSwitches += switches;
            noSwitchCountAdjacent = switches == 0 ? noSwitchCountAdjacent + 1 : 0;
            if (noSwitchCountAdjacent == timeOutCount) {
                System.out.println("Breaking loop, because no changes occurred after " + timeOutCount + " iterations "
                        + "(" + totalIterations + " total, " + totalSwitches + " switches)");
                break;
            }
        }
    }

    private Position searchPosInArea(Position pos, int areaSize) {
        int startX = Math.max(pos.getX() - areaSize, 1);
        int startY = Math.max(pos.getY() - areaSize, 1);
        int endX = Math.min(pos.getX() + areaSize, placements.length - 2);
        int endY = Math.min(pos.getY() + areaSize, placements[0].length - 2);

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                if (placements[x][y][0] == null) {
                    return new Position(x, y);
                }
            }
        }
        return null;
    }

    private boolean switchLogicBlocks(CircuitElement component, CircuitElement targetComponent) {
        if (component.getType() != ElementType.CLB || targetComponent.getType() != ElementType.CLB)
            throw new IllegalStateException("Only logic blocks can switch position!");
        if (component.knownPosition(targetComponent.getPosition())) return false;
        System.out.println(component.getBlockName() + " has switched position at " + targetComponent.getPosition());
        Position targetPos = targetComponent.getPosition();
        setPosition(targetComponent, component.getPosition());
        setPosition(component, targetPos);
        return true;
    }

    private boolean switchLogicBlockPosition(CircuitElement component, Position newPos) {
        if (component.getType() != ElementType.CLB)
            throw new IllegalStateException("Only logic blocks can switch position!");
        if (component.knownPosition(newPos)) return false;
        System.out.println(component.getBlockName() + " has switched position at " + newPos);
        placements[component.getX()][component.getY()][0] = null;
        setPosition(component, newPos);
        return true;
    }

    private Position calculateZFTPos(CircuitElement component) {
        int forceX = 0;
        int weight = 0;
        int forceY = 0;
        for (Net net : component.getPinList()) {
            for (CircuitElement pad : net.getConnectedPads()) {
                if (!pad.equals(component)) {
                    forceX += pad.getWeight() * pad.getX();
                    forceY += pad.getWeight() * pad.getY();
                    weight += pad.getWeight();
                }
            }
        }

        return new Position(forceX / weight, forceY / weight);
    }


}
