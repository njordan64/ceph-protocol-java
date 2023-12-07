package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.CephDecoder;
import ca.venom.ceph.protocol.CephEncoder;
import ca.venom.ceph.protocol.DecodingException;
import ca.venom.ceph.protocol.frames.MessageFrame;
import ca.venom.ceph.protocol.types.annotations.CephField;
import ca.venom.ceph.protocol.types.annotations.CephType;
import ca.venom.ceph.protocol.types.EncodingException;
import ca.venom.ceph.protocol.types.mon.MonSubscribeItem;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class MonGetMap extends MessageBase {
    @CephType
    public static class Payload {
        @Getter
        @Setter
        @CephField
        private Map<String, MonSubscribeItem> what = new HashMap<>();

        @Getter
        @Setter
        @CephField(order = 2)
        private String hostname;
    }

    @Getter
    @Setter
    private Payload payload;

    @Override
    protected MessageType getType() {
        return MessageType.CEPH_MSG_MON_GET_MAP;
    }

    @Override
    protected void encodePayload(MessageFrame messageFrame) throws EncodingException {
        ByteBuf byteBuf = Unpooled.buffer();
        CephEncoder.encode(payload, byteBuf, true);

        byte[] bytes = new byte[byteBuf.writerIndex()];
        byteBuf.readBytes(bytes);

        MessageFrame.Segment segment = new MessageFrame.Segment();
        segment.setEncodedBytes(bytes);
        segment.setLe(true);
        messageFrame.setFront(segment);
    }

    @Override
    protected void decodePayload(MessageFrame messageFrame) throws DecodingException {
        payload = CephDecoder.decode(
                Unpooled.wrappedBuffer(messageFrame.getFront().getEncodedBytes()),
                messageFrame.getFront().isLe(),
                Payload.class
        );
    }
}
