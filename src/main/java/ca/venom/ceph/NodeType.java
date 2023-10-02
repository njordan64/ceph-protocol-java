package ca.venom.ceph;

public enum NodeType implements TypeNumEnum {
    MON(0x01),
    MDS(0x02),
    OSD(0x04),
    CLIENT(0x08),
    MGR(0x10),
    AUTH(0x20),
    ANY(0xFF);

    private final int valueInt;

    private NodeType(int valueInt) {
        this.valueInt = valueInt;
    }

    public int getValueInt() {
        return valueInt;
    }

    public static NodeType getFromTypeNum(int valueInt) {
        for (NodeType nodeType : values()) {
            if (nodeType.valueInt == valueInt) {
                return nodeType;
            }
        }

        return null;
    }
}
