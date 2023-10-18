package ca.venom.ceph.protocol;

public class CephProtocolContext {
    public enum SecureMode {
        CRC(1),
        SECURE(2);

        private int modeNum;

        SecureMode(int modeNum) {
            this.modeNum = modeNum;
        }

        public int getModeNum() {
            return modeNum;
        }

        public static SecureMode fromModeNum(int modeNum) {
            for (SecureMode mode : values()) {
                if (modeNum == mode.getModeNum()) {
                    return mode;
                }
            }

            return null;
        }
    }

    private boolean rev1;
    private SecureMode secureMode;

    public boolean isRev1() {
        return rev1;
    }

    public void setRev1(boolean rev1) {
        this.rev1 = rev1;
    }

    public SecureMode getSecureMode() {
        return secureMode;
    }

    public void setSecureMode(SecureMode secureMode) {
        this.secureMode = secureMode;
    }
}
