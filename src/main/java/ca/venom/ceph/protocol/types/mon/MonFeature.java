package ca.venom.ceph.protocol.types.mon;

import ca.venom.ceph.protocol.types.CephBitSet;
import ca.venom.ceph.protocol.types.CephDataType;
import io.netty.buffer.ByteBuf;

import java.util.BitSet;

public class MonFeature implements CephDataType {
    private static final byte HEAD_VERSION = 1;
    private static final byte COMPAT_VERSION = 1;

    private CephBitSet features;

    public MonFeature() {
    }

    public MonFeature(BitSet features) {
        this.features = new CephBitSet(features, 8);
    }

    public BitSet getFeatures() {
        return features.getValue();
    }

    public void setFeatures(BitSet features) {
        this.features = new CephBitSet(features, 8);
    }

    @Override
    public int getSize() {
        return 14;
    }

    @Override
    public void encode(ByteBuf byteBuf, boolean le) {
        byteBuf.writeByte(HEAD_VERSION);
        byteBuf.writeByte(COMPAT_VERSION);

        if (le) {
            byteBuf.writeLongLE(14);
        } else {
            byteBuf.writeInt(14);
        }

        features.encode(byteBuf, le);
    }

    @Override
    public void decode(ByteBuf byteBuf, boolean le) {
        byte version = byteBuf.readByte();
        byte compatVersion = byteBuf.readByte();
        int length = le ? byteBuf.readIntLE() : byteBuf.readInt();

        features = new CephBitSet(8);
        features.decode(byteBuf, le);
    }
}
