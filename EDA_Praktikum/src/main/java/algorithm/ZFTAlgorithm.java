package algorithm;

import types.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Implementation of the zft algorithm.
 */
public class ZFTAlgorithm {

    private CircuitElement[][][] placements;
    private final Architecture architecture;
    private final boolean verbose;

    private final List<CircuitElement> ioElements = new ArrayList<>();
    private final List<CircuitElement> logicElements = new ArrayList<>();
    private final List<Net> nets;
    private boolean initPhase = true;

    public ZFTAlgorithm(List<CircuitElement> netlist, List<Net> nets, Architecture architecture,
                        boolean randomInitPlace, boolean verbose) {
        this.verbose = verbose;
        this.architecture = architecture;
        this.nets = nets;
        initPlacement(netlist, randomInitPlace);
        initPhase = false;
    }

    private void initPlacement(List<CircuitElement> netlist, boolean randomInitPlace) {
        List<CircuitElement> netlistCopy = new ArrayList<>(netlist);
        for (CircuitElement elem : netlistCopy) {
            if (elem.getType() == ElementType.CLB) {
                logicElements.add(elem);
            } else {
                ioElements.add(elem);
            }
        }

        int sizeLogicElements = (int) Math.ceil(Math.sqrt(logicElements.size())) + 2;
        int sizeIOElements = (int) Math.ceil(ioElements.size() / 4.0 / architecture.getIoRate()) + 2;
        int size = Math.max(sizeIOElements, sizeLogicElements);
        placements = new CircuitElement[size][size][architecture.getIoRate()];

        System.out.println("\n");
        if (randomInitPlace) {
            System.out.println("Randomizing placements of all blocks");
        } else {
            System.out.println("Trying to place blocks by cost factors");
        }
        System.out.println("The circuit will be mapped into a " + (size - 2) + " x " + (size - 2) + " array of clbs" + ".\n");

        initPadPosition(size);
        initLogicPosition(size, randomInitPlace);
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

    private void initLogicPosition(int size, boolean randomInitPlace) {
        List<Position> freePositions = new ArrayList<>();
        for (int x = 1; x < size - 1; x++) {
            for (int y = 1; y < size - 1; y++) {
                freePositions.add(new Position(x, y));
            }
        }
        if (randomInitPlace) {
            placeRandom(logicElements, freePositions);
        } else {
            placeGridBased(freePositions);
        }
    }

    private void placeGridBased(List<Position> freePositions) {

        nets.sort(Comparator.comparingDouble(Net::calcCrossings));

        for (Net net : nets) {
            //Platzierung der Elemente des Netzes
            for (CircuitElement element : net.getConnectedPads()) {
                // Überprüfe, ob das Element bereits platziert wurde
                if (element.getPosition() == null) {
                    setPosition(element, freePositions.get(0));
                    freePositions.remove(0);
                }
            }
        }
    }

    private void placeRandom(List<CircuitElement> netlist, List<Position> freePositions) {
        for (CircuitElement elem : netlist) {
            int rand = Math.toIntExact(Math.round(Math.random() * (freePositions.size() - 1)));
            Position randPos = freePositions.get(rand);
            setPosition(elem, randPos);
            freePositions.remove(rand);
        }
    }


    public List<CircuitElement> getIoElements() {
        return ioElements;
    }

    public List<CircuitElement> getLogicElements() {
        return logicElements;
    }

    /**
     * Returns all elements in a lists.
     *
     * @return all blocks
     */
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

    /**
     * Iterates over all logical blocks and calculates there ZFT-position. Then try's to swap to this
     * position, if it's free, a place nearby is free or changes with current block, if the costs are
     * more efficient.
     *
     * @param iterations   number of iterations
     * @param areaSwapSize size of the area, a block can change to, if his ZFT-position is occupied
     * @throws InterruptedException throwing interrupt-exception, if the current thread was interrupted
     */
    public void run(int iterations, int areaSwapSize) throws InterruptedException {
        int noSwitchCountAdjacent = 0;
        int totalIterations = 0;
        int totalSwitches = 0;
        int timeOutCount = 5;

        for (CircuitElement elem : logicElements) {
            elem.calcWeight();
        }
        for (CircuitElement elem : ioElements) {
            elem.calcWeight();
        }
        // iterations for switches
        for (int iter = 0; iter < iterations; iter++) {
            // calculations take a long time and may run in a separate thread, thus checking for interrupts
            if (Thread.currentThread().isInterrupted()) throw new InterruptedException("Thread was interrupted.");
            int switches = 0;
            // loop all logical components (only one change per iteration per component)
            for (CircuitElement component : logicElements) {
                Position idealPos = calculateZFTPos(component);

                // skip routine, if component is already ideal
                if (component.getPosition().equals(idealPos)) continue;
                boolean switchedPos = false;
                // ideal pos is free
                if (placements[idealPos.getX()][idealPos.getY()][0] == null) {
                    switchedPos = switchLogicBlockPosition(component, idealPos);
                }
                // pos in area is free
                if (!switchedPos && placements[idealPos.getX()][idealPos.getY()][0] != null) {
                    Position posInArea = searchPosInArea(idealPos, areaSwapSize);
                    if (posInArea != null) {
                        switchedPos = switchLogicBlockPosition(component, posInArea);
                    }
                }
                // check costs and switch
                if (!switchedPos && placements[idealPos.getX()][idealPos.getY()][0] != null) {
                    switchedPos = switchLogicBlocks(component, placements[idealPos.getX()][idealPos.getY()][0]);
                }
                if (switchedPos) {
                    switches++;
                }
            }
            totalIterations++;
            totalSwitches += switches;
            noSwitchCountAdjacent = switches == 0 ? noSwitchCountAdjacent + 1 : 0;
            if (noSwitchCountAdjacent == timeOutCount) {
                System.out.println("Breaking loop, because no changes occurred after " + timeOutCount + " iterations.");
                break;
            }
        }
        System.out.println("Placing ended after " + totalIterations + " iterations. " + totalSwitches + " blocks " +
                "were" + " " + "switched");
    }

    /**
     * Searches a free position nearby of the target position.
     *
     * @param pos target position
     * @param areaSize size of the area to search for
     * @return free position or null, if all are occupied
     */
    private Position searchPosInArea(Position pos, int areaSize) {
        int startX = Math.max(pos.getX() - areaSize, 1);
        int startY = Math.max(pos.getY() - areaSize, 1);
        int endX = Math.min(pos.getX() + areaSize, placements.length - 2);
        int endY = Math.min(pos.getY() + areaSize, placements[0].length - 2);

        List<Position> freePositions = new ArrayList<>();
        // search area for free positions
        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                if (placements[x][y][0] == null) {
                    freePositions.add(new Position(x, y));
                }
            }
        }
        if (freePositions.isEmpty()) return null;

        // sort for the shortest distance
        freePositions.sort((m1, m2) -> {
            double d1 = m1.distance(pos);
            double d2 = m2.distance(pos);
            if (d1 == d2) return 0;
            return d1 > d2 ? 1 : -1;
        });

        return freePositions.get(0);
    }

    /**
     * Try's to switch the position with the component of occupied the zft-position. The switch will only
     * be accepted, if the sum costs of both components are smaller than before.
     *
     * @param component component that wants to switch
     * @param targetComponent target of the zft-position of the component
     * @return true, if a switch occurred
     */
    private boolean switchLogicBlocks(CircuitElement component, CircuitElement targetComponent) {
        if (component.getType() != ElementType.CLB)
            throw new IllegalStateException("Only logic blocks can switch position!");
        if (component.knownPosition(targetComponent.getPosition()) || targetComponent.getType() != ElementType.CLB)
            return false;
        if (verbose)
            System.out.println(component.getBlockName() + " has switched position at " + targetComponent.getPosition());

        double currentCost = component.calcCosts() + targetComponent.calcCosts();

        Position targetPos = targetComponent.getPosition();
        setPosition(targetComponent, component.getPosition());
        setPosition(component, targetPos);

        double costAfterSwitch = component.calcCosts() + targetComponent.calcCosts();
        // switch back
        if (currentCost <= costAfterSwitch) {
            setPosition(component, targetComponent.getPosition());
            setPosition(targetComponent, targetPos);
            return false;
        }
        return true;
    }

    /**
     * Switches the logic block position to the new position and updates the placement array.
     * A switch only occurs, if the target position is valid.
     *
     * @param component component
     * @param newPos new position
     * @return true, if the switch was successful
     */
    private boolean switchLogicBlockPosition(CircuitElement component, Position newPos) {
        if (component.getType() != ElementType.CLB)
            throw new IllegalStateException("Only logic blocks can switch position!");
        if (component.knownPosition(newPos) || newPos.getX() == 0 || newPos.getX() == placements.length - 1 || newPos.getY() == 0 || newPos.getY() == placements.length - 1)
            return false;
        if (verbose) System.out.println(component.getBlockName() + " has switched position at " + newPos);
        placements[component.getX()][component.getY()][0] = null;
        setPosition(component, newPos);
        return true;
    }

    private void setPosition(CircuitElement elem, Position pos) {
        if (elem.getType() == ElementType.CLB) {
            elem.setPosition(pos);
            placements[pos.getX()][pos.getY()][0] = elem;
        } else {
            if (!initPhase)
                throw new IllegalStateException("IO-Components are not allowed to be placed after initialisation!");
            int i = 0;
            while (placements[pos.getX()][pos.getY()][i] != null && i < placements[pos.getX()][pos.getY()].length - 1)
                i++;
            if (i >= placements[pos.getX()][pos.getY()].length) throw new IllegalStateException("No free Positions");
            elem.setPosition(pos);
            placements[pos.getX()][pos.getY()][i] = elem;
        }
    }

    /**
     * Calculates the zft-position of the given component.
     *
     * @param component component
     * @return target position
     */
    private Position calculateZFTPos(CircuitElement component) {
        int forceX = 0;
        int forceY = 0;
        int totalWeight = 0;
        for (Net net : component.getPinList()) {
            for (CircuitElement element : net.getConnectedPads()) {
                if (!element.equals(component)) {
                    forceX += element.getWeight() * element.getX();
                    forceY += element.getWeight() * element.getY();
                    totalWeight += element.getWeight();
                }
            }
        }

        return new Position(forceX / totalWeight, forceY / totalWeight);
    }
}
