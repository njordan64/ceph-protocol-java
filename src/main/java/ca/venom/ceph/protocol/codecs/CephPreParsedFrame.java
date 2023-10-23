package ca.venom.ceph.protocol.codecs;

import ca.venom.ceph.protocol.MessageType;
import io.netty.buffer.ByteBuf;

public class CephPreParsedFrame {
    public static class Segment {
        private final ByteBuf segmentByteBuf;
        private final int length;
        private final boolean le;
        private long crc32c;

        public Segment(ByteBuf segmentByteBuf, int length, boolean le) {
            this.segmentByteBuf = segmentByteBuf;
            this.length = length;
            this.le = le;
        }

        public ByteBuf getSegmentByteBuf() {
            return segmentByteBuf;
        }

        public int getLength() {
            return length;
        }

        public boolean isLE() {
            return le;
        }

        public long getCrc32c() {
            return crc32c;
        }

        public void setCrc32c(long crc32c) {
            this.crc32c = crc32c;
        }
    }

    private ByteBuf headerByteBuf;
    private MessageType messageType;
    private boolean earlyDataCompressed;
    private Segment segment1;
    private Segment segment2;
    private Segment segment3;
    private Segment segment4;
    private boolean needHeaderChecksum;

    public ByteBuf getHeaderByteBuf() {
        return headerByteBuf;
    }

    public void setHeaderByteBuf(ByteBuf headerByteBuf) {
        this.headerByteBuf = headerByteBuf;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public boolean isEarlyDataCompressed() {
        return earlyDataCompressed;
    }

    public void setEarlyDataCompressed(boolean earlyDataCompressed) {
        this.earlyDataCompressed = earlyDataCompressed;
    }

    public Segment getSegment1() {
        return segment1;
    }

    public void setSegment1(Segment segment1) {
        this.segment1 = segment1;
    }

    public Segment getSegment2() {
        return segment2;
    }

    public void setSegment2(Segment segment2) {
        this.segment2 = segment2;
    }

    public Segment getSegment3() {
        return segment3;
    }

    public void setSegment3(Segment segment3) {
        this.segment3 = segment3;
    }

    public Segment getSegment4() {
        return segment4;
    }

    public void setSegment4(Segment segment4) {
        this.segment4 = segment4;
    }

    public boolean isNeedHeaderChecksum() {
        return needHeaderChecksum;
    }

    public void setNeedHeaderChecksum(boolean needHeaderChecksum) {
        this.needHeaderChecksum = needHeaderChecksum;
    }
}