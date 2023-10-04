package ca.venom.ceph;

public enum AuthMode implements EnumWithIntValue {
    NONE(0),
    AUTHORIZER(1),
    AUTHORIZER_MAX(9),
    MON(10),
    MON_MAX(19);

    private final int valueInt;

    private AuthMode(int valueInt) {
        this.valueInt = valueInt;
    }

    public int getValueInt() {
        return valueInt;
    }

    public static AuthMode getFromTypeNum(int valueInt) {
        for (AuthMode authMode : values()) {
            if (authMode.valueInt == valueInt) {
                return authMode;
            }
        }

        return null;
    }
}
