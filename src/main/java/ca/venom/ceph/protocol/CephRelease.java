package ca.venom.ceph.protocol;

import ca.venom.ceph.EnumWithIntValue;

public enum CephRelease implements EnumWithIntValue {
    UNKNOWN(0),
    ARGONAUT(1),
    BOBTAIL(2),
    CUTTLEFISH(3),
    DUMPLING(4),
    EMPEROR(5),
    FIREFLY(6),
    GIANT(7),
    HAMMER(8),
    INFERNALIS(9),
    JEWEL(10),
    KRAKEN(11),
    LUMINOUS(12),
    MIMIC(13),
    NAUTILUS(14),
    OCTOPUS(15),
    PACIFIC(16),
    QINCY(17),
    MAX(18);

    private int value;

    CephRelease(int value) {
        this.value = value;
    }

    public static CephRelease getFromValueInt(int value) {
        for (CephRelease release : values()) {
            if (release.value == value) {
                return release;
            }
        }

        return null;
    }

    @Override
    public int getValueInt() {
        return value;
    }
}
