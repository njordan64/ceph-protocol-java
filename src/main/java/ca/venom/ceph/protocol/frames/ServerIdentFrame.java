package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.CephDecoder;
import ca.venom.ceph.protocol.CephEncoder;
import ca.venom.ceph.protocol.ControlFrameType;
import ca.venom.ceph.protocol.DecodingException;
import ca.venom.ceph.protocol.types.AddrVec;
import ca.venom.ceph.protocol.types.annotations.CephEncodingSize;
import ca.venom.ceph.protocol.types.annotations.CephField;
import ca.venom.ceph.protocol.types.annotations.CephType;
import ca.venom.ceph.protocol.types.Addr;
import ca.venom.ceph.protocol.types.EncodingException;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

import java.util.BitSet;
import java.util.List;

public class ServerIdentFrame extends ControlFrame {
    @CephType
    public static class Segment1 {
        @Getter
        @Setter
        @CephField
        private AddrVec myAddresses;

        @Getter
        @Setter
        @CephField(order = 2)
        private long globalId;

        @Getter
        @Setter
        @CephField(order = 3)
        private long globalSeq;

        @Getter
        @Setter
        @CephField(order = 4)
        @CephEncodingSize(8)
        private BitSet supportedFeatures;

        @Getter
        @Setter
        @CephField(order = 5)
        @CephEncodingSize(8)
        private BitSet requiredFeatures;

        @Getter
        @Setter
        @CephField(order = 6)
        @CephEncodingSize(8)
        private BitSet flags;

        @Getter
        @Setter
        @CephField(order = 7)
        private long clientCookie;
    }

    @Getter
    @Setter
    private Segment1 segment1;

    @Override
    public void encodeSegment1(ByteBuf byteBuf, boolean le) throws EncodingException {
        CephEncoder.encode(segment1, byteBuf, le);
    }

    @Override
    public void decodeSegment1(ByteBuf byteBuf, boolean le) throws DecodingException {
        segment1 = CephDecoder.decode(byteBuf, le, Segment1.class);
    }

    @Override
    public ControlFrameType getTag() {
        return ControlFrameType.SERVER_IDENT;
    }
}
