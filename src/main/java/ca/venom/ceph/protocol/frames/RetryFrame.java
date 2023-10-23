package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.Int64;
import io.netty.buffer.ByteBuf;

public class RetryFrame extends ControlFrame {
    private Int64 connectionSeq;

    public Int64 getConnectionSeq() {
        return connectionSeq;
    }

    public void setConnectionSeq(Int64 connectionSeq) {
        this.connectionSeq = connectionSeq;
    }

    @Override
    public void encodeSegment1(ByteBuf byteBuf, boolean le) {
        connectionSeq.encode(byteBuf, le);
    }

    @Override
    public void decodeSegment1(ByteBuf byteBuf, boolean le) {
        connectionSeq = new Int64();
        connectionSeq.decode(byteBuf, le);
    }

    @Override
    public MessageType getTag() {
        return MessageType.SESSION_RETRY;
    }
}
