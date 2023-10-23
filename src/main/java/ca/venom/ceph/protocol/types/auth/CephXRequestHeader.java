package ca.venom.ceph.protocol.types.auth;

import ca.venom.ceph.protocol.types.CephDataType;
import ca.venom.ceph.protocol.types.Int16;
import io.netty.buffer.ByteBuf;

public class CephXRequestHeader implements CephDataType {
    private Int16 requestType;

    public Int16 getRequestType() {
        return requestType;
    }

    public void setRequestType(Int16 requestType) {
        this.requestType = requestType;
    }

    @Override
    public int getSize() {
        return 2;
    }

    @Override
    public void encode(ByteBuf byteBuf, boolean le) {
        requestType.encode(byteBuf, le);
    }

    @Override
    public void decode(ByteBuf byteBuf, boolean le) {
        requestType = new Int16();
        requestType.decode(byteBuf, le);
    }
}
