package ca.venom.ceph.protocol.codecs;

import ca.venom.ceph.CephCRC32C;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;

public class ChecksumCodec extends MessageToMessageCodec<CephPreParsedFrame, CephPreParsedFrame> {
    @Override
    protected void encode(ChannelHandlerContext ctx, CephPreParsedFrame frame, List<Object> list) throws Exception {
        frame.setNeedHeaderChecksum(true);
        if (frame.getSegment1() != null) {
            updateSegmentChecksum(frame.getSegment1());
        }
        if (frame.getSegment2() != null) {
            updateSegmentChecksum(frame.getSegment2());
        }
        if (frame.getSegment3() != null) {
            updateSegmentChecksum(frame.getSegment3());
        }
        if (frame.getSegment4() != null) {
            updateSegmentChecksum(frame.getSegment4());
        }

        list.add(frame);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, CephPreParsedFrame frame, List<Object> list) throws Exception {
        ByteBuf headerByteBuf = frame.getHeaderByteBuf();
        byte[] crc32cBytes = new byte[8];
        headerByteBuf.getBytes(28, crc32cBytes, 0, 4);
        long crc32cValue = headerByteBuf.readLongLE();

        CephCRC32C crc32C = new CephCRC32C(0L);
        crc32C.update(headerByteBuf.array(), headerByteBuf.arrayOffset(), 28);

        if (crc32cValue == crc32C.getValue()) {
            list.add(frame);
        } else {
            throw new Exception("Header checksum validation failed");
        }

        validateSegmentChecksum(frame.getSegment1());
        if (frame.getSegment2() != null) {
            validateSegmentChecksum(frame.getSegment2());
        }
        if (frame.getSegment3() != null) {
            validateSegmentChecksum(frame.getSegment3());
        }
        if (frame.getSegment4() != null) {
            validateSegmentChecksum(frame.getSegment4());
        }
    }

    private void validateSegmentChecksum(CephPreParsedFrame.Segment segment) throws Exception {
        CephCRC32C crc32C = new CephCRC32C(-1L);
        crc32C.update(
                segment.getSegmentByteBuf().array(),
                segment.getSegmentByteBuf().arrayOffset(),
                segment.getLength());

        if (segment.getCrc32c() != crc32C.getValue()) {
            throw new Exception("Segment checksum validation failed");
        }
    }

    private void updateSegmentChecksum(CephPreParsedFrame.Segment segment) {
        CephCRC32C crc32C = new CephCRC32C();
        crc32C.update(
                segment.getSegmentByteBuf().array(),
                segment.getSegmentByteBuf().arrayOffset(),
                segment.getLength());

        segment.setCrc32c(crc32C.getValue());
    }
}
