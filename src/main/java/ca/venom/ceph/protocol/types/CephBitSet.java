package ca.venom.ceph.protocol.types;

import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;
import java.util.BitSet;

public class CephBitSet implements CephDataType {
    private BitSet value;
    private int byteCount;

    public CephBitSet(int byteCount) {
        this.byteCount = byteCount;
    }

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

    @Override
    public int getSize() {
        return byteCount;
    }

    @Override
    public void encode(ByteBuf byteBuf, boolean le) {
        byte[] bytes = new byte[byteCount];
        byte[] bitsetBytes = value.toByteArray();

        if (le) {
            System.arraycopy(bitsetBytes, 0, bytes, 0, Math.min(byteCount, bitsetBytes.length));
        } else {
            for (int i = 0; i < Math.min(byteCount, bitsetBytes.length); i++) {
                bytes[byteCount - i - 1] = bitsetBytes[i];
            }
        }

        byteBuf.writeBytes(bytes);
    }

    @Override
    public void decode(ByteBuf byteBuf, boolean le) {
        byte[] bytes = new byte[byteCount];

        if (le) {
            byteBuf.readBytes(bytes);
        } else {
            for (int i = byteCount - 1; i >= 0; i--) {
                bytes[i] = byteBuf.readByte();
            }
        }

        value = BitSet.valueOf(bytes);
    }
}
