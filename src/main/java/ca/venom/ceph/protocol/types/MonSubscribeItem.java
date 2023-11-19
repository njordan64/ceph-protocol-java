package ca.venom.ceph.protocol.types;

import io.netty.buffer.ByteBuf;

import java.util.BitSet;

public class MonSubscribeItem implements CephDataType {
    private Int64 start;
    private CephBitSet flags;

    public long getStart() {
        return start.getValue();
    }

    public void setStart(long start) {
        this.start = new Int64(start);
    }

    public BitSet getFlags() {
        return flags.getValue();
    }

    public void setFlags(BitSet flags) {
        this.flags = new CephBitSet(flags, 1);
    }

    @Override
    public int getSize() {
        return 9;
    }

    @Override
    public void encode(ByteBuf byteBuf, boolean le) {
        start.encode(byteBuf, le);
        flags.encode(byteBuf, le);
    }

    @Override
    public void decode(ByteBuf byteBuf, boolean le) {
        start = new Int64();
        start.decode(byteBuf, le);
        flags = new CephBitSet(1);
        flags.decode(byteBuf, le);
    }
}
