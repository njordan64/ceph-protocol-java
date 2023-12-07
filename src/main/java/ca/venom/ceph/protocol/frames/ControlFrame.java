package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.ControlFrameType;
import ca.venom.ceph.protocol.DecodingException;
import ca.venom.ceph.protocol.types.EncodingException;
import io.netty.buffer.ByteBuf;

public abstract class ControlFrame {
    public abstract ControlFrameType getTag();

    public abstract void encodeSegment1(ByteBuf byteBuf, boolean le) throws EncodingException;

    public void encodeSegment2(ByteBuf byteBuf, boolean le) throws EncodingException {
    }

    public void encodeSegment3(ByteBuf byteBuf, boolean le) throws EncodingException {
    }

    public void encodeSegment4(ByteBuf byteBuf, boolean le) throws EncodingException {
    }

    public abstract void decodeSegment1(ByteBuf byteBuf, boolean le) throws DecodingException;

    public void decodeSegment2(ByteBuf byteBuf, boolean le) throws DecodingException {
    }

    public void decodeSegment3(ByteBuf byteBuf, boolean le) throws DecodingException {
    }

    public void decodeSegment4(ByteBuf byteBuf, boolean le) throws DecodingException {
    }
}
