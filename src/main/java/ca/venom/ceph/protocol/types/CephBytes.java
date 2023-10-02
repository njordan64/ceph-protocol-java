package ca.venom.ceph.protocol.types;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class CephBytes implements CephDataType {
    private byte[] bytes;

    public CephBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public static CephBytes read(ByteBuffer byteBuffer) {
        UInt32 length = UInt32.read(byteBuffer);
        byte[] bytes = new byte[(int) length.getValue()];
        byteBuffer.get(bytes);

        return new CephBytes(bytes);
    }

    public byte[] getValue() {
        return bytes;
    }

    @Override
    public int getSize() {
        return 4 + bytes.length;
    }

    @Override
    public void encode(ByteArrayOutputStream outputStream) {
        new UInt32(bytes.length).encode(outputStream);
        outputStream.writeBytes(bytes);
    }

    @Override
    public void encode(ByteBuffer byteBuffer) {
        new UInt32(bytes.length).encode(byteBuffer);
        byteBuffer.put(bytes);
    }
}
