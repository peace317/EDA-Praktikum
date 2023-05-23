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

    /* By VPR: Expected crossing counts for nets with different #'s of pins.  From *
     * ICCAD 94 pp. 690 - 695 (with linear interpolation applied by me).   */
    private final double[] crossCount = new double[]{   /* [0..49] */
            1.0, 1.0, 1.0, 1.0828, 1.1536, 1.2206, 1.2823, 1.3385, 1.3991, 1.4493, 1.4974, 1.5455, 1.5937, 1.6418,
            1.6899, 1.7304, 1.7709, 1.8114, 1.8519, 1.8924, 1.9288, 1.9652, 2.0015, 2.0379, 2.0743, 2.1061, 2.1379,
            2.1698, 2.2016, 2.2334, 2.2646, 2.2958, 2.3271, 2.3583, 2.3895, 2.4187, 2.4479, 2.4772, 2.5064, 2.5356,
            2.5610, 2.5864, 2.6117, 2.6371, 2.6625, 2.6887, 2.7148, 2.7410, 2.7671, 2.7933};

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
            crossings = crossCount[connectedPads.size() - 1];
        }
        return crossings;
    }

    /**
     * Calculates the bb-costs of the net
     * @return
     */
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

    /**
     * Determines the bounding box of the net.
     *
     * @return Pair of two Positions, Left minimal, right max
     */
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
