package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.CephDecoder;
import ca.venom.ceph.protocol.CephEncoder;
import ca.venom.ceph.protocol.ControlFrameType;
import ca.venom.ceph.protocol.DecodingException;
import ca.venom.ceph.protocol.types.annotations.CephField;
import ca.venom.ceph.protocol.types.annotations.CephType;
import ca.venom.ceph.protocol.types.Addr;
import ca.venom.ceph.protocol.types.EncodingException;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class ReconnectFrame extends ControlFrame {
    @CephType
    public static class Segment {
        @Getter
        @Setter
        @CephField
        private List<Addr> myAddresses;

        @Getter
        @Setter
        @CephField(order = 2)
        private long clientCookie;

        @Getter
        @Setter
        @CephField(order = 3)
        private long serverCookie;

        @Getter
        @Setter
        @CephField(order = 4)
        private long globalSeq;

        @Getter
        @Setter
        @CephField(order = 5)
        private long connectSeq;

        @Getter
        @Setter
        @CephField(order = 6)
        private long messageSeq;
    }

    @Getter
    @Setter
    private Segment segment1;

    @Override
    public void encodeSegment1(ByteBuf byteBuf, boolean le) throws EncodingException {
        CephEncoder.encode(segment1, byteBuf, le);
    }

    @Override
    public void decodeSegment1(ByteBuf byteBuf, boolean le) throws DecodingException {
        segment1 = CephDecoder.decode(byteBuf, le, Segment.class);
    }

    @Override
    public ControlFrameType getTag() {
        return ControlFrameType.SESSION_RECONNECT;
    }
}
