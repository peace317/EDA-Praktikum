package gui;

public class VPRListener {

    private RouteEvent event;

    public VPRListener(RouteEvent event) {
        this.event = event;
    }

    public void accept(String line) {
        checkConsistency(line);
        if (line.contains("Error in")) {
            event.placementConsistencyCheck(false);
            event.netDelayValueCrossCheck(false);
            event.routingConsistencyCheck(false);
        }

    }

    private void checkConsistency(String line) {
        if (line.contains("placement consistency check")) {
            event.placementConsistencyCheck(line.contains("Completed placement consistency check successfully"));
        }
        if (line.contains("net delay value cross check")) {
            event.netDelayValueCrossCheck(line.contains("Completed net delay value cross check successfully"));
        }
        if (line.contains("routing consistency check")) {
            event.routingConsistencyCheck(line.contains("Completed routing consistency check successfully"));
        }
    }
}
