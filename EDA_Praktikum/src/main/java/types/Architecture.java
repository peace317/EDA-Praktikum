package types;

import java.util.ArrayList;
import java.util.List;

public class Architecture {

    private Integer subblocksPerClb;
    private Integer subblockLutSize;
    private Integer ioRate;
    private List<ClassType> ioClasses;

    public void setSubblocksPerClb(Integer subblocksPerClb) {
        this.subblocksPerClb = subblocksPerClb;
    }

    public Integer getSubblocksPerClb() {
        return subblocksPerClb;
    }

    public void setSubblockLutSize(Integer subblockLutSize) {
        this.subblockLutSize = subblockLutSize;
    }

    public Integer getSubblockLutSize() {
        return subblockLutSize;
    }

    public void setIoRate(Integer ioRate) {
        this.ioRate = ioRate;
    }

    public Integer getIoRate() {
        return ioRate;
    }

    public List<ClassType> getIoClasses() {
        return ioClasses;
    }

    public void setIoClasses(List<ClassType> ioClasses) {
        this.ioClasses = ioClasses;
    }
}
