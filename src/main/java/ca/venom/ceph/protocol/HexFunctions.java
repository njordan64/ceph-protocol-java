package ca.venom.ceph.protocol;

public class HexFunctions {
    private static final char[] HEX_CHARS = {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    public static short readUInt16(byte[] bytes, int start) {
        return (short) (((bytes[start + 1] << 8) & 65280) |
               (bytes[start] & 255));
               
    }
    
    public static int readUInt32(byte[] bytes, int start) {
        return ((bytes[start + 3] << 24) & -16777216) |
               ((bytes[start + 2] << 16) & 16711680) |
               ((bytes[start + 1] << 8) & 65280) |
               (bytes[start] & 255);
    }

    public static String hexToString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < bytes.length; i++) {
            if (i > 0 && i % 16 == 0) {
                sb.append("\n");
            } else if (i % 16 == 8) {
                sb.append("    ");
            } else if (i > 0) {
                sb.append(" ");
            }

            char highChar = HEX_CHARS[(bytes[i] & 0xf0) >> 4];
            char lowChar = HEX_CHARS[bytes[i] & 0x0f];

            sb.append(highChar);
            sb.append(lowChar);
        }

        return sb.toString();
    }

    public static void printHexString(byte[] bytes) {
        System.out.println(hexToString(bytes));
    }
}
