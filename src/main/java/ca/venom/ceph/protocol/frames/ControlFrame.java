package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.ControlFrameType;
import io.netty.buffer.ByteBuf;

public abstract class ControlFrame {
    public abstract ControlFrameType getTag();

    public abstract void encodeSegment1(ByteBuf byteBuf, boolean le);

    public void encodeSegment2(ByteBuf byteBuf, boolean le) {
    }

    public void encodeSegment3(ByteBuf byteBuf, boolean le) {
    }

    public void encodeSegment4(ByteBuf byteBuf, boolean le) {
    }

    public abstract void decodeSegment1(ByteBuf byteBuf, boolean le);

    public void decodeSegment2(ByteBuf byteBuf, boolean le) {
    }

    public void decodeSegment3(ByteBuf byteBuf, boolean le) {
    }

    public void decodeSegment4(ByteBuf byteBuf, boolean le) {
    }
}
