package ca.venom.ceph.protocol.types;

import io.netty.buffer.ByteBuf;

public class CephEntityName implements CephDataType {
    private Int8 type;
    private Int64 num;

    public byte getType() {
        return type.getValue();
    }

    public void setType(byte type) {
        this.type = new Int8(type);
    }

    public long getNum() {
        return num.getValue();
    }

    public void setNum(long num) {
        this.num = new Int64(num);
    }

    @Override
    public int getSize() {
        return 7;
    }

    @Override
    public void encode(ByteBuf byteBuf, boolean le) {
        type.encode(byteBuf, le);
        num.encode(byteBuf, le);
    }

    @Override
    public void decode(ByteBuf byteBuf, boolean le) {
        type = new Int8();
        type.decode(byteBuf, le);
        num = new Int64();
        num.decode(byteBuf, le);
    }
}
