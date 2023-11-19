package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.ControlFrameType;
import ca.venom.ceph.protocol.types.UTime;
import io.netty.buffer.ByteBuf;

public class KeepAliveAck extends ControlFrame {
    private UTime timestamp;

    public UTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(UTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public void encodeSegment1(ByteBuf byteBuf, boolean le) {
        timestamp.encode(byteBuf, le);
    }

    @Override
    public void decodeSegment1(ByteBuf byteBuf, boolean le) {
        timestamp = new UTime();
        timestamp.decode(byteBuf, le);
    }

    @Override
    public ControlFrameType getTag() {
        return ControlFrameType.KEEPALIVE2;
    }
}
