package types;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NetClass {

    private final List<Net> inPins = new ArrayList<>();
    private final List<Net> outPins = new ArrayList<>();
    private final List<Net> globalPins = new ArrayList<>();

    public void add(Net net, ClassType classType) {
        switch (classType) {
            case INPUT -> inPins.add(net);
            case OUTPUT -> outPins.add(net);
            case GLOBAL -> globalPins.add(net);
        }
    }

    public void addBlock(CircuitElement circuitElement) {
        for (Net net : collectAllNets()) {
            net.addPad(circuitElement);
        }
    }

    public int calcWeight() {
        int weight = 1;
        for (Net net : collectAllNets()) {
            weight += net.getConnectedPads().size() - 1;
        }
        return weight;
    }

    public double calcCosts() {
        double sum = 0;
        for (Net net : collectIONets()) {
            sum += net.calcCosts();
        }
        return sum;
    }

    public void invalidateNetCosts() {
        for (Net net : collectAllNets()) {
            net.invalidateCosts();
        }
    }

    public List<Net> collectIONets() {
        return Stream.concat(inPins.stream(), outPins.stream()).collect(Collectors.toList());
    }

    public List<Net> collectAllNets() {
        return Stream.concat(Stream.concat(inPins.stream(), outPins.stream()), globalPins.stream()).collect(Collectors.toList());
    }


}
