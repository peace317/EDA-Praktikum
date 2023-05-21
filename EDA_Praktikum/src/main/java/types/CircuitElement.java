package types;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CircuitElement {

    private final static Integer MAX_RECENT_POS = 2;
    private final ElementType type;
    private final String blockName;
    private final Integer blockNumber;
    private final NetClass pinList;
    private final String subBlockName;
    private final List<String> subblock;

    private final LinkedList<Position> recentPositions;
    private Position position;
    private Integer subblockNumber;
    private int weight;

    public CircuitElement(ElementType type, String blockname, Integer blockNumber, NetClass pinList) {
        this(type, blockname, blockNumber, pinList, null, Collections.emptyList());
    }

    public CircuitElement(ElementType type, String blockname, Integer blockNumber, NetClass pinList,
                          String subBlockName, List<String> subblock) {
        this.type = type;
        this.blockName = blockname;
        this.blockNumber = blockNumber;
        this.pinList = pinList;
        this.subBlockName = subBlockName;
        this.subblock = subblock;
        this.recentPositions = new LinkedList<>();
        this.position = null;
        this.weight = 1;
        this.subblockNumber = 0;
        pinList.addBlock(this);
    }

    public ElementType getType() {

        return this.type;
    }

    public String getBlockName() {

        return this.blockName;
    }

    public List<Net> getPinList() {

        return pinList.collectIONets();
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

    public void calcWeight() {
        weight = pinList.calcWeight();
    }

    public void setPosition(Position pos) {
        recentPositions.add(pos);
        if (recentPositions.size() > MAX_RECENT_POS)
            recentPositions.removeFirst();
        pinList.invalidateNetCosts();
        position = pos;
    }

    public Position getPosition() {
        return position;
    }

    public String getSubBlockName() {
        return subBlockName;
    }

    public Integer getBlockNumber() {
        return blockNumber;
    }

    public boolean knownPosition(Position pos) {
        return recentPositions.contains(pos);
    }

    public Integer getSubblockNumber() {
        return subblockNumber;
    }

    public void setSubblockNumber(Integer subblockNumber) {
        this.subblockNumber = subblockNumber;
    }

    public double calcCosts() {
        return pinList.calcCosts();
    }

}