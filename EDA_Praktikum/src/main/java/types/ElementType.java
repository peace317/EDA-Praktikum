package types;

public enum ElementType {
    INPUT(".input"), OUTPUT(".output"), CLB(".clb");
    private final String typeName;

    ElementType(String typeName) {
        this.typeName = typeName;
    }

    @Override
    public String toString() {
        return typeName;
    }
}