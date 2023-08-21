package ca.venom.ceph.protocol.types;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class CString {
    private final String value;

    public CString(ByteBuffer byteBuffer) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        while (true) {
            byte b = byteBuffer.get();
            if (b == 0) {
                break;
            }

            baos.write(b);
        }

        byte[] bytes = baos.toByteArray();
        value = new String(bytes);
    }

    private CString(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static CString fromValue(String value) {
        return new CString(value);
    }

    public void encode(ByteBuffer byteBuffer) {
        for (int i = 0; i < value.length(); i++) {
            byteBuffer.put((byte) value.codePointAt(i));
        }
        byteBuffer.put((byte) 0);
    }

    public boolean equals(Object obj) {
        if (obj instanceof CString) {
            return value.equals(((CString) obj).value);
        }

        return false;
    }

    public int hashCode() {
        return value.hashCode();
    }
}
