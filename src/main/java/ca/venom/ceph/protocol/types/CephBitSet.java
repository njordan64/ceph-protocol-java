package ca.venom.ceph.protocol.types;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.BitSet;

public class CephBitSet implements CephDataType {
    private BitSet value;
    private int byteCount;

    public CephBitSet(BitSet value, int byteCount) {
        this.value = value;
        this.byteCount = byteCount;
    }

    public static CephBitSet read(ByteBuffer byteBuffer, int byteCount) {
        byte[] bytes = new byte[byteCount];
        byteBuffer.get(bytes);
        return new CephBitSet(BitSet.valueOf(bytes), byteCount);
    }

    public BitSet getValue() {
        return value;
    }

    public void setValue(BitSet value) {
        this.value = value;
    }

    public int getByteCount() {
        return byteCount;
    }

    public void setByteCount(int byteCount) {
        this.byteCount = byteCount;
    }

    @Override
    public int getSize() {
        return byteCount;
    }

    @Override
    public void encode(ByteArrayOutputStream outputStream) {
        byte[] bytes = new byte[byteCount];
        byte[] bitsetBytes = value.toByteArray();
        System.arraycopy(bitsetBytes, 0, bytes, 0, Math.min(byteCount, bitsetBytes.length));
        outputStream.writeBytes(bytes);
    }

    @Override
    public void encode(ByteBuffer byteBuffer) {
        byte[] bytes = new byte[byteCount];
        byte[] bitsetBytes = value.toByteArray();
        System.arraycopy(bitsetBytes, 0, bytes, 0, Math.min(byteCount, bitsetBytes.length));
        byteBuffer.put(bytes);
    }
}
