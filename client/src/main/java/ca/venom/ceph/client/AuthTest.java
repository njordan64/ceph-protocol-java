/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.client;

import ca.venom.ceph.protocol.AuthMode;
import ca.venom.ceph.protocol.CephProtocolContext;
import ca.venom.ceph.protocol.ControlFrameType;
import ca.venom.ceph.protocol.NodeType;
import ca.venom.ceph.protocol.frames.AuthDoneFrame;
import ca.venom.ceph.protocol.frames.AuthReplyMoreFrame;
import ca.venom.ceph.protocol.frames.AuthRequestFrame;
import ca.venom.ceph.protocol.frames.AuthRequestMoreFrame;
import ca.venom.ceph.protocol.frames.AuthSignatureFrame;
import ca.venom.ceph.protocol.frames.BannerFrame;
import ca.venom.ceph.protocol.frames.HelloFrame;
import ca.venom.ceph.protocol.types.AddrIPv4;
import ca.venom.ceph.protocol.types.auth.AuthDoneMonPayload;
import ca.venom.ceph.protocol.types.auth.AuthRequestMoreMonPayload;
import ca.venom.ceph.protocol.types.auth.AuthRequestMonPayload;
import ca.venom.ceph.protocol.types.auth.CephXAuthenticate;
import ca.venom.ceph.protocol.types.auth.CephXRequestHeader;
import ca.venom.ceph.protocol.types.auth.CephXTicketBlob;
import ca.venom.ceph.protocol.types.auth.CephXTicketInfo;
import ca.venom.ceph.protocol.types.auth.EntityName;
import ca.venom.ceph.utils.HexFunctions;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class AuthTest {
    private static final byte[] MAGIC_BYTES = new byte[] {
            (byte) 0x55, (byte) 0xaa, (byte) 0x26, (byte) 0x88,
            (byte) 0xad, (byte) 0x9c, (byte) 0x00, (byte) 0xff
    };

    private static final String PROOF_IV = "cephsageyudagreg";

    private static SecretKey streamKey;
    private static byte[] rxNonceBytes;
    private static ByteBuffer rxNonceByteBuffer;
    private static byte[] txNonceBytes;
    private static ByteBuffer txNonceByteBuffer;
    private static CephProtocolContext ctx;

    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            System.err.println("Need hostname and port");
        }
        final String hostname = args[0];
        final int port = Integer.parseInt(args[1]);
        final String username = args[2];
        final String userKey = args[3];
        Socket socket = new Socket(hostname, port);

        ctx = new CephProtocolContext();
        ctx.setRev1(true);
        ctx.setSecureMode(CephProtocolContext.SecureMode.CRC);

        sendBanner(socket);
        processBanner(socket);

        sendHello(socket);
        processHello(socket);

        sendAuthRequest(socket, username);
        processAuthReplyMore(socket, userKey);

        processAuthDone(socket, userKey);
        processAuthSignature(socket);

        socket.close();
    }

    private static void sendBanner(Socket socket) throws IOException {
        BannerFrame bannerFrame = new BannerFrame();
        bannerFrame.setRevision1Supported(true);
        bannerFrame.setRevision1Required(true);
        bannerFrame.setCompressionSupported(false);
        bannerFrame.setCompressionRequired(false);
        bannerFrame.encode(socket.getOutputStream());
    }

    private static void processBanner(Socket socket) throws IOException {
        BannerFrame bannerFrame = new BannerFrame();
        //bannerFrame.decode(socket.getInputStream());

        if (!bannerFrame.isRevision1Supported()) {
            throw new IOException("Only revision message version 2.1 is supported");
        }

        if (bannerFrame.isCompressionRequired()) {
            throw new IOException("Compression is not supported");
        }
    }

    private static void sendHello(Socket socket) throws IOException {
        HelloFrame helloFrame = new HelloFrame();
        helloFrame.setSegment1(new HelloFrame.Segment1());
        helloFrame.getSegment1().setNodeType(NodeType.CLIENT);
        AddrIPv4 addr = new AddrIPv4();
        helloFrame.getSegment1().setAddr(addr);
        addr.setNonce(0);
        addr.setPort((short) socket.getLocalPort());
        addr.setAddrBytes(socket.getLocalAddress().getAddress());

        //socket.getOutputStream().write(helloFrame.encode(ctx));
    }

    private static void processHello(Socket socket) throws IOException {
        int msgType = socket.getInputStream().read();
        if (msgType != ControlFrameType.HELLO.getTagNum()) {
            throw new IOException("Unexpected message type: " + msgType);
        }

        HelloFrame helloFrame = new HelloFrame();
        //helloFrame.decode(socket.getInputStream(), ctx);
    }

    private static void sendAuthRequest(Socket socket, String username) throws IOException {
        AuthRequestFrame authRequestFrame = new AuthRequestFrame();
        authRequestFrame.getSegment1().setAuthMethod(2);
        List<Integer> preferredModes = new ArrayList<>();
        preferredModes.add(2);
        preferredModes.add(1);
        authRequestFrame.getSegment1().setPreferredModes(preferredModes);

        AuthRequestMonPayload authRequestPayload = new AuthRequestMonPayload();
        authRequestPayload.setAuthMode(AuthMode.MON);
        authRequestPayload.setEntityName(new EntityName());
        authRequestPayload.getEntityName().setType(8);
        authRequestPayload.getEntityName().setEntityName(username);
        authRequestPayload.setGlobalId(0L);
        authRequestFrame.getSegment1().setPayload(authRequestPayload);

        //socket.getOutputStream().write(authRequestFrame.encode(ctx));
    }

    private static void processAuthReplyMore(Socket socket, String userKey) throws Exception {
        int msgType = socket.getInputStream().read();
        if (msgType != ControlFrameType.AUTH_REPLY_MORE.getTagNum()) {
            throw new IOException("Unexpected message type: " + msgType);
        }

        AuthReplyMoreFrame replyMore = new AuthReplyMoreFrame();
        //replyMore.decode(socket.getInputStream(), ctx);

        byte[] clientChallenge = new byte[8];
        SecureRandom random = new SecureRandom();
        random.nextBytes(clientChallenge);

        byte[] serverChallenge = new byte[8];
        ByteBuffer serverChallengeBuffer = ByteBuffer.wrap(serverChallenge);
        //replyMore.getPayload().getServerChallenge().getServerChallenge().encode(serverChallengeBuffer);

        byte[] unencryptedBytes = new byte[32];
        unencryptedBytes[0] = 1;
        System.arraycopy(MAGIC_BYTES, 0, unencryptedBytes, 1, 8);
        System.arraycopy(serverChallenge, 0, unencryptedBytes, 9, 8);
        System.arraycopy(clientChallenge, 0, unencryptedBytes, 17, 8);
        Arrays.fill(unencryptedBytes, 25, 32, (byte) (32 - 25));

        SecretKeySpec secretKey = new SecretKeySpec(Base64.getDecoder().decode(userKey), 12, 16, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(PROOF_IV.getBytes()));
        byte[] encryptedBytes = cipher.doFinal(unencryptedBytes);

        byte[] readyForXor = new byte[36];
        readyForXor[0] = (byte) 32;
        System.arraycopy(encryptedBytes, 0, readyForXor, 4, 32);

        byte[] proof = new byte[8];
        for (int i = 0; i - (i % 8) + 8 < readyForXor.length; i++) {
            proof[i % 8] ^= readyForXor[i];
        }

        AuthRequestMoreFrame requestMore = new AuthRequestMoreFrame();
        AuthRequestMoreMonPayload requestMorePayload = new AuthRequestMoreMonPayload();
        requestMore.setPayload(requestMorePayload);
        CephXRequestHeader requestHeader = new CephXRequestHeader();
        requestHeader.setRequestType((short) 0x100);
        requestMorePayload.setRequestHeader(requestHeader);

        System.out.println("-------------------------------------------------");
        System.out.println("Proof");
        HexFunctions.printHexString(proof);
        System.out.println("-------------------------------------------------");

        CephXTicketBlob blob = new CephXTicketBlob();
        blob.setSecretId(0L);
        blob.setBlob(new byte[0]);
        CephXAuthenticate authenticate = new CephXAuthenticate();
        authenticate.setClientChallenge(clientChallenge);
        authenticate.setKey(proof);
        authenticate.setOldTicket(blob);
        authenticate.setOtherKeys(32);
        requestMorePayload.setAuthenticate(authenticate);

        //socket.getOutputStream().write(requestMore.encode(ctx));
    }

    private static void processAuthDone(Socket socket, String userKey) throws Exception {
        int msgType = socket.getInputStream().read();
        if (msgType != ControlFrameType.AUTH_DONE.getTagNum()) {
            throw new IOException("Unexpected message type: " + msgType);
        }

        AuthDoneFrame authDone = new AuthDoneFrame();
        //authDone.decode(socket.getInputStream(), ctx);
        AuthDoneMonPayload payload = null;
        System.out.println(">>> Global ID: " + authDone.getSegment1().getGlobalId());
        System.out.println(">>> Connection Mode: " + authDone.getSegment1().getConnectionMode());
        System.out.println(">>> Response Type: " + payload.getResponseHeader().getResponseType());
        System.out.println(">>> Status: " + payload.getResponseHeader().getStatus());

        SecretKeySpec sessionKey = null;
        for (CephXTicketInfo ticketInfo : payload.getTicketInfos()) {
            System.out.println("  >>> Service ID: " + ticketInfo.getServiceId());

            SecretKeySpec secretKey = new SecretKeySpec(Base64.getDecoder().decode(userKey), 12, 16, "AES");
            byte[] iv = PROOF_IV.getBytes();
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
            byte[] decryptedBytes = cipher.doFinal(ticketInfo.getServiceTicket());
            ByteBuffer decryptedByteBuffer = ByteBuffer.wrap(decryptedBytes);
            decryptedByteBuffer.position(9);
            //CephXServiceTicket serviceTicket = CephXServiceTicket.read(decryptedByteBuffer);
            //System.out.println("  >>> Service Ticket Type: " + serviceTicket.getSessionKey().getType().getValue());

            //sessionKey = new SecretKeySpec(serviceTicket.getSessionKey().getSecret().getValue(), "AES");

            if (!ticketInfo.isEncrypted()) {
                decryptedByteBuffer = ByteBuffer.wrap(ticketInfo.getTicket());
                //CephXTicketBlob ticketBlob = CephXTicketBlob.read(decryptedByteBuffer);
                //System.out.println("  >>> Secret ID: " + ticketBlob.getSecretId().getValue());
                //HexFunctions.printHexString(ticketBlob.getBlob().getValue());
            }
        }

        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, sessionKey, new IvParameterSpec(PROOF_IV.getBytes()));
        System.out.println(">>> Extra:");
        byte[] encryptedSecret = payload.getEncryptedSecret();
        byte[] decryptedSecret = cipher.doFinal(encryptedSecret, 4, encryptedSecret.length - 4);

        streamKey = new SecretKeySpec(decryptedSecret, 13, 16, "AES");
        rxNonceBytes = new byte[12];
        System.arraycopy(decryptedSecret, 29, rxNonceBytes, 0, 12);
        rxNonceByteBuffer = ByteBuffer.wrap(rxNonceBytes);
        txNonceBytes = new byte[12];
        System.arraycopy(decryptedSecret, 41, txNonceBytes, 0, 12);
        txNonceByteBuffer = ByteBuffer.wrap(txNonceBytes);
    }

    private static void processAuthSignature(Socket socket) throws Exception {
        byte[] encryptedBytes = new byte[96];
        int bytesRead = socket.getInputStream().read(encryptedBytes);
        if (bytesRead < encryptedBytes.length) {
            throw new IOException("Unable to read all bytes");
        }

        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, rxNonceBytes);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, streamKey,gcmParameterSpec);
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        ByteArrayInputStream decryptedStream = new ByteArrayInputStream(decryptedBytes);
        int msgType = decryptedStream.read();
        if (msgType != ControlFrameType.AUTH_SIGNATURE.getTagNum()) {
            throw new IOException("Wrong message type, expected an Auth Signature");
        }

        AuthSignatureFrame signatureFrame = new AuthSignatureFrame();
        ctx.setSecureMode(CephProtocolContext.SecureMode.SECURE);
        //signatureFrame.decode(decryptedStream, ctx);
        HexFunctions.printHexString(signatureFrame.getSegment1().getSha256Digest());
    }
}
