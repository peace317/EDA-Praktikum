package algorithm;

import types.CircuitElement;
import types.Net;
import types.Position;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ZFTAlgorithm {

    private CircuitElement[][] placements;
    private final Integer iterations;

    public ZFTAlgorithm(List<CircuitElement> netlist, Integer iterations) {
        this.iterations = iterations;
        initPlacement(netlist);
    }

    private void initPlacement(List<CircuitElement> netlist) {
        int size = (int) Math.ceil(Math.sqrt(netlist.size()));
        placements = new CircuitElement[size][size];
        List<CircuitElement> netlistCopy = new ArrayList<>(netlist);
        List<Position> freePositions = new ArrayList<>();
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                freePositions.add(new Position(x, y));
            }
        }
        for (CircuitElement elem : netlistCopy) {
            int rand = Math.toIntExact(Math.round(Math.random() * (freePositions.size() - 1)));
            Position randPos = freePositions.get(rand);
            elem.setPosition(randPos);
            placements[randPos.getX()][randPos.getY()] = elem;
            freePositions.remove(rand);
        }
    }

    public CircuitElement[][] getPlacements() {
        return placements;
    }

    public void run() {
        List<CircuitElement> netlist = new ArrayList<>();
        for (CircuitElement[] placementRow : placements) {
            netlist.addAll(Arrays.asList(placementRow));
        }
        // Iterative Berechnung der Kräfte auf jede Komponente
        for (int iter = 0; iter < iterations; iter++) {
            for (CircuitElement component : netlist) {
                if (component == null) continue;
                Position idealPos = calculateZFTPos(component);
                if (placements[idealPos.getX()][idealPos.getY()] == null) {
                    switchPosition(component, idealPos);
                } else {
                    Position posInArea = searchPosInArea(idealPos, 4);
                    if (posInArea != null) {
                        switchPosition(component, posInArea);
                    }
                }
            }
        }
    }

    private Position searchPosInArea(Position pos, int areaSize) {
        int startX = Math.max(pos.getX() - areaSize, 0);
        int startY = Math.max(pos.getY() - areaSize, 0);
        int endX = Math.min(pos.getX() + areaSize, placements.length - 1);
        int endY = Math.min(pos.getY() + areaSize, placements[0].length - 1);

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {
                if (placements[x][y] == null) {
                    return new Position(x, y);
                }
            }
        }
        return null;
    }

    private void switchPosition(CircuitElement component, Position newPos) {
        if (component.knownPosition(newPos)) return;
        System.out.println(component.getBlockName() + " has switched position at " + newPos);
        placements[component.getX()][component.getY()] = null;
        component.setPosition(newPos);
        placements[component.getX()][component.getY()] = component;
    }
    private Position calculateZFTPos(CircuitElement component) {
        int forceX = 0;
        int weight = 0;
        int forceY = 0;
        for (Net net : component.getPinList()) {
            for (CircuitElement pad : net.getConnectedPads()) {
                forceX += pad.getWeight() * pad.getX();
                forceY += pad.getWeight() * pad.getY();
                weight += pad.getWeight();
            }
        }
        return new Position(forceX / weight, forceY / weight);
    }


}
