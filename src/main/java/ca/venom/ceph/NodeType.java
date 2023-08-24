package ca.venom.ceph;

public enum NodeType {
    MON(0x01),
    MDS(0x02),
    OSD(0x04),
    CLIENT(0x08),
    MGR(0x10),
    AUTH(0x20),
    ANY(0xFF);

    private int typeNum;

    private NodeType(int typeNum) {
        this.typeNum = typeNum;
    }

    public int getTypeNum() {
        return typeNum;
    }

    public static NodeType getFromTypeNum(int typeNum) {
        for (NodeType nodeType : values()) {
            if (nodeType.typeNum == typeNum) {
                return nodeType;
            }
        }

        return null;
    }
}
