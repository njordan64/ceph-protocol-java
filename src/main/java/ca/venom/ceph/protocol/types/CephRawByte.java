package ca.venom.ceph.protocol.types;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class CephRawByte implements CephDataType {
    private byte value;

    public CephRawByte(byte value) {
        this.value = value;
    }

    public static CephRawByte read(ByteBuffer byteBuffer) {
        return new CephRawByte(byteBuffer.get());
    }

    public byte getValue() {
        return value;
    }

    public void setValue(byte value) {
        this.value = value;
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public void encode(ByteArrayOutputStream outputStream) {
        outputStream.write(0xff & value);
    }

    @Override
    public void encode(ByteBuffer byteBuffer) {
        byteBuffer.put(value);
    }
}
