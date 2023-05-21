package types;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class Net {

    private final String name;

    private final List<CircuitElement> connectedPads;

    private double lastCalculatedCosts = 0;

    private boolean calculatedCostsValid = false;

    public Net(String name) {
        this.name = name;
        connectedPads = new ArrayList<>();
    }

    public void addPad(CircuitElement pad) {
        connectedPads.add(pad);
    }

    public List<CircuitElement> getConnectedPads() {
        return connectedPads;
    }

    public void invalidateCosts() {
        calculatedCostsValid = false;
    }

    public Double calcCrossings() {
        double crossings = 0;

        if (connectedPads.size() > 50) {
            crossings = 2.7933 + 0.02616 * (connectedPads.size() - 50);
        } else {
            crossings = connectedPads.size() - 1;
        }
        return crossings;
    }

    public double calcCosts() {
        if (calculatedCostsValid) return lastCalculatedCosts;

        Pair<Position, Position> minMaxPos = getBoundingBox();
        double crossings = calcCrossings();
        double costs = 0;

        costs = (minMaxPos.getRight().getX() - minMaxPos.getLeft().getX() + 1) * crossings;
        costs += (minMaxPos.getRight().getY() - minMaxPos.getLeft().getY() + 1) * crossings;

        lastCalculatedCosts = costs;
        calculatedCostsValid = true;
        return costs;
    }

    private Pair<Position, Position> getBoundingBox() {
        int xMin = Integer.MAX_VALUE;
        int yMin = Integer.MAX_VALUE;
        int xMax = Integer.MIN_VALUE;
        int yMax = Integer.MIN_VALUE;

        for (CircuitElement element : connectedPads) {
            xMin = Math.min(xMin, element.getX());
            yMin = Math.min(yMin, element.getY());
            xMax = Math.max(xMax, element.getX());
            yMax = Math.max(yMax, element.getY());
        }
        return new ImmutablePair<>(new Position(xMin, yMin), new Position(xMax, yMax));
    }
}
