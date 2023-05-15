package types;

import types.Net;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CircuitElement {
    private final ElementType type;
    private final String blockName;
    private final Integer blockNumber;
    private final List<Net> pinList;
    private final String subBlockName;
    private final List<String> subblock;

    private final List<Position> rescentPositions;
    private Position position;
    private int weight;

    public CircuitElement(ElementType type, String blockname, Integer blockNumber, List<Net> pinList) {
        this(type, blockname, blockNumber, pinList, null, Collections.emptyList());
    }

    public CircuitElement(ElementType type, String blockname, Integer blockNumber, List<Net> pinList,
                          String subBlockName, List<String> subblock) {
        this.type = type;
        this.blockName = blockname;
        this.blockNumber = blockNumber;
        this.pinList = pinList;
        this.subBlockName = subBlockName;
        this.subblock = subblock;
        this.rescentPositions = new ArrayList<>();
        this.position = null;
        this.weight = 1;
        for (Net net : pinList) {
            net.addPad(this);
        }
    }

    public ElementType getType() {

        return this.type;
    }

    public String getBlockName() {

        return this.blockName;
    }

    public List<Net> getPinList() {

        return this.pinList;
    }

    public List<String> getSubblock() {

        return this.subblock;
    }

    public int getX() {
        return this.position.getX();
    }

    public int getY() {
        return this.position.getY();
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public void setPosition(Position pos) {
        rescentPositions.add(pos);
        position = pos;
    }

    public String getSubBlockName() {
        return subBlockName;
    }

    public Integer getBlockNumber() {
        return blockNumber;
    }

    public boolean knownPosition(Position pos) {
        return rescentPositions.contains(pos);
    }
}