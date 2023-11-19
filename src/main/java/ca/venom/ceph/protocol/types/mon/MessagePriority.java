package ca.venom.ceph.protocol.types.mon;

public enum MessagePriority {
    CEPH_MSG_PRIO_LOW(64),
    CEPH_MSG_PRIO_DEFAULT(127),
    CEPH_MSG_PRIO_HIGH(196),
    CEPH_MSG_PRIO_HIGHEST(255);

    private int value;

    MessagePriority(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static MessagePriority getFromValue(int value) {
        for (MessagePriority item : values()) {
            if (item.value == value) {
                return item;
            }
        }

        return null;
    }
}
