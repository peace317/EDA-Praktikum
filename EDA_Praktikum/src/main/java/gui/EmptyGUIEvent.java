package gui;

public class EmptyGUIEvent implements BlockEvent, RouteEvent, PlacementEvent{

    @Override
    public void blockUI() {

    }

    @Override
    public void freeUI() {

    }

    @Override
    public void placementConsistencyCheck(boolean successful) {

    }

    @Override
    public void netDelayValueCrossCheck(boolean successful) {

    }

    @Override
    public void routingConsistencyCheck(boolean successful) {

    }

    @Override
    public void generating(boolean successful) {

    }
}
