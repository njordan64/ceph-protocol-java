package ca.venom.ceph.protocol.types;

import io.netty.buffer.ByteBuf;

import java.util.List;

public class AddrVec implements CephDataType {
    private static final byte VERSION = 2;

    private CephList<Addr> v;

    public AddrVec() {
    }

    public AddrVec(List<Addr> v) {
        this.v = new CephList<>(v, Addr.class);
    }

    public CephList<Addr> getV() {
        return v;
    }

    public void setV(CephList<Addr> v) {
        this.v = v;
    }

    @Override
    public int getSize() {
        return 1 + v.getSize();
    }

    @Override
    public void encode(ByteBuf byteBuf, boolean le) {
        byteBuf.writeByte(2);
        v.encode(byteBuf, le);
    }

    @Override
    public void decode(ByteBuf byteBuf, boolean le) {
        byte version = byteBuf.readByte();
        v = new CephList<>(Addr.class);
        v.decode(byteBuf, le);
    }
}
