package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.CephBoolean;
import ca.venom.ceph.protocol.types.Int32;
import io.netty.buffer.ByteBuf;

public class CompressionDoneFrame extends ControlFrame {
    private CephBoolean compress;
    private Int32 method;

    public boolean isCompress() {
        return compress.getValue();
    }

    public void setCompress(boolean compress) {
        this.compress = new CephBoolean(compress);
    }

    public Int32 getMethod() {
        return method;
    }

    public void setMethod(Int32 method) {
        this.method = method;
    }

    @Override
    public void encodeSegment1(ByteBuf byteBuf, boolean le) {
        compress.encode(byteBuf, le);
        method.encode(byteBuf, le);
    }

    @Override
    public void decodeSegment1(ByteBuf byteBuf, boolean le) {
        compress = new CephBoolean();
        compress.decode(byteBuf, le);

        method = new Int32();
        method.decode(byteBuf, le);
    }

    @Override
    public MessageType getTag() {
        return MessageType.COMPRESSION_DONE;
    }
}
