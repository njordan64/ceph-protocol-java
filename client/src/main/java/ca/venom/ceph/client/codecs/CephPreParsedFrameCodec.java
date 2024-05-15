/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.client.codecs;

import ca.venom.ceph.protocol.ControlFrameType;
import ca.venom.ceph.protocol.decode.DecryptionException;
import ca.venom.ceph.protocol.decode.FrameDecoder;
import ca.venom.ceph.protocol.decode.PreParsedFrame;
import ca.venom.ceph.utils.CephCRC32C;
import ca.venom.ceph.utils.HexFunctions;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.util.List;

public class CephPreParsedFrameCodec extends ByteToMessageCodec<PreParsedFrame> {
    private static final Logger LOG = LoggerFactory.getLogger(CephPreParsedFrameCodec.class);

    private boolean secureMode;
    private SecretKey streamKey;
    private byte[] txNonceBytes;
    private long txCounter;
    private boolean captureBytes = true;
    private ByteBuf receivedByteBuf;
    private ByteBuf sentByteBuf;
    private FrameDecoder receivedFrameDecoder;

    public CephPreParsedFrameCodec(ByteBuf receivedByteBuf, ByteBuf sentByteBuf) {
        this.receivedByteBuf = receivedByteBuf;
        this.sentByteBuf = sentByteBuf;
        receivedFrameDecoder = new FrameDecoder();
    }

    public void enableSecureMode(SecretKey streamKey, byte[] rxNonceBytes, byte[] txNonceBytes) {
        try {
            receivedFrameDecoder.enableSecureMode(streamKey, rxNonceBytes);
            this.secureMode = true;
            this.streamKey = streamKey;
            this.txNonceBytes = txNonceBytes;

            ByteBuf byteBuf = Unpooled.wrappedBuffer(txNonceBytes);
            txCounter = byteBuf.getLongLE(4);
        } catch (DecryptionException e) {
            LOG.warn("Unable to enable secure mode", e);
        }
    }

    public void disableSecureMode() {
        secureMode = false;
        streamKey = null;
        txNonceBytes = null;
        txCounter = 0L;
        receivedFrameDecoder.disableSecureMode();
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
            final int startIndex = byteBuf.readerIndex();

            PreParsedFrame frame = receivedFrameDecoder.decode(byteBuf);
            if (frame != null) {
                LOG.debug(">>> Decoded: (" + frame.getMessageType().name() + ")");
                if (captureBytes) {
                    receivedByteBuf.writeBytes(byteBuf, startIndex, byteBuf.readerIndex() - startIndex);
                }

                list.add(frame);
            } else {
                break;
            }

            if (frame.getMessageType() == ControlFrameType.AUTH_DONE) {
                ctx.channel().config().setAutoRead(false);
                break;
            }
        }
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, PreParsedFrame frame, ByteBuf byteBuf) throws Exception {
        LOG.debug(">>> CephPreParsedFrameCodec.encode (" + frame.getMessageType().name() + ")");

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

        ByteBuf outputByteBuf;
        if (secureMode) {
            outputByteBuf = writeEncryptedFrame(headerByteBuf, frame);
        } else {
            outputByteBuf = writePlainTextFrame(headerByteBuf, frame);
        }

        if (captureBytes) {
            int readerIndex = outputByteBuf.readerIndex();
            sentByteBuf.writeBytes(outputByteBuf);
            outputByteBuf.readerIndex(readerIndex);
        }

        int readerIndex = outputByteBuf.readerIndex();
        byte[] bytes = new byte[outputByteBuf.writerIndex()];
        outputByteBuf.readBytes(bytes);
        outputByteBuf.readerIndex(readerIndex);
        LOG.trace("Encoded message:\n" + HexFunctions.hexToString(bytes));

        byteBuf.writeBytes(outputByteBuf);
    }

    private int getSegmentCount(PreParsedFrame frame) {
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

    private void writeSegmentHeader(PreParsedFrame.Segment segment, ByteBuf headerByteBuf) {
        if (segment != null) {
            headerByteBuf.writeIntLE(segment.getLength());
            headerByteBuf.writeShortLE(segment.isLe() ? 8 : 0);
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

    private ByteBuf writePlainTextFrame(ByteBuf headerByteBuf, PreParsedFrame frame) {
        ByteBuf frameByteBuf = Unpooled.buffer();
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

        byte [] sentBytes = new byte[frameByteBuf.writerIndex()];
        frameByteBuf.getBytes(0, sentBytes);


        byte[] x = new byte[frameByteBuf.writerIndex()];
        frameByteBuf.getBytes(0, x);

        return frameByteBuf;
    }

    private ByteBuf writeEncryptedFrame(ByteBuf headerByteBuf, PreParsedFrame frame) throws Exception {
        ByteBuf outputByteBuf = Unpooled.buffer();
        ByteBuf chunkByteBuf = Unpooled.buffer(80);

        int segment1Length = frame.getSegment1().getSegmentByteBuf().writerIndex();
        int segment2Length = frame.getSegment2() == null ? 0 : frame.getSegment2().getSegmentByteBuf().writerIndex();
        int segment3Length = frame.getSegment3() == null ? 0 : frame.getSegment3().getSegmentByteBuf().writerIndex();
        int segment4Length = frame.getSegment4() == null ? 0 : frame.getSegment4().getSegmentByteBuf().writerIndex();

        chunkByteBuf.writeBytes(headerByteBuf);
        if (segment1Length > 48) {
            chunkByteBuf.writeBytes(frame.getSegment1().getSegmentByteBuf(), 0, 48);
            outputByteBuf.writeBytes(encryptChunk(chunkByteBuf));

            int unencyptedSize = segment1Length - 48;
            int paddingLen = 80 - (unencyptedSize % 80);
            chunkByteBuf = Unpooled.buffer(unencyptedSize + paddingLen);
            chunkByteBuf.writeBytes(frame.getSegment1().getSegmentByteBuf(), 48, unencyptedSize);
            chunkByteBuf.writerIndex(unencyptedSize + paddingLen);
            outputByteBuf.writeBytes(encryptChunk(chunkByteBuf));
        } else {
            chunkByteBuf.writeBytes(frame.getSegment1().getSegmentByteBuf());
            chunkByteBuf.writerIndex(80);
            outputByteBuf.writeBytes(encryptChunk(chunkByteBuf));
        }

        chunkByteBuf = Unpooled.buffer();
        if (segment2Length > 0) {
            int paddingLen = 16 - (segment2Length % 16);
            chunkByteBuf.writeBytes(frame.getSegment2().getSegmentByteBuf());
            if (segment3Length > 0 || segment4Length > 0) {
                chunkByteBuf.writeZero(paddingLen);
            }
        }

        if (segment3Length > 0) {
            int paddingLen = 16 - (segment3Length % 16);
            chunkByteBuf.writeBytes(frame.getSegment3().getSegmentByteBuf());
            if (segment4Length > 0) {
                chunkByteBuf.writeZero(paddingLen);
            }
        }

        if (segment4Length > 0) {
            chunkByteBuf.writeBytes(frame.getSegment4().getSegmentByteBuf());
        }

        if (segment2Length > 0 && segment3Length > 0 && segment4Length > 0) {
            chunkByteBuf.writeZero(1); // Flags
        }
        if (segment2Length > 0) {
            writeSegmentCrc(frame.getSegment2().getSegmentByteBuf(), segment2Length, chunkByteBuf);
        }
        if (segment3Length > 0) {
            writeSegmentCrc(frame.getSegment3().getSegmentByteBuf(), segment3Length, chunkByteBuf);
        }
        if (segment4Length > 0) {
            writeSegmentCrc(frame.getSegment4().getSegmentByteBuf(), segment4Length, chunkByteBuf);
        }

        if (chunkByteBuf.writerIndex() % 16 > 0) {
            chunkByteBuf.writeZero(16 - (chunkByteBuf.writerIndex() % 16));
        }

        if (chunkByteBuf.writerIndex() > 0) {
            outputByteBuf.writeBytes(encryptChunk(chunkByteBuf));
        }

        return outputByteBuf;
    }

    private byte[] encryptChunk(ByteBuf byteBuf) throws Exception {
        LOG.trace("*** StreamKey: " + HexFunctions.hexToString(streamKey.getEncoded()));
        LOG.trace("*** Nonce: " + HexFunctions.hexToString(txNonceBytes));

        byte[] bytesToEncrypt = new byte[byteBuf.writerIndex()];
        byteBuf.readBytes(bytesToEncrypt);

        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, txNonceBytes);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, streamKey, gcmParameterSpec);
        byte[] encryptedBytes = cipher.doFinal(bytesToEncrypt);

        LOG.trace("*** Unencrypted Data:\n" + HexFunctions.hexToString(bytesToEncrypt));
        LOG.trace("*** Encrypted Data:\n" + HexFunctions.hexToString(encryptedBytes));

        txCounter++;
        ByteBuf txNonceByteBuf = Unpooled.wrappedBuffer(txNonceBytes);
        txNonceByteBuf.setLongLE(4, txCounter);

        return encryptedBytes;
    }
}
