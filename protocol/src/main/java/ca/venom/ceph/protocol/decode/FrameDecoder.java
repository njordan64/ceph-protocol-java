/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.protocol.decode;

import ca.venom.ceph.protocol.ControlFrameType;
import ca.venom.ceph.utils.CephCRC32C;
import ca.venom.ceph.utils.HexFunctions;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class FrameDecoder {
    private static final Logger LOG = LoggerFactory.getLogger(FrameDecoder.class);

    private static final short LE_VALUE = (short) 8;

    @Setter
    private SecretKey sessionKey;
    @Setter
    private byte[] nonceBytes;
    private Cipher cipher;

    public void enableSecureMode(SecretKey sessionKey, byte[] nonceBytes) throws DecryptionException {
        try {
            cipher = Cipher.getInstance("AES/GCM/NoPadding");
            this.sessionKey = sessionKey;
            this.nonceBytes = nonceBytes;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new DecryptionException("Unable to enable secure mode", e);
        }
    }

    public void disableSecureMode() {
        cipher = null;
        sessionKey = null;
        nonceBytes = null;
    }

    public PreParsedFrame decode(ByteBuf byteBuf) throws InvalidChecksumException, DecryptionException {
        if (cipher == null) {
            return decodeUnencryptedFrame(byteBuf);
        } else {
            return decodeEncryptedFrame(byteBuf);
        }
    }

    private PreParsedFrame decodeUnencryptedFrame(ByteBuf byteBuf) throws InvalidChecksumException {
        if (byteBuf.readableBytes() < 32) {
            LOG.debug(">>> Not enough bytes");
            return null;
        }

        ByteBuf headerByteBuf = byteBuf.retainedSlice(byteBuf.readerIndex(), byteBuf.readerIndex() + 32);
        validateHeaderCrc(headerByteBuf);

        final int segment1Length = headerByteBuf.getIntLE(2);
        final boolean segment1Le = headerByteBuf.getShortLE(6) == LE_VALUE;
        final int segment2Length = headerByteBuf.getIntLE(8);
        final boolean segment2Le = headerByteBuf.getShortLE(12) == LE_VALUE;
        final int segment3Length = headerByteBuf.getIntLE(14);
        final boolean segment3Le = headerByteBuf.getShortLE(18) == LE_VALUE;
        final int segment4Length = headerByteBuf.getIntLE(20);
        final boolean segment4Le = headerByteBuf.getShortLE(24) == LE_VALUE;

        int messageLength = calculateMessageLength(
                segment1Length,
                segment2Length,
                segment3Length,
                segment4Length);

        if (byteBuf.readableBytes() < messageLength) {
            return null;
        }

        PreParsedFrame preParsedFrame = new PreParsedFrame();
        preParsedFrame.setHeaderByteBuf(headerByteBuf);
        preParsedFrame.setMessageType(ControlFrameType.getFromTagNum(headerByteBuf.getByte(0)));
        preParsedFrame.setEarlyDataCompressed(headerByteBuf.getByte(26) == 1);

        int currentOffset = byteBuf.readerIndex() + 32;
        if (segment1Length > 0) {
            preParsedFrame.setSegment1(new PreParsedFrame.Segment(
                    byteBuf.retainedSlice(currentOffset, segment1Length),
                    segment1Length,
                    segment1Le
            ));
            currentOffset += segment1Length;

            validateSegmentCrc(1, byteBuf, currentOffset, preParsedFrame.getSegment1().getSegmentByteBuf());
            currentOffset += 4;
        }

        if (segment2Length > 0) {
            preParsedFrame.setSegment2(new PreParsedFrame.Segment(
                    byteBuf.retainedSlice(currentOffset, segment2Length),
                    segment2Length,
                    segment2Le
            ));
            currentOffset += segment2Length;
        }

        if (segment3Length > 0) {
            preParsedFrame.setSegment3(new PreParsedFrame.Segment(
                    byteBuf.retainedSlice(currentOffset, segment3Length),
                    segment3Length,
                    segment3Le
            ));
            currentOffset += segment3Length;
        }

        if (segment4Length > 0) {
            preParsedFrame.setSegment4(new PreParsedFrame.Segment(
                    byteBuf.retainedSlice(currentOffset, segment4Length),
                    segment4Length,
                    segment4Le
            ));
            currentOffset += segment4Length;
        }

        if (segment2Length > 0 || segment3Length > 0 || segment4Length > 0) {
            byte lateFlags = byteBuf.getByte(currentOffset++);

            if (segment2Length > 0) {
                validateSegmentCrc(2, byteBuf, currentOffset, preParsedFrame.getSegment2().getSegmentByteBuf());
                currentOffset += 4;
            }

            if (segment3Length > 0) {
                validateSegmentCrc(3, byteBuf, currentOffset, preParsedFrame.getSegment3().getSegmentByteBuf());
                currentOffset += 4;
            }

            if (segment3Length > 0) {
                validateSegmentCrc(4, byteBuf, currentOffset, preParsedFrame.getSegment4().getSegmentByteBuf());
            }
        }

        byteBuf.readerIndex(byteBuf.readerIndex() + messageLength);

        return preParsedFrame;
    }

    private PreParsedFrame decodeEncryptedFrame(ByteBuf byteBuf) throws DecryptionException {
        if (byteBuf.readableBytes() < 96) {
            LOG.debug(">>> Not enough bytes");
            return null;
        }

        final byte[] currentNonceBytes = new byte[12];
        System.arraycopy(nonceBytes, 0, currentNonceBytes, 0, 12);
        final ByteBuf nonceByteBuf = Unpooled.wrappedBuffer(currentNonceBytes);

        ByteBuf decryptedByteBuf = decryptHeader(byteBuf, nonceByteBuf);
        PreParsedFrame preParsedFrame = new PreParsedFrame();
        preParsedFrame.setMessageType(ControlFrameType.getFromTagNum(decryptedByteBuf.getByte(0)));
        preParsedFrame.setEarlyDataCompressed(decryptedByteBuf.getByte(26) == 1);

        final int segment1Length = decryptedByteBuf.getIntLE(2);
        final boolean segment1Le = decryptedByteBuf.getShortLE(6) == LE_VALUE;
        final int segment2Length = decryptedByteBuf.getIntLE(8);
        final boolean segment2Le = decryptedByteBuf.getShortLE(12) == LE_VALUE;
        final int segment3Length = decryptedByteBuf.getIntLE(14);
        final boolean segment3Le = decryptedByteBuf.getShortLE(18) == LE_VALUE;
        final int segment4Length = decryptedByteBuf.getIntLE(20);
        final boolean segment4Le = decryptedByteBuf.getShortLE(24) == LE_VALUE;

        preParsedFrame.setHeaderByteBuf(decryptedByteBuf.retainedSlice(0, 32));
        int[] chunkLengths = calculateEncryptedChunkLengths(
                segment1Length,
                segment2Length,
                segment3Length,
                segment4Length);

        if (byteBuf.readableBytes() < chunkLengths[0] + chunkLengths[1]) {
            return null;
        }

        if (segment1Length > 0) {
            final int initialSize = Math.min(48, segment1Length);
            ByteBuf segment1ByteBuf = Unpooled.buffer();
            segment1ByteBuf.writeBytes(decryptedByteBuf, 32, initialSize);
            preParsedFrame.setSegment1(new PreParsedFrame.Segment(
                    segment1ByteBuf,
                    segment1Length,
                    segment1Le
            ));
        }

        int offset = byteBuf.readerIndex() + 96;
        if (chunkLengths[0] > 0) {
            preParsedFrame.getSegment1().getSegmentByteBuf().writeBytes(
                    decryptSegment(
                            byteBuf.slice(offset, chunkLengths[0]),
                            segment1Length - 48,
                            chunkLengths[0],
                            nonceByteBuf)
            );

            offset += chunkLengths[0];
        }

        if (chunkLengths[1] > 0) {
            int segment2PaddingLength = 16 - (segment2Length % 16);
            segment2PaddingLength = segment2PaddingLength == 16 ? 0 : segment2PaddingLength;

            int segment3PaddingLength = 16 - (segment3Length % 16);
            segment3PaddingLength = segment3PaddingLength == 16 ? 0 : segment3PaddingLength;

            int segment4PaddingLength = 16 - (segment4Length % 16);
            segment4PaddingLength = segment4PaddingLength == 16 ? 0 : segment4PaddingLength;


            decryptedByteBuf = decryptSegment(
                    byteBuf.slice(offset, chunkLengths[1]),
                    segment2Length + segment2PaddingLength +
                            segment3Length + segment3PaddingLength +
                            segment4Length + segment4PaddingLength +
                            16,
                    chunkLengths[1],
                    nonceByteBuf);
            offset += chunkLengths[1];

            if (segment2Length > 0) {
                preParsedFrame.setSegment2(new PreParsedFrame.Segment(
                        decryptedByteBuf.retainedSlice(0, segment2Length),
                        segment2Length,
                        segment2Le
                ));
            }

            if (segment3Length > 0) {
                preParsedFrame.setSegment3(new PreParsedFrame.Segment(
                        decryptedByteBuf.retainedSlice(
                                segment2Length + segment2PaddingLength,
                                segment3Length
                        ),
                        segment3Length,
                        segment3Le
                ));
            }

            if (segment4Length > 0) {
                preParsedFrame.setSegment4(new PreParsedFrame.Segment(
                        decryptedByteBuf.retainedSlice(
                                segment2Length + segment2PaddingLength +
                                        segment3Length + segment3PaddingLength,
                                segment4Length
                        ),
                        segment4Length,
                        segment4Le
                ));
            }
        }

        byteBuf.readerIndex(offset);
        nonceBytes = currentNonceBytes;

        return preParsedFrame;
    }

    private void validateSegmentCrc(int segmentNumber, ByteBuf frameByteBuf, int offset, ByteBuf segmentByteBuf)
            throws InvalidChecksumException {
        byte[] crcBytes = new byte[8];
        frameByteBuf.getBytes(offset, crcBytes, 0, 4);

        ByteBuf crcByteBuf = Unpooled.wrappedBuffer(crcBytes);
        long expectedCrc = crcByteBuf.readLongLE();

        byte[] bytes = new byte[segmentByteBuf.writerIndex()];
        segmentByteBuf.getBytes(0, bytes);

        CephCRC32C crc32C = new CephCRC32C(-1L);
        crc32C.update(bytes);

        if (expectedCrc != crc32C.getValue()) {
            throw new InvalidChecksumException(segmentNumber);
        }
    }

    private ByteBuf decryptHeader(ByteBuf byteBuf, ByteBuf nonceByteBuf) throws DecryptionException {
        long counter = nonceByteBuf.getLongLE(4);
        byte[] currentNonceBytes = new byte[12];
        nonceByteBuf.getBytes(0, currentNonceBytes);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, currentNonceBytes);

        try {
            cipher.init(Cipher.DECRYPT_MODE, sessionKey, gcmParameterSpec);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new DecryptionException("Unable to decrypt message.", e);
        }

        byte[] encryptedBytes = new byte[96];
        byteBuf.getBytes(byteBuf.readerIndex(), encryptedBytes);

        try {
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            System.out.println("****************************************************");
            HexFunctions.printHexString(decryptedBytes);
            System.out.println("****************************************************");

            nonceByteBuf.setLongLE(4, counter + 1);
            return Unpooled.wrappedBuffer(decryptedBytes);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new DecryptionException("Unable to decrypt message.", e);
        }
    }

    private ByteBuf decryptSegment(ByteBuf byteBuf, int segmentLength, int encryptedLength, ByteBuf nonceByteBuf)
            throws DecryptionException {
        long counter = nonceByteBuf.getLongLE(4);
        byte[] currentNonceBytes = new byte[12];
        nonceByteBuf.getBytes(0, currentNonceBytes);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, currentNonceBytes);

        try {
            cipher.init(Cipher.DECRYPT_MODE, sessionKey, gcmParameterSpec);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new DecryptionException("Unable to decrypt message.", e);
        }

        byte[] encryptedBytes = new byte[encryptedLength];
        byteBuf.readBytes(encryptedBytes);

        try {
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            System.out.println("****************************************************");
            HexFunctions.printHexString(decryptedBytes);
            System.out.println("****************************************************");

            nonceByteBuf.setLongLE(4, counter + 1);
            return Unpooled.wrappedBuffer(decryptedBytes, 0, segmentLength);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new DecryptionException("Unable to decrypt message.", e);
        }
    }

    private void validateHeaderCrc(ByteBuf headerByteBuf) throws InvalidChecksumException {
        byte[] crcBytes = new byte[8];
        headerByteBuf.getBytes(28, crcBytes, 0, 4);
        ByteBuf crcByteBuf = Unpooled.wrappedBuffer(crcBytes);
        long expectedCrc = crcByteBuf.readLongLE();

        byte[] bytes = new byte[28];
        headerByteBuf.getBytes(0, bytes);
        CephCRC32C crc32C = new CephCRC32C(0L);
        crc32C.update(bytes);

        if (expectedCrc != crc32C.getValue()) {
            throw new InvalidChecksumException();
        }
    }

    private int calculateMessageLength(
            int segment1Length,
            int segment2Length,
            int segment3Length,
            int segment4Length) {
        int messageLength = 32;
        messageLength += segment1Length + segment2Length + segment3Length + segment4Length;

        if (segment1Length > 0) {
            messageLength += 4;
        }

        if (segment2Length > 0 || segment3Length > 0 || segment4Length > 0) {
            messageLength += 13;
        }

        return messageLength;
    }

    private int[] calculateEncryptedChunkLengths(
            int segment1Length,
            int segment2Length,
            int segment3Length,
            int segment4Length) {
        int[] chunkLengths = new int[2];

        if (segment1Length > 48) {
            chunkLengths[0] = ((segment1Length - 48 + 15) / 16) * 16 + 16;
        }

        if (segment2Length > 0) {
            chunkLengths[1] += ((segment2Length + 15) / 16) * 16;
        }

        if (segment3Length > 0) {
            chunkLengths[1] += ((segment3Length + 15) / 16) * 16;
        }

        if (segment4Length > 0) {
            chunkLengths[1] += ((segment4Length + 15) / 16) * 16;
        }

        if (chunkLengths[1] > 0) {
            chunkLengths[1] += 32;
        }

        return chunkLengths;
    }
}
