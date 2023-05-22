package benchmark;

public class Stats {
    private String minChanelWidth;
    private String critPath;
    private String costs;
    private String placeRuntime;
    private String vprRuntime;

    public Stats() {
    }

    public String getSwitchAmount() {
        return switchAmount;
    }

    public void setSwitchAmount(String switchAmount) {
        this.switchAmount = switchAmount;
    }

    private String switchAmount;


    public String getMinChanelWidth() {
        return minChanelWidth;
    }

    public void setMinChanelWidth(String minChanelWidth) {
        this.minChanelWidth = minChanelWidth;
    }

    public String getCritPath() {
        return critPath;
    }

    public void setCritPath(String critPath) {
        this.critPath = critPath;
    }

    public String getCosts() {
        return costs;
    }

    public void setCosts(String costs) {
        this.costs = costs;
    }

    public String getPlaceRuntime() {
        return placeRuntime;
    }

    public void setPlaceRuntime(String runtime) {
        this.placeRuntime = runtime;
    }

    public String getVprRuntime() {
        return vprRuntime;
    }

    public void setVprRuntime(String runtime) {
        this.vprRuntime = runtime;
    }

    @Override
    public String toString() {
        return "Stats{" + "minChanelWidth='" + minChanelWidth + '\'' + ", critPath='" + critPath + '\'' + ", costs='" + costs + '\'' + ", placeRuntime='" + placeRuntime + '\'' + ", vprRuntime='" + vprRuntime + '\'' + ", switchAmount='" + switchAmount + '\'' + '}';
    }
}
