package ca.venom.ceph.protocol;

import ca.venom.ceph.protocol.messages.*;
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
    IDENT_MISSING_FEATURES(10, null),
    SESSION_RECONNECT(11, null),
    SESSION_RESET(12, Reset.class),
    SESSION_RETRY(13, Retry.class),
    SESSION_RETRY_GLOBAL(14, RetryGlobal.class),
    SESSION_RECONNECT_OK(15, ReconnectOk.class),
    WAIT(16, Wait.class),
    MESSAGE(17, null),
    KEEPALIVE2(18, null),
    KEEPALIVE2_ACK(19, null),
    ACK(20, null),
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
