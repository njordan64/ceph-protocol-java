package ca.venom.ceph.client.codecs;

import ca.venom.ceph.protocol.AuthMode;
import ca.venom.ceph.protocol.CephDecoder;
import ca.venom.ceph.protocol.frames.AuthDoneFrame;
import ca.venom.ceph.protocol.frames.AuthFrameBase;
import ca.venom.ceph.protocol.frames.AuthReplyMoreFrame;
import ca.venom.ceph.protocol.frames.AuthRequestFrame;
import ca.venom.ceph.protocol.frames.AuthRequestMoreFrame;
import ca.venom.ceph.protocol.frames.AuthSignatureFrame;
import ca.venom.ceph.protocol.types.auth.CephXServiceTicket;
import ca.venom.ceph.protocol.types.auth.AuthRequestMorePayload;
import ca.venom.ceph.protocol.types.auth.AuthRequestPayload;
import ca.venom.ceph.protocol.types.auth.CephXAuthenticate;
import ca.venom.ceph.protocol.types.auth.CephXRequestHeader;
import ca.venom.ceph.protocol.types.auth.CephXTicketBlob;
import ca.venom.ceph.protocol.types.auth.CephXTicketInfo;
import ca.venom.ceph.protocol.types.auth.EntityName;
import ca.venom.ceph.utils.HexFunctions;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AuthHandler extends SimpleChannelInboundHandler<AuthFrameBase> {
    private static final Logger LOG = LoggerFactory.getLogger(AuthHandler.class);
    private static final byte[] MAGIC_BYTES = new byte[] {
            (byte) 0x55, (byte) 0xaa, (byte) 0x26, (byte) 0x88,
            (byte) 0xad, (byte) 0x9c, (byte) 0x00, (byte) 0xff
    };
    private static final String PROOF_IV = "cephsageyudagreg";

    private enum State {
        NONE,
        INITIATED,
        PROOF_SENT,
        COMPLETE
    }

    private final String username;
    private final SecretKeySpec authKey;
    private State state = State.NONE;
    private SecretKeySpec sessionKey;
    private ByteBuf sentByteBuf;
    private ByteBuf receivedByteBuf;
    private CompletableFuture<Void> authFinished;

    public AuthHandler(String username, String keyString) {
        this.username = username;

        byte[] keyBytes = Base64.getDecoder().decode(keyString);
        authKey = new SecretKeySpec(keyBytes, 12, 16, "AES");
    }

    public CompletableFuture<Void> start(Channel channel) {
        LOG.debug(">>> AuthHandler.start");

        synchronized (this) {
            if (authFinished != null) {
                CompletableFuture<Void> authInProgress = new CompletableFuture<>();
                authInProgress.completeExceptionally(new IllegalStateException("Auth in progress"));
                return authInProgress;
            } else {
                authFinished = new CompletableFuture<>();
            }
        }

        if (state == State.COMPLETE) {
            authFinished.complete(null);
            CompletableFuture<Void> toReturn = authFinished;

            synchronized (this) {
                authFinished = null;
            }

            return toReturn;
        }

        AuthRequestFrame request = new AuthRequestFrame();
        request.setSegment1(new AuthRequestFrame.Segment1());
        request.getSegment1().setAuthMethod(2);

        List<Integer> preferredModes = new ArrayList<>(2);
        preferredModes.add(2);
        preferredModes.add(1);
        request.getSegment1().setPreferredModes(preferredModes);

        AuthRequestPayload authRequestPayload = new AuthRequestPayload();
        authRequestPayload.setAuthMode(AuthMode.MON);
        authRequestPayload.setEntityName(new EntityName());
        authRequestPayload.getEntityName().setType(8);
        authRequestPayload.getEntityName().setEntityName(username);
        authRequestPayload.setGlobalId(0L);
        request.getSegment1().setPayload(authRequestPayload);

        state = State.INITIATED;

        channel.writeAndFlush(request);

        return authFinished;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AuthFrameBase frame) throws Exception {
        LOG.debug(">>> AuthHandler.channelRead0: " + frame.getClass().getName());

        if (frame instanceof AuthReplyMoreFrame replyMoreFrame) {
            handeAuthRequestMore(ctx, replyMoreFrame);
        } else if (frame instanceof AuthDoneFrame authDoneFrame) {
            handleAuthDone(ctx, authDoneFrame);
        } else if (frame instanceof AuthSignatureFrame authSignatureFrame) {
            handleAuthSignature(ctx, authSignatureFrame);
        }
    }

    private void handeAuthRequestMore(ChannelHandlerContext ctx, AuthReplyMoreFrame request) throws Exception {
        switch (state) {
            case NONE:
                LOG.debug("Unexpected auth request more frame");
                throw new IllegalStateException("Unexpected auth request more frame");
            case PROOF_SENT:
            case COMPLETE:
                LOG.debug("Unexpected auth request more frame, auth complete");
                return;
        }

        byte[] clientChallenge = new byte[8];
        SecureRandom random = new SecureRandom();
        random.nextBytes(clientChallenge);

        byte[] serverChallenge = request.getPayload().getServerChallenge().getServerChallenge();

        byte[] unencryptedBytes = new byte[32];
        unencryptedBytes[0] = 1;
        System.arraycopy(MAGIC_BYTES, 0, unencryptedBytes, 1, 8);
        System.arraycopy(serverChallenge, 0, unencryptedBytes, 9, 8);
        System.arraycopy(clientChallenge, 0, unencryptedBytes, 17, 8);
        Arrays.fill(unencryptedBytes, 25, 32, (byte) (32 - 25));

        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, authKey, new IvParameterSpec(PROOF_IV.getBytes()));
        byte[] encryptedBytes = cipher.doFinal(unencryptedBytes);

        byte[] readyForXor = new byte[36];
        readyForXor[0] = (byte) 32;
        System.arraycopy(encryptedBytes, 0, readyForXor, 4, 32);

        byte[] proof = new byte[8];
        for (int i = 0; i - (i % 8) + 8 < readyForXor.length; i++) {
            proof[i % 8] ^= readyForXor[i];
        }

        AuthRequestMoreFrame requestMore = new AuthRequestMoreFrame();
        AuthRequestMorePayload requestMorePayload = new AuthRequestMorePayload();
        requestMore.setPayload(requestMorePayload);
        CephXRequestHeader requestHeader = new CephXRequestHeader();
        requestHeader.setRequestType((short) 0x100);
        requestMorePayload.setRequestHeader(requestHeader);

        CephXTicketBlob blob = new CephXTicketBlob();
        blob.setSecretId(0L);
        blob.setBlob(new byte[0]);
        CephXAuthenticate authenticate = new CephXAuthenticate();
        authenticate.setClientChallenge(clientChallenge);
        authenticate.setKey(proof);
        authenticate.setOldTicket(blob);
        authenticate.setOtherKeys(32);
        requestMorePayload.setAuthenticate(authenticate);

        state = State.PROOF_SENT;

        ctx.writeAndFlush(requestMore).sync();
    }

    private void handleAuthDone(ChannelHandlerContext ctx, AuthDoneFrame request) throws Exception {
        for (CephXTicketInfo ticketInfo : request.getSegment1().getPayload().getTicketInfos()) {
            byte[] iv = PROOF_IV.getBytes();
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, authKey, new IvParameterSpec(iv));
            byte[] decryptedBytes = cipher.doFinal(ticketInfo.getServiceTicket());

            ByteBuf decryptedByteBuf = Unpooled.wrappedBuffer(decryptedBytes);
            decryptedByteBuf.skipBytes(9);
            CephXServiceTicket serviceTicket = CephDecoder.decode(decryptedByteBuf, true, CephXServiceTicket.class);

            sessionKey = new SecretKeySpec(serviceTicket.getSessionKey().getSecret(), "AES");

            if (!ticketInfo.isEncrypted()) {
                decryptedByteBuf = Unpooled.wrappedBuffer(ticketInfo.getTicket());
                CephXTicketBlob ticketBlob = CephDecoder.decode(decryptedByteBuf, true, CephXTicketBlob.class);
            }
        }

        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, sessionKey, new IvParameterSpec(PROOF_IV.getBytes()));
        byte[] encryptedSecret = request.getSegment1().getPayload().getEncryptedSecret();
        byte[] decryptedSecret = cipher.doFinal(encryptedSecret, 4, encryptedSecret.length - 4);

        SecretKey streamKey = new SecretKeySpec(decryptedSecret, 13, 16, "AES");
        byte[] rxNonceBytes = new byte[12];
        System.arraycopy(decryptedSecret, 29, rxNonceBytes, 0, 12);
        byte[] txNonceBytes = new byte[12];
        System.arraycopy(decryptedSecret, 41, txNonceBytes, 0, 12);

        state = State.COMPLETE;

        CephPreParsedFrameCodec preParsedFrameCodec = ctx.pipeline().get(CephPreParsedFrameCodec.class);
        if (request.getSegment1().getConnectionMode() == 2) {
            preParsedFrameCodec.enableSecureMode(streamKey, rxNonceBytes, txNonceBytes);
        }

        sentByteBuf = preParsedFrameCodec.getSentByteBuf();
        receivedByteBuf =  preParsedFrameCodec.getReceivedByteBuf();
        preParsedFrameCodec.releaseSentByteBuf();
        preParsedFrameCodec.releaseReceivedByteBuf();
        preParsedFrameCodec.setCaptureBytes(false);

        ctx.channel().config().setAutoRead(true);
    }

    private void handleAuthSignature(ChannelHandlerContext ctx, AuthSignatureFrame request) throws Exception {
        LOG.debug(">>> AuthHandler.handleAuthSignature");

        byte[] x = new byte[sentByteBuf.writerIndex()];
        sentByteBuf.getBytes(0, x);
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(sessionKey);

        byte[] sentBytes = new byte[sentByteBuf.writerIndex()];
        sentByteBuf.readBytes(sentBytes);
        HexFunctions.printHexString(sentBytes);

        byte[] expectedSignature = request.getSegment1().getSha256Digest();
        byte[] actualSignature = mac.doFinal(sentBytes);
        for (int i = 0; i < 32; i++) {
            if (expectedSignature[i] != actualSignature[i]) {
                throw new Exception("Invalid auth signature");
            }
        }

        sentByteBuf = null;

        mac = Mac.getInstance("HmacSHA256");
        mac.init(sessionKey);

        byte[] receivedBytes = new byte[receivedByteBuf.writerIndex()];
        receivedByteBuf.readBytes(receivedBytes);

        AuthSignatureFrame signatureFrame = new AuthSignatureFrame();
        signatureFrame.setSegment1(new AuthSignatureFrame.Segment1());
        signatureFrame.getSegment1().setSha256Digest(mac.doFinal(receivedBytes));

        receivedByteBuf = null;

        ctx.writeAndFlush(signatureFrame).sync();

        authFinished.complete(null);
        synchronized (this) {
            authFinished = null;
        }
    }
}
