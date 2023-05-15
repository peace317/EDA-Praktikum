package types;

public class Architecture {

    private Integer subblocksPerClb;
    private Integer subblockLutSize;
    
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
}
