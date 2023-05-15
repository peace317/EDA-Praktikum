package types;

public class Architecture {

    private Integer subblocksPerClb;
    private Integer subblockLutSize;
    private Integer ioRate;

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
}
