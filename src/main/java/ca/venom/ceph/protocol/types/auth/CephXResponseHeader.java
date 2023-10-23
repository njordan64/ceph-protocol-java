package ca.venom.ceph.protocol.types.auth;

import ca.venom.ceph.protocol.types.CephDataType;
import ca.venom.ceph.protocol.types.Int16;
import ca.venom.ceph.protocol.types.Int32;
import io.netty.buffer.ByteBuf;

public class CephXResponseHeader implements CephDataType {
    private Int16 responseType;
    private Int32 status;

    public Int16 getResponseType() {
        return responseType;
    }

    public void setResponseType(Int16 responseType) {
        this.responseType = responseType;
    }

    public Int32 getStatus() {
        return status;
    }

    public void setStatus(Int32 status) {
        this.status = status;
    }

    @Override
    public int getSize() {
        return 6;
    }

    @Override
    public void encode(ByteBuf byteBuf, boolean le) {
        responseType.encode(byteBuf, le);
        status.encode(byteBuf, le);
    }

    @Override
    public void decode(ByteBuf byteBuf, boolean le) {
        responseType = new Int16();
        responseType.decode(byteBuf, le);

        status = new Int32();
        status.decode(byteBuf, le);
    }
}
