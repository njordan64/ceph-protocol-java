package ca.venom.ceph.protocol.codecs;

import ca.venom.ceph.CephCRC32C;
import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.frames.BannerFrame;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.io.ByteArrayOutputStream;
import java.util.List;

public class CephPreParsedFrameCodec extends ByteToMessageCodec<CephPreParsedFrame> {
    private static final Logger LOG = LoggerFactory.getLogger(CephPreParsedFrameCodec.class);

    private boolean secureMode;
    private SecretKey streamKey;
    private byte[] rxNonceBytes;
    private long rxCounter;
    private byte[] txNonceBytes;
    private long txCounter;
    private boolean captureBytes = true;
    private ByteBuf receivedByteBuf;
    private ByteBuf sentByteBuf;

    public CephPreParsedFrameCodec(ByteBuf receivedByteBuf, ByteBuf sentByteBuf) {
        this.receivedByteBuf = receivedByteBuf;
        this.sentByteBuf = sentByteBuf;
    }

    public void enableSecureMode(SecretKey streamKey, byte[] rxNonceBytes, byte[] txNonceBytes) {
        this.secureMode = true;
        this.streamKey = streamKey;
        this.rxNonceBytes = rxNonceBytes;
        this.txNonceBytes = txNonceBytes;

        ByteBuf byteBuf = Unpooled.wrappedBuffer(rxNonceBytes);
        rxCounter = byteBuf.getLongLE(4);
        byteBuf = Unpooled.wrappedBuffer(txNonceBytes);
        txCounter = byteBuf.getLongLE(4);
    }

    public void disableSecureMode() {
        secureMode = false;
        streamKey = null;
        rxNonceBytes = null;
        txNonceBytes = null;
        rxCounter = 0L;
        txCounter = 0L;
    }

    public void setCaptureBytes(boolean captureBytes) {
        this.captureBytes = captureBytes;
    }

    public ByteBuf getReceivedByteBuf() {
        return receivedByteBuf;
    }

    public void releaseReceivedByteBuf() {
        receivedByteBuf = Unpooled.buffer();
    }

    public ByteBuf getSentByteBuf() {
        return sentByteBuf;
    }

    public void releaseSentByteBuf() {
        sentByteBuf = Unpooled.buffer();
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) throws Exception {
        LOG.debug(">>> CephPreParsedFrameCodec.decode");

        while (byteBuf.readableBytes() > 0) {
            int startIndex = byteBuf.readerIndex();
            CephPreParsedFrame frame = decodeSingleFrame(byteBuf);
            if (frame != null) {
                if (captureBytes) {
                    receivedByteBuf.writeBytes(byteBuf, startIndex, byteBuf.readerIndex() - startIndex);
                }
                list.add(frame);
            } else {
                break;
            }

            if (frame.getMessageType() == MessageType.AUTH_DONE) {
                ctx.channel().config().setAutoRead(false);
                break;
            }
        }
    }

    private CephPreParsedFrame decodeSingleFrame(ByteBuf byteBuf) throws Exception {
        int startIndex = byteBuf.readerIndex();

        if (!secureMode && byteBuf.readableBytes() < 32 || secureMode && byteBuf.readableBytes() < 96) {
            LOG.debug(">>> Not enough bytes");
            return null;
        }

        int messageLength;
        ByteBuf headerByteBuf;
        ByteBuf parseByteBuf;
        if (secureMode) {
            headerByteBuf = decryptChunk(byteBuf, 0);
            int frameLength = getMessageLengthFromHeader(headerByteBuf);

            messageLength = 96;
            while (headerByteBuf.writerIndex() < frameLength) {
                if (byteBuf.readableBytes() >= messageLength + 96) {
                    ByteBuf nextChunk = decryptChunk(byteBuf, messageLength);
                    headerByteBuf.writeBytes(nextChunk);
                    messageLength += 96;
                } else {
                    return null;
                }
            }

            parseByteBuf = headerByteBuf.slice(32, headerByteBuf.writerIndex() - 32);
            if (parseByteBuf.readableBytes() < frameLength - 32) {
                return null;
            }
        } else {
            headerByteBuf = byteBuf.slice(byteBuf.readerIndex(), byteBuf.readerIndex() + 32);
            validateHeaderCrc(headerByteBuf);
            messageLength = getMessageLengthFromHeader(headerByteBuf);

            if (byteBuf.readableBytes() < messageLength) {
                return null;
            }

            parseByteBuf = byteBuf.slice(byteBuf.readerIndex() + 32, messageLength - 32);
        }

        int lateCrcPosition = getLateCrcOffset(headerByteBuf);

        CephPreParsedFrame frame = new CephPreParsedFrame();
        frame.setMessageType(MessageType.getFromTagNum(headerByteBuf.getByte(0)));

        byte flags = headerByteBuf.getByte(26);
        frame.setEarlyDataCompressed(flags == 1);

        int segmentLength = headerByteBuf.getIntLE(2);
        int offset = 0;
        if (segmentLength > 0) {
            CephPreParsedFrame.Segment segment = createSegment(
                    parseByteBuf,
                    offset,
                    segmentLength,
                    headerByteBuf.getShortLE(6) == 8,
                    offset + segmentLength);
            frame.setSegment1(segment);
            offset += segmentLength + 4;
        }

        segmentLength = headerByteBuf.getIntLE(8);
        if (segmentLength > 0) {
            CephPreParsedFrame.Segment segment = createSegment(
                    parseByteBuf,
                    offset,
                    segmentLength,
                    headerByteBuf.getShortLE(12) == 8,
                    lateCrcPosition
            );
            frame.setSegment2(segment);
            offset += segmentLength;
            lateCrcPosition += 4;
        }

        segmentLength = headerByteBuf.getIntLE(14);
        if (segmentLength > 0) {
            CephPreParsedFrame.Segment segment = createSegment(
                    parseByteBuf,
                    offset,
                    segmentLength,
                    headerByteBuf.getShortLE(18) == 8,
                    lateCrcPosition
            );
            frame.setSegment3(segment);
            offset += segmentLength;
            lateCrcPosition += 4;
        }

        segmentLength = headerByteBuf.getIntLE(20);
        if (segmentLength > 0) {
            CephPreParsedFrame.Segment segment = createSegment(
                    parseByteBuf,
                    offset,
                    segmentLength,
                    headerByteBuf.getShortLE(24) == 8,
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

        byteBuf.readerIndex(startIndex + messageLength);
        frame.setHeaderByteBuf(byteBuf.retainedSlice(startIndex, 32));

        return frame;
    }

    private ByteBuf decryptChunk(ByteBuf byteBuf, int offset) throws Exception {
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, rxNonceBytes);
        ByteBuf nonceByteBuf = Unpooled.wrappedBuffer(rxNonceBytes);
        nonceByteBuf.writerIndex(4);
        nonceByteBuf.writeLongLE(++rxCounter);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, streamKey,gcmParameterSpec);

        byte[] encryptedBytes = new byte[96];
        byteBuf.getBytes(byteBuf.readerIndex() + offset, encryptedBytes);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        ByteBuf decryptedByteBuf = Unpooled.buffer(decryptedBytes.length);
        decryptedByteBuf.writeBytes(decryptedBytes);
        return decryptedByteBuf;
    }

    private void validateHeaderCrc(ByteBuf headerByteBuf) throws Exception {
        byte[] crcBytes = new byte[8];
        headerByteBuf.getBytes(28, crcBytes, 0, 4);
        ByteBuf crcByteBuf = Unpooled.wrappedBuffer(crcBytes);
        long expectedCrc = crcByteBuf.readLongLE();

        byte[] bytes = new byte[28];
        headerByteBuf.getBytes(0, bytes);
        CephCRC32C crc32C = new CephCRC32C(0L);
        crc32C.update(bytes);

        if (expectedCrc != crc32C.getValue()) {
            throw new Exception("Header checksum validation failed");
        }
    }

    private int getLateCrcOffset(ByteBuf headerByteBuf) {
        int lateCrcPosition = 32;
        int segmentsCount = headerByteBuf.getByte(1);

        int segmentLength1 = headerByteBuf.getIntLE(2);
        int segmentLength2 = headerByteBuf.getIntLE(8);
        int segmentLength3 = headerByteBuf.getIntLE(14);
        int segmentLength4 = headerByteBuf.getIntLE(20);

        lateCrcPosition += segmentLength1 + 4 + segmentLength2 + segmentLength3 + segmentLength4;
        if (segmentsCount > 1) {
            lateCrcPosition++;
        }

        return lateCrcPosition;
    }

    private int getMessageLengthFromHeader(ByteBuf headerByteBuf) {
        int messageLength = 36;

        boolean haveLateFlags = false;
        for (int i = 0; i < 4; i++) {
            int segmentLengths = headerByteBuf.getIntLE(2 + i * 6);
            messageLength += segmentLengths;

            if (i > 0 && segmentLengths > 0) {
                messageLength += 4;
                haveLateFlags = true;
            }
        }

        messageLength += haveLateFlags ? 1 : 0;

        return messageLength;
    }

    private CephPreParsedFrame.Segment createSegment(ByteBuf byteBuf,
                                                     int offset,
                                                     int segmentLength,
                                                     boolean le,
                                                     int crcOffset) throws Exception {
        ByteBuf segmentByteBuf = byteBuf.slice(offset, segmentLength);

        if (!secureMode) {
            byte[] crcBytes = new byte[8];
            byteBuf.getBytes(crcOffset, crcBytes, 0, 4);

            ByteBuf crcByteBuf = Unpooled.wrappedBuffer(crcBytes);
            long expectedCrc = crcByteBuf.readLongLE();

            byte[] bytes = new byte[segmentLength];
            segmentByteBuf.getBytes(0, bytes);

            CephCRC32C crc32C = new CephCRC32C(-1L);
            crc32C.update(bytes);

            if (expectedCrc != crc32C.getValue()) {
                throw new Exception("Segment checksum validation failed");
            }
        }

        CephPreParsedFrame.Segment segment = new CephPreParsedFrame.Segment(segmentByteBuf, segmentLength, le);
        return segment;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, CephPreParsedFrame frame, ByteBuf byteBuf) throws Exception {
        LOG.debug(">>> CephPreParsedFrameCodec.encode");

        ByteBuf frameByteBuf = Unpooled.buffer();

        ByteBuf headerByteBuf = Unpooled.buffer(32);
        headerByteBuf.writeByte((byte) frame.getMessageType().getTagNum());
        headerByteBuf.writeByte((byte) getSegmentCount(frame));

        writeSegmentHeader(frame.getSegment1(), headerByteBuf);
        writeSegmentHeader(frame.getSegment2(), headerByteBuf);
        writeSegmentHeader(frame.getSegment3(), headerByteBuf);
        writeSegmentHeader(frame.getSegment4(), headerByteBuf);

        headerByteBuf.writeByte(frame.isEarlyDataCompressed() ? 1 : 0);
        headerByteBuf.writeByte(0);

        addHeaderCrc(headerByteBuf);

        frameByteBuf.writeBytes(headerByteBuf, 0, 32);

        if (frame.getSegment1() != null) {
            frameByteBuf.writeBytes(
                    frame.getSegment1().getSegmentByteBuf(),
                    0,
                    frame.getSegment1().getLength());
            writeSegmentCrc(
                    frame.getSegment1().getSegmentByteBuf(),
                    frame.getSegment1().getLength(),
                    frameByteBuf);
        }

        boolean needEpilogue = false;
        if (frame.getSegment2() != null) {
            needEpilogue = true;
            frameByteBuf.writeBytes(
                    frame.getSegment2().getSegmentByteBuf(),
                    0,
                    frame.getSegment2().getLength());
        }

        if (frame.getSegment3() != null) {
            needEpilogue = true;
            frameByteBuf.writeBytes(
                    frame.getSegment3().getSegmentByteBuf(),
                    0,
                    frame.getSegment3().getLength());
        }

        if (frame.getSegment4() != null) {
            needEpilogue = true;
            frameByteBuf.writeBytes(
                    frame.getSegment4().getSegmentByteBuf(),
                    0,
                    frame.getSegment4().getLength());
        }

        if (needEpilogue) {
            frameByteBuf.writeByte(0);

            if (frame.getSegment2() != null) {
                writeSegmentCrc(
                        frame.getSegment2().getSegmentByteBuf(),
                        frame.getSegment2().getLength(),
                        frameByteBuf
                );
            }

            if (frame.getSegment3() != null) {
                writeSegmentCrc(
                        frame.getSegment3().getSegmentByteBuf(),
                        frame.getSegment3().getLength(),
                        frameByteBuf
                );
            }

            if (frame.getSegment4() != null) {
                writeSegmentCrc(
                        frame.getSegment4().getSegmentByteBuf(),
                        frame.getSegment4().getLength(),
                        frameByteBuf
                );
            }
        }

        int originalIndex = frameByteBuf.readerIndex();
        byte [] sentBytes = new byte[frameByteBuf.writerIndex()];
        frameByteBuf.getBytes(0, sentBytes);
        sentByteBuf.writeBytes(sentBytes);

        if (secureMode) {
            writeEncryptedFrame(frameByteBuf, byteBuf);
        } else {
            byte[] x = new byte[frameByteBuf.writerIndex()];
            frameByteBuf.getBytes(0, x);
            byteBuf.writeBytes(frameByteBuf, 0, frameByteBuf.writerIndex());
        }
    }

    private int getSegmentCount(CephPreParsedFrame frame) {
        int segmentCount = 1;
        if (frame.getSegment2() != null) {
            segmentCount++;
        }
        if (frame.getSegment3() != null) {
            segmentCount++;
        }
        if (frame.getSegment4() != null) {
            segmentCount++;
        }

        return segmentCount;
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
        crcByteBuf.writerIndex(0);
        crcByteBuf.writeLongLE(crc32C.getValue());

        headerByteBuf.writeBytes(crcBytes, 0, 4);
    }

    private void writeSegmentCrc(ByteBuf segmentByteBuf, int length, ByteBuf frameByteBuf) {
        byte[] bytes = new byte[length];
        segmentByteBuf.getBytes(0, bytes);

        CephCRC32C crc32C = new CephCRC32C(-1L);
        crc32C.update(bytes);
        long crcValue = crc32C.getValue();

        byte[] crcBytes = new byte[8];
        ByteBuf crcByteBuf = Unpooled.wrappedBuffer(crcBytes);
        crcByteBuf.setLongLE(0, crcValue);

        frameByteBuf.writeBytes(crcBytes, 0, 4);
    }

    private void writeEncryptedFrame(ByteBuf frameByteBuf, ByteBuf byteBuf) throws Exception {
        int offset = 0;
        while (offset < frameByteBuf.writerIndex()) {
            byte[] bytesToEncrypt = new byte[80];
            offset += bytesToEncrypt.length;
            frameByteBuf.readBytes(bytesToEncrypt, offset, Math.min(80, frameByteBuf.writerIndex() - offset));

            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, txNonceBytes);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, streamKey, gcmParameterSpec);
            byteBuf.writeBytes(cipher.doFinal(bytesToEncrypt, 0, 96));

            txCounter++;
            ByteBuf txNonceByteBuf = Unpooled.wrappedBuffer(txNonceBytes);
            txNonceByteBuf.setLongLE(4, txCounter);
        }
    }
}
