/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.analysis.tools;

import ca.venom.ceph.protocol.CephDecoder;
import ca.venom.ceph.protocol.decode.FrameDecoder;
import ca.venom.ceph.protocol.decode.PreParsedFrame;
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
import ca.venom.ceph.protocol.frames.ServerIdentFrame;
import ca.venom.ceph.protocol.types.auth.AuthDoneMonPayload;
import ca.venom.ceph.protocol.types.auth.AuthRequestAuthorizerPayload;
import ca.venom.ceph.protocol.types.auth.AuthRequestMoreAuthorizerPayload;
import ca.venom.ceph.protocol.types.auth.CephXAuthorize;
import ca.venom.ceph.protocol.types.auth.CephXAuthorizeReplyV2;
import ca.venom.ceph.protocol.types.auth.CephXServerChallenge;
import ca.venom.ceph.protocol.types.auth.CephXServiceTicket;
import ca.venom.ceph.protocol.types.auth.CephXServiceTicketInfo;
import ca.venom.ceph.protocol.types.auth.CephXTicketInfo;
import ca.venom.ceph.utils.HexFunctions;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.util.Base64;

/**
 * Decodes client and server streams to enable analysis of the traffic.
 */
public class DecodeStream {
    private static final String PROOF_IV = "cephsageyudagreg";

    private ByteBuf clientByteBuf;
    private ByteBuf serverByteBuf;
    private final SecretKeySpec authKey;
    private SecretKey streamKey;
    private SecretKeySpec sessionKey;
    private byte[] rxNonceBytes;
    private byte[] txNonceBytes;
    private boolean compressionSupported = true;
    private final ObjectMapper objectMapper;
    private final FrameDecoder clientDecoder;
    private final FrameDecoder serverDecoder;

    private DecodeStream(byte[] key, String clientFilename, String serverFilename) throws Exception {
        this.authKey = new SecretKeySpec(key, 12, 16, "AES");

        FileInputStream stream = new FileInputStream(clientFilename);
        this.clientByteBuf = Unpooled.wrappedBuffer(stream.readAllBytes());
        stream.close();

        stream = new FileInputStream(serverFilename);
        this.serverByteBuf = Unpooled.wrappedBuffer(stream.readAllBytes());
        stream.close();

        this.objectMapper = new ObjectMapper();
        this.clientDecoder = new FrameDecoder();
        this.serverDecoder = new FrameDecoder();
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

        if (authRequestFrame.getSegment1().getPayload() instanceof AuthRequestAuthorizerPayload payload) {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, authKey, new IvParameterSpec(PROOF_IV.getBytes()));
            byte[] unencryptedBytes = cipher.doFinal(payload.getTicket().getBlob());

            System.out.println("** Ticket Info *************************************");
            CephXServiceTicketInfo ticketInfo = CephDecoder.decode(
                    Unpooled.wrappedBuffer(unencryptedBytes, 9, unencryptedBytes.length - 9),
                    true,
                    CephXServiceTicketInfo.class);
            System.out.println(objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(ticketInfo));
            System.out.println("****************************************************");
            sessionKey = new SecretKeySpec(ticketInfo.getSessionKey().getSecret(), 0, 16, "AES");
        }
    }

    private void parseAuthReplyMore() throws Exception {
        System.out.println("[Server] Auth Reply More");
        AuthReplyMoreFrame authReplyMoreFrame = (AuthReplyMoreFrame) decodeFrame(serverByteBuf, false);
        System.out.println(objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(authReplyMoreFrame));

        if (sessionKey != null) {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, sessionKey, new IvParameterSpec(PROOF_IV.getBytes()));
            byte[] decryptedBytes = cipher.doFinal(authReplyMoreFrame.getPayload().getPayload());
            byte[] serverChallenge = new byte[8];
            System.arraycopy(decryptedBytes, 9, serverChallenge, 0, 8);

            System.out.println("** Server Challenge ********************************");
            HexFunctions.printHexString(serverChallenge);
            System.out.println("****************************************************");
        } else {
            ByteBuf byteBuf = Unpooled.wrappedBuffer(authReplyMoreFrame.getPayload().getPayload());
            CephXServerChallenge serverChallenge = CephDecoder.decode(byteBuf, true, CephXServerChallenge.class);
            System.out.println("** Server Challenge ********************************");
            System.out.println(objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(serverChallenge));
            System.out.println("****************************************************");
        }
    }

    private void parseAuthRequestMore() throws Exception {
        System.out.println("[Client] Auth Request More");
        final AuthRequestMoreFrame authRequestMoreFrame = (AuthRequestMoreFrame) decodeFrame(clientByteBuf, true);
        System.out.println(objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(authRequestMoreFrame));
        if (sessionKey != null) {
            AuthRequestMoreAuthorizerPayload payload = (AuthRequestMoreAuthorizerPayload) authRequestMoreFrame.getPayload();
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, authKey, new IvParameterSpec(PROOF_IV.getBytes()));
            byte[] decryptedBytes = cipher.doFinal(payload.getTicket().getBlob());
            ByteBuf byteBuf = Unpooled.wrappedBuffer(decryptedBytes, 9, decryptedBytes.length - 9);
            CephXServiceTicketInfo ticketInfo = CephDecoder.decode(byteBuf, true, CephXServiceTicketInfo.class);

            System.out.println("** Ticket Info *************************************");
            System.out.println(objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(ticketInfo));

            System.out.println("** Decrypted Authorize *****************************");
            decryptedBytes = cipher.doFinal(payload.getEncryptedAuthMsg());
            byteBuf = Unpooled.wrappedBuffer(decryptedBytes);
            CephXAuthorize authorize = CephDecoder.decode(byteBuf, true, CephXAuthorize.class);
            System.out.println(objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(authorize));
            System.out.println("****************************************************");
        }
    }

    private void parseAuthDone() throws Exception {
        System.out.println("[Server] Auth Done");
        AuthDoneFrame authDoneFrame = (AuthDoneFrame) decodeFrame(serverByteBuf, false);
        System.out.println(objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(authDoneFrame));

        byte[] decryptedSecret = null;
        int decryptedSecretOffset = 0;
        if (sessionKey == null) {
            AuthDoneMonPayload payload = CephDecoder.decode(
                    Unpooled.wrappedBuffer(authDoneFrame.getSegment1().getPayload()),
                    true,
                    AuthDoneMonPayload.class
            );

            for (CephXTicketInfo ticketInfo : payload.getTicketInfos()) {
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
            byte[] encryptedSecret = payload.getEncryptedSecret();
            decryptedSecret = cipher.doFinal(encryptedSecret, 4, encryptedSecret.length - 4);
            decryptedSecretOffset = 13;
        } else {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, sessionKey, new IvParameterSpec(PROOF_IV.getBytes()));
            byte[] decryptedBytes = cipher.doFinal(
                    authDoneFrame.getSegment1().getPayload(),
                    4,
                    authDoneFrame.getSegment1().getPayload().length - 4
            );

            CephXAuthorizeReplyV2 authorizeReply = CephDecoder.decode(
                    Unpooled.wrappedBuffer(decryptedBytes, 9, decryptedBytes.length - 9),
                    true,
                    CephXAuthorizeReplyV2.class
            );
            decryptedSecret = authorizeReply.getConnectionSecret();

            System.out.println("** Authorize Reply *********************************");
            System.out.println(objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(authorizeReply));
            System.out.println("****************************************************");
        }

        streamKey = new SecretKeySpec(decryptedSecret, decryptedSecretOffset, 16, "AES");
        rxNonceBytes = new byte[12];
        System.arraycopy(decryptedSecret, decryptedSecretOffset + 16, rxNonceBytes, 0, 12);
        serverDecoder.enableSecureMode(streamKey, rxNonceBytes);
        txNonceBytes = new byte[12];
        System.arraycopy(decryptedSecret, decryptedSecretOffset + 28, txNonceBytes, 0, 12);
        clientDecoder.enableSecureMode(streamKey, txNonceBytes);
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

    private boolean haveMessages(boolean isClient) {
        if (isClient) {
            return clientByteBuf.readableBytes() > 0;
        } else {
            return serverByteBuf.readableBytes() > 0;
        }
    }

    private void parseControlFrame(boolean isClient) throws Exception {
        System.out.printf("[%s] Message\n", isClient ? "Client" : "Server");
        ControlFrame frame = decodeFrame(isClient ? clientByteBuf : serverByteBuf, isClient);
        System.out.println(objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(frame));
    }

    private ControlFrame decodeFrame(ByteBuf byteBuf, boolean isClient) throws Exception {
        PreParsedFrame preParsedFrame = isClient ? clientDecoder.decode(clientByteBuf) : serverDecoder.decode(serverByteBuf);
        ControlFrame controlFrame = preParsedFrame.getMessageType().getInstance();
        if (preParsedFrame.getSegment1() != null) {
            controlFrame.decodeSegment1(preParsedFrame.getSegment1().getSegmentByteBuf(), preParsedFrame.getSegment1().isLe());
            preParsedFrame.getSegment1().getSegmentByteBuf().release();
        }
        if (preParsedFrame.getSegment2() != null) {
            controlFrame.decodeSegment2(preParsedFrame.getSegment2().getSegmentByteBuf(), preParsedFrame.getSegment2().isLe());
            preParsedFrame.getSegment2().getSegmentByteBuf().release();
        }
        if (preParsedFrame.getSegment3() != null) {
            controlFrame.decodeSegment3(preParsedFrame.getSegment3().getSegmentByteBuf(), preParsedFrame.getSegment3().isLe());
            preParsedFrame.getSegment3().getSegmentByteBuf().release();
        }
        if (preParsedFrame.getSegment4() != null) {
            controlFrame.decodeSegment4(preParsedFrame.getSegment4().getSegmentByteBuf(), preParsedFrame.getSegment4().isLe());
            preParsedFrame.getSegment4().getSegmentByteBuf().release();
        }

        return controlFrame;
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

        while (decoder.haveMessages(true) || decoder.haveMessages(false)) {
            if (decoder.haveMessages(true)) {
                decoder.parseControlFrame(true);
            }
            if (decoder.haveMessages(false)) {
                decoder.parseControlFrame(false);
            }
        }
    }
}
