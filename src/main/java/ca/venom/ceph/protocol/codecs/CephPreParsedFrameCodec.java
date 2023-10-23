package ca.venom.ceph.protocol.codecs;

import ca.venom.ceph.CephCRC32C;
import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.Int32;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

public class CephPreParsedFrameCodec extends ByteToMessageCodec<CephPreParsedFrame> {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) throws Exception {
        while (true) {
            CephPreParsedFrame frame = decodeSingleFrame(byteBuf);
            if (frame != null) {
                list.add(frame);
            } else {
                break;
            }
        }
    }

    private CephPreParsedFrame decodeSingleFrame(ByteBuf byteBuf) throws Exception {
        int startIndex = byteBuf.readerIndex();

        if (byteBuf.readableBytes() < 32) {
            return null;
        }

        ByteBuf headerByteBuf = byteBuf.slice(byteBuf.readerIndex(), byteBuf.readerIndex() + 32);

        int messageLength = 32;
        int segmentsCount = headerByteBuf.getByte(1);

        int segmentLength1 = headerByteBuf.getIntLE(2);
        boolean segmentLe1 = headerByteBuf.getShortLE(6) == 8;
        int segmentLength2 = headerByteBuf.getIntLE(8);
        boolean segmentLe2 = headerByteBuf.getShortLE(12) == 8;
        int segmentLength3 = headerByteBuf.getIntLE(14);
        boolean segmentLe3 = headerByteBuf.getShortLE(18) == 8;
        int segmentLength4 = headerByteBuf.getIntLE(20);
        boolean segmentLe4 = headerByteBuf.getShortLE(24) == 8;

        int lateCrcPosition = messageLength + segmentLength1 + 4 + segmentLength2 + segmentLength3 + segmentLength4;
        if (segmentsCount > 1) {
            lateCrcPosition++;
            messageLength = lateCrcPosition + 4 * (segmentsCount - 1);
        }

        if (byteBuf.readableBytes() >= messageLength) {
            CephPreParsedFrame frame = new CephPreParsedFrame();
            frame.setMessageType(MessageType.getFromTagNum(headerByteBuf.getByte(0)));

            byte flags = headerByteBuf.getByte(1);
            frame.setEarlyDataCompressed(flags == 1);

            int offset = byteBuf.readerIndex() + 32;
            if (segmentLength1 > 0) {
                CephPreParsedFrame.Segment segment = createSegment(
                        byteBuf,
                        offset,
                        segmentLength1,
                        segmentLe1,
                        offset + segmentLength1);
                frame.setSegment1(segment);
                offset += segmentLength1 + 4;
            }

            if (segmentLength2 > 0) {
                CephPreParsedFrame.Segment segment = createSegment(
                        byteBuf,
                        offset,
                        segmentLength2,
                        segmentLe2,
                        lateCrcPosition
                );
                frame.setSegment2(segment);
                offset += segmentLength2;
                lateCrcPosition += 4;
            }

            if (segmentLength3 > 0) {
                CephPreParsedFrame.Segment segment = createSegment(
                        byteBuf,
                        offset,
                        segmentLength3,
                        segmentLe3,
                        lateCrcPosition
                );
                frame.setSegment3(segment);
                offset += segmentLength3;
                lateCrcPosition += 4;
            }

            if (segmentLength4 > 0) {
                CephPreParsedFrame.Segment segment = createSegment(
                        byteBuf,
                        offset,
                        segmentLength4,
                        segmentLe4,
                        lateCrcPosition
                );
                frame.setSegment4(segment);
            }

            if (frame.getSegment1() != null) {
                frame.getSegment1().getSegmentByteBuf().retain();
            }
            if (frame.getSegment2() != null) {
                frame.getSegment2().getSegmentByteBuf().retain();
            }
            if (frame.getSegment3() != null) {
                frame.getSegment3().getSegmentByteBuf().retain();
            }
            if (frame.getSegment4() != null) {
                frame.getSegment4().getSegmentByteBuf().retain();
            }

            byteBuf.skipBytes(messageLength);

            frame.setHeaderByteBuf(byteBuf.retainedSlice(startIndex, 32));

            return frame;
        }

        return null;
    }

    private CephPreParsedFrame.Segment createSegment(ByteBuf byteBuf,
                                                     int offset,
                                                     int segmentLength,
                                                     boolean le,
                                                     int crcOffset) {
        ByteBuf segmentByteBuf = byteBuf.slice(offset, segmentLength);
        byte[] crcBytes = new byte[8];
        byteBuf.getBytes(crcOffset, crcBytes, 0, 4);

        ByteBuf crcByteBuf = Unpooled.wrappedBuffer(crcBytes);
        long crc32c = crcByteBuf.readLongLE();

        CephPreParsedFrame.Segment segment = new CephPreParsedFrame.Segment(segmentByteBuf, segmentLength, le);
        segment.setCrc32c(crc32c);

        return segment;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, CephPreParsedFrame frame, ByteBuf byteBuf) throws Exception {
        ByteBuf headerByteBuf = Unpooled.buffer(32);
        headerByteBuf.writeByte((byte) frame.getMessageType().getTagNum());
        headerByteBuf.writeByte(frame.isEarlyDataCompressed() ? 1 : 0);

        writeSegmentHeader(frame.getSegment1(), headerByteBuf);
        writeSegmentHeader(frame.getSegment2(), headerByteBuf);
        writeSegmentHeader(frame.getSegment3(), headerByteBuf);
        writeSegmentHeader(frame.getSegment4(), headerByteBuf);

        headerByteBuf.writeByte(0);
        headerByteBuf.writeByte(0);

        addHeaderCrc(headerByteBuf);

        byteBuf.writeBytes(headerByteBuf, 0, 32);

        if (frame.getSegment1() != null) {
            byteBuf.writeBytes(
                    frame.getSegment1().getSegmentByteBuf(),
                    0,
                    frame.getSegment1().getLength());
            new Int32(frame.getSegment1().getCrc32c()).encode(byteBuf, true);
        }

        boolean needEpilogue = false;
        if (frame.getSegment2() != null) {
            needEpilogue = true;
            byteBuf.writeBytes(
                    frame.getSegment2().getSegmentByteBuf(),
                    0,
                    frame.getSegment2().getLength());
        }

        if (frame.getSegment3() != null) {
            needEpilogue = true;
            byteBuf.writeBytes(
                    frame.getSegment3().getSegmentByteBuf(),
                    0,
                    frame.getSegment3().getLength());
        }

        if (frame.getSegment4() != null) {
            needEpilogue = true;
            byteBuf.writeBytes(
                    frame.getSegment4().getSegmentByteBuf(),
                    0,
                    frame.getSegment4().getLength());
        }

        if (needEpilogue) {
            byteBuf.writeByte(0);

            if (frame.getSegment2() != null) {
                new Int32(frame.getSegment2().getCrc32c()).encode(byteBuf, true);
            }

            if (frame.getSegment3() != null) {
                new Int32(frame.getSegment3().getCrc32c()).encode(byteBuf, true);
            }

            if (frame.getSegment4() != null) {
                new Int32(frame.getSegment4().getCrc32c()).encode(byteBuf, true);
            }
        }
    }

    private void writeSegmentHeader(CephPreParsedFrame.Segment segment, ByteBuf headerByteBuf) {
        if (segment != null) {
            headerByteBuf.writeIntLE(segment.getLength());
            headerByteBuf.writeShortLE(segment.isLE() ? 8 : 0);
        } else {
            headerByteBuf.writeIntLE(0);
            headerByteBuf.writeShortLE(0);
        }
    }

    private void addHeaderCrc(ByteBuf headerByteBuf) {
        CephCRC32C crc32C = new CephCRC32C(0L);
        crc32C.update(headerByteBuf.array(), headerByteBuf.arrayOffset(), 28);

        byte[] crcBytes = new byte[8];
        ByteBuf crcByteBuf = Unpooled.wrappedBuffer(crcBytes);
        crcByteBuf.writeLongLE(crc32C.getValue());

        headerByteBuf.writeBytes(crcBytes, 0, 4);
    }
}
