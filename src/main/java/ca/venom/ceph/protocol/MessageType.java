package ca.venom.ceph.protocol;

import ca.venom.ceph.protocol.messages.Ack;
import ca.venom.ceph.protocol.messages.AuthBadMethod;
import ca.venom.ceph.protocol.messages.AuthDone;
import ca.venom.ceph.protocol.messages.AuthRequest;
import ca.venom.ceph.protocol.messages.AuthRequestMore;
import ca.venom.ceph.protocol.messages.AuthReplyMore;
import ca.venom.ceph.protocol.messages.AuthSignature;
import ca.venom.ceph.protocol.messages.ControlFrame;
import ca.venom.ceph.protocol.messages.Hello;
import ca.venom.ceph.protocol.messages.IdentMissingFeatures;
import ca.venom.ceph.protocol.messages.KeepAlive;
import ca.venom.ceph.protocol.messages.KeepAliveAck;
import ca.venom.ceph.protocol.messages.ReconnectOk;
import ca.venom.ceph.protocol.messages.Reset;
import ca.venom.ceph.protocol.messages.Retry;
import ca.venom.ceph.protocol.messages.RetryGlobal;
import ca.venom.ceph.protocol.messages.Wait;
import ca.venom.ceph.protocol.types.UInt8;

public enum MessageType {
    HELLO(1, Hello.class),
    AUTH_REQUEST(2, AuthRequest.class),
    AUTH_BAD_METHOD(3, AuthBadMethod.class),
    AUTH_REPLY_MORE(4, AuthReplyMore.class),
    AUTH_REQUEST_MORE(5, AuthRequestMore.class),
    AUTH_DONE(6, AuthDone.class),
    AUTH_SIGNATURE(7, AuthSignature.class),
    CLIENT_IDENT(8, null),
    SERVER_IDENT(9, null),
    IDENT_MISSING_FEATURES(10, IdentMissingFeatures.class),
    SESSION_RECONNECT(11, null),
    SESSION_RESET(12, Reset.class),
    SESSION_RETRY(13, Retry.class),
    SESSION_RETRY_GLOBAL(14, RetryGlobal.class),
    SESSION_RECONNECT_OK(15, ReconnectOk.class),
    WAIT(16, Wait.class),
    MESSAGE(17, null),
    KEEPALIVE2(18, KeepAlive.class),
    KEEPALIVE2_ACK(19, KeepAliveAck.class),
    ACK(20, Ack.class),
    COMPRESSION_REQUEST(21, null),
    COMPRESSION_DONE(22, null);

    private final UInt8 tagNum;
    private final Class<? extends ControlFrame> clazz;

    private MessageType(int tagNum, Class<? extends ControlFrame> clazz) {
        this.tagNum = new UInt8(tagNum);
        this.clazz = clazz;
    }

    public static MessageType getFromTagNum(UInt8 tagNum) {
        for (MessageType messageType : MessageType.values()) {
            if (messageType.tagNum.equals(tagNum)) {
                return messageType;
            }
        }

        return null;
    }

    public UInt8 getTagNum() {
        return tagNum;
    }

    public ControlFrame getInstance() {
        if (clazz == null) {
            return null;
        }

        try {
            return clazz.getConstructor().newInstance();
        } catch (Exception ex) {
            return null;
        }
    }
}
