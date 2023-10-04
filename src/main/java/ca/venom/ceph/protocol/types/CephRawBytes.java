package ca.venom.ceph.protocol.types;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class CephRawBytes implements CephDataType {
    private byte[] bytes;

    public CephRawBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public static CephRawBytes read(ByteBuffer byteBuffer, int byteCount) {
        byte[] bytes = new byte[byteCount];
        byteBuffer.get(bytes);

        return new CephRawBytes(bytes);
    }

    public byte[] getValue() {
        return bytes;
    }

    @Override
    public int getSize() {
        return bytes.length;
    }

    @Override
    public void encode(ByteArrayOutputStream outputStream) {
        outputStream.writeBytes(bytes);
    }

    @Override
    public void encode(ByteBuffer byteBuffer) {
        byteBuffer.put(bytes);
    }
}
