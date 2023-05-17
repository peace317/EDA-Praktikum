package gui;

public interface RouteEvent {

    public void placementConsistencyCheck(boolean successful);
    public void netDelayValueCrossCheck(boolean successful);
    public void routingConsistencyCheck(boolean successful);

}
