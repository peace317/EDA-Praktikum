package types;

public enum ClassType {
    INPUT("input"), OUTPUT("output"), GLOBAL("global");
    private final String classType;

    ClassType(String typeName) {
        this.classType = typeName;
    }

    @Override
    public String toString() {
        return classType;
    }
}