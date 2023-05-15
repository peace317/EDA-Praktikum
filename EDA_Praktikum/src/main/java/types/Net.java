package types;

import java.util.ArrayList;
import java.util.List;

public class Net {

    private final String name;

    private final List<CircuitElement> connectedPads;

    public Net(String name) {
        this.name = name;
        connectedPads = new ArrayList<>();
    }

    public void addPad(CircuitElement pad) {
        if (connectedPads.contains(pad))
            throw new IllegalStateException("Pad '" + pad.getBlockName() + "' already assigned to net. Only one pin " +
                    "can be connected to a pad!");
        connectedPads.add(pad);
    }

    public List<CircuitElement> getConnectedPads() {
        return connectedPads;
    }

    public double getCosts() {
        double sum = 0;
        for (int i = 0; i < connectedPads.size() - 1; i++) {
            for (int j = i + 1; j < connectedPads.size(); j++) {
                sum += connectedPads.get(i).getPosition().rectilinear(connectedPads.get(j).getPosition());
            }
        }
        return sum;
    }
}
