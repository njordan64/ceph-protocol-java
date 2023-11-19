package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.frames.MessageFrame;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class MonMap extends MessageBase {
    private ByteBuf monmapByteBuf;

    @Override
    protected MessageType getType() {
        return MessageType.CEPH_MSG_MON_MAP;
    }

    public ByteBuf getMonmapByteBuf() {
        return monmapByteBuf;
    }

    public void setMonmapByteBuf(ByteBuf monmapByteBuf) {
        this.monmapByteBuf = monmapByteBuf;
    }

    @Override
    protected void encodePayload(MessageFrame messageFrame) {
        int length = monmapByteBuf.writerIndex() - monmapByteBuf.readerIndex();
        ByteBuf frontByteBuf = Unpooled.buffer(4 + length);
        frontByteBuf.writeIntLE(length);
        frontByteBuf.writeBytes(monmapByteBuf, monmapByteBuf.readerIndex(), length);

        messageFrame.setFront(frontByteBuf);
        messageFrame.setFrontLE(true);
    }

    @Override
    protected void decodePayload(MessageFrame messageFrame) {
        ByteBuf byteBuf = messageFrame.getFront();
        boolean le = messageFrame.isFrontLE();

        int length;
        if (le) {
            length = byteBuf.readIntLE();
        } else {
            length = byteBuf.readInt();
        }

        monmapByteBuf = Unpooled.buffer(length);
        byteBuf.readBytes(monmapByteBuf, length);
    }
}
