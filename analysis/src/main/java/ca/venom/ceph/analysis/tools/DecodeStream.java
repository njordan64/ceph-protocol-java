package ca.venom.ceph.analysis.tools;

import ca.venom.ceph.protocol.CephDecoder;
import ca.venom.ceph.protocol.ControlFrameType;
import ca.venom.ceph.protocol.frames.AuthDoneFrame;
import ca.venom.ceph.protocol.frames.AuthReplyMoreFrame;
import ca.venom.ceph.protocol.frames.AuthRequestFrame;
import ca.venom.ceph.protocol.frames.AuthRequestMoreFrame;
import ca.venom.ceph.protocol.frames.AuthSignatureFrame;
import ca.venom.ceph.protocol.frames.BannerFrame;
import ca.venom.ceph.protocol.frames.ClientIdentFrame;
import ca.venom.ceph.protocol.frames.CompressionDoneFrame;
import ca.venom.ceph.protocol.frames.CompressionRequestFrame;
import ca.venom.ceph.protocol.frames.ControlFrame;
import ca.venom.ceph.protocol.frames.HelloFrame;
import ca.venom.ceph.protocol.frames.MessageFrame;
import ca.venom.ceph.protocol.frames.ServerIdentFrame;
import ca.venom.ceph.protocol.messages.MonMap;
import ca.venom.ceph.protocol.types.Addr;
import ca.venom.ceph.protocol.types.AddrIPv4;
import ca.venom.ceph.protocol.types.AddrIPv6;
import ca.venom.ceph.protocol.types.auth.CephXServiceTicket;
import ca.venom.ceph.protocol.types.auth.CephXTicketInfo;
import ca.venom.ceph.protocol.types.mon.MonInfo;
import ca.venom.ceph.utils.CephCRC32C;
import ca.venom.ceph.utils.HexFunctions;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.util.Base64;
import java.util.Map;

public class DecodeStream {
    private static final String PROOF_IV = "cephsageyudagreg";

    private ByteBuf clientByteBuf;
    private ByteBuf serverByteBuf;
    private final SecretKeySpec authKey;
    private boolean secureMode;
    private SecretKey streamKey;
    private SecretKeySpec sessionKey;
    private byte[] rxNonceBytes;
    private long rxCounter;
    private byte[] txNonceBytes;
    private long txCounter;
    private boolean compressionSupported = true;
    private final ObjectMapper objectMapper;

    private DecodeStream(byte[] key, String clientFilename, String serverFilename) throws Exception {
        authKey = new SecretKeySpec(key, 12, 16, "AES");

        FileInputStream stream = new FileInputStream(clientFilename);
        clientByteBuf = Unpooled.wrappedBuffer(stream.readAllBytes());
        stream.close();

        stream = new FileInputStream(serverFilename);
        serverByteBuf = Unpooled.wrappedBuffer(stream.readAllBytes());
        stream.close();

        objectMapper = new ObjectMapper();
    }

    private void parseClientBanner() throws Exception {
        parseBanner("Client", clientByteBuf);
    }

    private void parseServerBanner() throws Exception {
        parseBanner("Server", serverByteBuf);
    }

    private void parseBanner(String source, ByteBuf byteBuf) throws Exception {
        System.out.printf("[%s] Banner%n", source);

        final BannerFrame frame = new BannerFrame();
        frame.decode(byteBuf);

        System.out.println(objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(frame));

        if (compressionSupported) {
            compressionSupported = frame.isCompressionSupported();
        }
    }

    private void parseHello(boolean isClient) throws Exception {
        System.out.printf("[%s] Hello%n", isClient ? "Client" : "Server");
        ByteBuf byteBuf = isClient ? clientByteBuf : serverByteBuf;

        HelloFrame helloFrame = (HelloFrame) decodeFrame(byteBuf, isClient);
        System.out.println(objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(helloFrame));
    }

    private void parseAuthRequest() throws Exception {
        System.out.println("[Client] Auth Request");
        AuthRequestFrame authRequestFrame = (AuthRequestFrame) decodeFrame(clientByteBuf, true);
        System.out.println(objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(authRequestFrame));
    }

    private void parseAuthReplyMore() throws Exception {
        System.out.println("[Server] Auth Reply More");
        AuthReplyMoreFrame authReplyMoreFrame = (AuthReplyMoreFrame) decodeFrame(serverByteBuf, false);
        System.out.println(objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(authReplyMoreFrame));
    }

    private void parseAuthRequestMore() throws Exception {
        System.out.println("[Client] Auth Reply More");
        final AuthRequestMoreFrame authRequestMoreFrame = (AuthRequestMoreFrame) decodeFrame(clientByteBuf, true);
        System.out.println(objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(authRequestMoreFrame));
    }

    private void parseAuthDone() throws Exception {
        System.out.println("[Server] Auth Done");
        AuthDoneFrame authDoneFrame = (AuthDoneFrame) decodeFrame(serverByteBuf, false);
        System.out.println(objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(authDoneFrame));

        for (CephXTicketInfo ticketInfo : authDoneFrame.getSegment1().getPayload().getTicketInfos()) {
            byte[] iv = PROOF_IV.getBytes();
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, authKey, new IvParameterSpec(iv));
            byte[] decryptedBytes = cipher.doFinal(ticketInfo.getServiceTicket());

            ByteBuf decryptedByteBuf = Unpooled.wrappedBuffer(decryptedBytes);
            decryptedByteBuf.skipBytes(9);
            CephXServiceTicket serviceTicket = CephDecoder.decode(decryptedByteBuf, true, CephXServiceTicket.class);

            sessionKey = new SecretKeySpec(serviceTicket.getSessionKey().getSecret(), "AES");
        }

        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, sessionKey, new IvParameterSpec(PROOF_IV.getBytes()));
        byte[] encryptedSecret = authDoneFrame.getSegment1().getPayload().getEncryptedSecret();
        byte[] decryptedSecret = cipher.doFinal(encryptedSecret, 4, encryptedSecret.length - 4);

        streamKey = new SecretKeySpec(decryptedSecret, 13, 16, "AES");
        rxNonceBytes = new byte[12];
        System.arraycopy(decryptedSecret, 29, rxNonceBytes, 0, 12);
        ByteBuf byteBuf = Unpooled.wrappedBuffer(rxNonceBytes);
        rxCounter = byteBuf.getLongLE(4);
        txNonceBytes = new byte[12];
        System.arraycopy(decryptedSecret, 41, txNonceBytes, 0, 12);
        byteBuf = Unpooled.wrappedBuffer(txNonceBytes);
        txCounter = byteBuf.getLongLE(4);

        secureMode = true;
    }

    private void parseServerAuthSignature() throws Exception {
        System.out.println("[Server] Auth Signature");
        AuthSignatureFrame authSignatureFrame = (AuthSignatureFrame) decodeFrame(serverByteBuf, false);
        System.out.println(objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(authSignatureFrame));
    }

    private void parseClientAuthSignature() throws Exception {
        System.out.println("[Client] Auth Signature");
        AuthSignatureFrame authSignatureFrame = (AuthSignatureFrame) decodeFrame(clientByteBuf, true);
        System.out.println(objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(authSignatureFrame));
    }

    private void parseCompressionRequest() throws Exception {
        System.out.println("[Client] Compression Request");
        CompressionRequestFrame compressionRequestFrame = (CompressionRequestFrame) decodeFrame(clientByteBuf, true);
        System.out.println(objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(compressionRequestFrame));
    }

    private void parseCompressionDone() throws Exception {
        System.out.println("[Server] Compression Done");
        CompressionDoneFrame compressionDoneFrame = (CompressionDoneFrame) decodeFrame(serverByteBuf, false);
        System.out.println(objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(compressionDoneFrame));
    }

    private void parseClientIdentFrame() throws Exception {
        System.out.println("[Client] Client Ident");
        ClientIdentFrame clientIdentFrame = (ClientIdentFrame) decodeFrame(clientByteBuf, true);
        System.out.println(objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(clientIdentFrame));
    }

    private void parseServerIdent() throws Exception {
        System.out.println("[Server] Server Ident");
        ServerIdentFrame serverIdentFrame = (ServerIdentFrame) decodeFrame(serverByteBuf, false);
        System.out.println(objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(serverIdentFrame));
    }

    private void parseMessageFrame(boolean isClient) throws Exception {
        System.out.printf("[%s] Message\n", isClient ? "Client" : "Server");
        MessageFrame messageFrame = (MessageFrame) decodeFrame(isClient ? clientByteBuf : serverByteBuf, isClient);
        System.out.println(objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(messageFrame));
    }

    private void parseMonMap() throws Exception {
        System.out.println("[Server] Mon Map");
        MessageFrame messageFrame = (MessageFrame) decodeFrame(serverByteBuf, false);
        System.out.println(objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(messageFrame));
    }

    private CephPreParsedFrame decodeSingleFrame(ByteBuf byteBuf, boolean isClient) throws Exception {
        int startIndex = byteBuf.readerIndex();

        if (!secureMode && byteBuf.readableBytes() < 32 || secureMode && byteBuf.readableBytes() < 96) {
            throw new Exception("Not enough bytes");
        }

        int messageLength;
        ByteBuf headerByteBuf;
        ByteBuf parseByteBuf;
        if (secureMode) {
            long originalCounter = isClient ? rxCounter : txCounter;

            headerByteBuf = Unpooled.buffer(80);
            if (isClient) {
                decryptClientChunk(byteBuf, 0, 80, headerByteBuf);
            } else {
                decryptServerChunk(byteBuf, 0, 80, headerByteBuf);
            }
            int segment1Size = headerByteBuf.getIntLE(2) + 4;
            int segment2Size = headerByteBuf.getIntLE(8);
            int segment3Size = headerByteBuf.getIntLE(14);
            int segment4Size = headerByteBuf.getIntLE(20);
            if (segment1Size < 48) {
                headerByteBuf.writerIndex(32 + segment1Size);
            }

            int[] remainingChunkSizes = getEncryptedChunkSizes(headerByteBuf);
            int bytesNeeded = 96;
            for (int chunkSize : remainingChunkSizes) {
                bytesNeeded += chunkSize;
            }

            if (byteBuf.readableBytes() >= bytesNeeded) {
                parseByteBuf = Unpooled.buffer(48);
                parseByteBuf.writeBytes(headerByteBuf, 32, 48);
                headerByteBuf.writerIndex(32);

                int offset = 96;
                if (remainingChunkSizes[0] > 0) {
                    int decryptedChunkSize = segment1Size - 48;
                    int paddingLen = 16 - (decryptedChunkSize % 16);
                    if (isClient) {
                        decryptClientChunk(byteBuf, offset, decryptedChunkSize + paddingLen, parseByteBuf);
                    } else {
                        decryptServerChunk(byteBuf, offset, decryptedChunkSize + paddingLen, parseByteBuf);
                    }
                    parseByteBuf.writerIndex(parseByteBuf.writerIndex() - paddingLen);
                    offset += remainingChunkSizes[0];
                } else if (segment1Size < 48) {
                    parseByteBuf.writerIndex(parseByteBuf.writerIndex() - (48 - segment1Size) - 4);
                }

                if (remainingChunkSizes[1] > 0) {
                    if (segment3Size == 0 && segment4Size == 0) {
                        segment2Size += 5;
                    }

                    int paddingLen = 16 - (segment2Size % 16);
                    if (isClient) {
                        decryptClientChunk(byteBuf, offset, segment2Size + paddingLen, parseByteBuf);
                    } else {
                        decryptServerChunk(byteBuf, offset, segment2Size + paddingLen, parseByteBuf);
                    }
                    parseByteBuf.writerIndex(parseByteBuf.writerIndex() - paddingLen);
                    offset += remainingChunkSizes[1];
                }

                if (remainingChunkSizes[2] > 0) {
                    if (segment4Size == 0) {
                        segment3Size += 5;
                        if (segment2Size > 0) {
                            segment3Size += 4;
                        }
                    }

                    int paddingLen = 16 - (segment4Size % 16);
                    if (isClient) {
                        decryptClientChunk(byteBuf, offset, segment3Size + paddingLen, parseByteBuf);
                    } else {
                        decryptServerChunk(byteBuf, offset, segment3Size + paddingLen, parseByteBuf);
                    }
                    parseByteBuf.writerIndex(parseByteBuf.writerIndex() - paddingLen);
                    offset += remainingChunkSizes[2];
                }

                if (remainingChunkSizes[3] > 0) {
                    segment4Size += 5;
                    if (segment2Size > 0) {
                        segment4Size += 4;
                    }
                    if (segment3Size > 0) {
                        segment4Size += 4;
                    }

                    int paddingLen = 16 - (segment4Size % 16);
                    if (isClient) {
                        decryptClientChunk(byteBuf, offset, segment4Size + paddingLen, parseByteBuf);
                    } else {
                        decryptServerChunk(byteBuf, offset, segment4Size + paddingLen, parseByteBuf);
                    }
                    parseByteBuf.writerIndex(parseByteBuf.writerIndex() - paddingLen);
                    offset += remainingChunkSizes[3];
                }

                messageLength = offset;
            } else {
                if (isClient) {
                    rxCounter = originalCounter;
                } else {
                    txCounter = originalCounter;
                }
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
        frame.setMessageType(ControlFrameType.getFromTagNum(headerByteBuf.getByte(0)));

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

    private int[] getEncryptedChunkSizes(ByteBuf headerByteBuf) {
        int[] chunkSizes = new int[4];

        int segmentSize = headerByteBuf.getIntLE(2);
        if (segmentSize > 48) {
            chunkSizes[0] = ((79 + segmentSize - 48) / 80) * 96;
        } else {
            chunkSizes[0] = 0;
        }

        segmentSize = headerByteBuf.getIntLE(8);
        if (segmentSize > 0) {
            chunkSizes[1] = ((79 + segmentSize) / 80) * 96;
        } else {
            chunkSizes[1] = 0;
        }

        segmentSize = headerByteBuf.getIntLE(14);
        if (segmentSize > 0) {
            chunkSizes[2] = ((79 + segmentSize) / 80) * 96;
        } else {
            chunkSizes[2] = 0;
        }

        segmentSize = headerByteBuf.getIntLE(20);
        if (segmentSize > 0) {
            chunkSizes[3] = ((79 + segmentSize) / 80) * 96;
        } else {
            chunkSizes[3] = 0;
        }

        return chunkSizes;
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

    private ControlFrame decodeFrame(ByteBuf byteBuf, boolean isClient) throws Exception {
        CephPreParsedFrame frame = decodeSingleFrame(byteBuf, isClient);
        ControlFrame controlFrame = frame.getMessageType().getInstance();
        if (frame.getSegment1() != null) {
            controlFrame.decodeSegment1(frame.getSegment1().getSegmentByteBuf(), frame.getSegment1().isLE());
            frame.getSegment1().getSegmentByteBuf().release();
        }
        if (frame.getSegment2() != null) {
            controlFrame.decodeSegment2(frame.getSegment2().getSegmentByteBuf(), frame.getSegment2().isLE());
            frame.getSegment2().getSegmentByteBuf().release();
        }
        if (frame.getSegment3() != null) {
            controlFrame.decodeSegment3(frame.getSegment3().getSegmentByteBuf(), frame.getSegment3().isLE());
            frame.getSegment3().getSegmentByteBuf().release();
        }
        if (frame.getSegment4() != null) {
            controlFrame.decodeSegment4(frame.getSegment4().getSegmentByteBuf(), frame.getSegment4().isLE());
            frame.getSegment4().getSegmentByteBuf().release();
        }

        frame.getHeaderByteBuf().release();

        return controlFrame;
    }

    private void decryptClientChunk(ByteBuf byteBuf, int offset, int length, ByteBuf decryptedByteBuf) throws Exception {
        ByteBuf nonceByteBuf = Unpooled.wrappedBuffer(txNonceBytes);
        nonceByteBuf.writerIndex(4);
        nonceByteBuf.writeLongLE(txCounter++);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, txNonceBytes);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, streamKey, gcmParameterSpec);

        byte[] encryptedBytes = new byte[length + 16];
        byteBuf.getBytes(byteBuf.readerIndex() + offset, encryptedBytes);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        decryptedByteBuf.writeBytes(decryptedBytes);
    }

    private void decryptServerChunk(ByteBuf byteBuf, int offset, int length, ByteBuf decryptedByteBuf) throws Exception {
        ByteBuf nonceByteBuf = Unpooled.wrappedBuffer(rxNonceBytes);
        nonceByteBuf.writerIndex(4);
        nonceByteBuf.writeLongLE(rxCounter++);
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, rxNonceBytes);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, streamKey, gcmParameterSpec);

        byte[] encryptedBytes = new byte[length + 16];
        byteBuf.getBytes(byteBuf.readerIndex() + offset, encryptedBytes);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        decryptedByteBuf.writeBytes(decryptedBytes);
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.err.println("Usage: DecodeStream <KEY> <CLIENT_FILENAME> <SERVER_FILENAME>");
            System.exit(1);
        }

        DecodeStream decoder = new DecodeStream(Base64.getDecoder().decode(args[0]), args[1], args[2]);
        decoder.parseClientBanner();
        decoder.parseServerBanner();
        if (decoder.compressionSupported) {
            decoder.parseHello(false);
            decoder.parseHello(true);
        }
        decoder.parseAuthRequest();
        decoder.parseAuthReplyMore();
        decoder.parseAuthRequestMore();
        decoder.parseAuthDone();
        decoder.parseServerAuthSignature();
        decoder.parseClientAuthSignature();
        decoder.parseCompressionRequest();
        decoder.parseCompressionDone();
        decoder.parseClientIdentFrame();
        decoder.parseServerIdent();
        decoder.parseMessageFrame(true);
        decoder.parseMonMap();
    }
}
