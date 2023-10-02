package ca.venom.ceph.protocol.types;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class CephBoolean implements CephDataType {
    private boolean value;

    public CephBoolean(boolean value) {
        this.value = value;
    }

    public static CephBoolean read(ByteBuffer byteBuffer) {
        return new CephBoolean(byteBuffer.get() != 0);
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public void encode(ByteArrayOutputStream outputStream) {
        outputStream.write(value ? 1 : 0);
    }

    @Override
    public void encode(ByteBuffer byteBuffer) {
        byteBuffer.put(value ? (byte) 1 : (byte) 0);
    }
}
