package ca.venom.ceph.protocol;

import ca.venom.ceph.protocol.messages.HelloMessage;
import ca.venom.ceph.protocol.messages.MessageBase;
import ca.venom.ceph.protocol.types.UInt8;

public enum MessageType {
    HELLO(1, HelloMessage.class),
    AUTH_REQUEST(2, null),
    AUTH_BAD_METHOD(3, null),
    AUTH_REPLY_MORE(4, null),
    AUTH_REQUEST_MORE(5, null),
    AUTH_DONE(6, null),
    AUTH_SIGNATURE(7, null),
    CLIENT_IDENT(8, null),
    SERVER_IDENT(9, null),
    IDENT_MISSING_FEATURES(10, null),
    SESSION_RECONNECT(11, null),
    SESSION_RESET(12, null),
    SESSION_RETRY(13, null),
    SESSION_RETRY_GLOBAL(14, null),
    SESSION_RECONNECT_OK(15, null),
    WAIT(16, null),
    MESSAGE(17, null),
    KEEPALIVE2(18, null),
    KEEPALIVE2_ACK(19, null),
    ACK(20, null),
    COMPRESSION_REQUEST(21, null),
    COMPRESSION_DONE(22, null);

    private final UInt8 tagNum;
    private final Class<? extends MessageBase> clazz;

    private MessageType(int tagNum, Class<? extends MessageBase> clazz) {
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

    public MessageBase getInstance() {
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
