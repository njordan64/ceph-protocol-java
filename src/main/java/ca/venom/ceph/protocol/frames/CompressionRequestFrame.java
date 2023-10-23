package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.CephBoolean;
import ca.venom.ceph.protocol.types.CephList;
import ca.venom.ceph.protocol.types.Int32;
import io.netty.buffer.ByteBuf;

import java.util.List;

public class CompressionRequestFrame extends ControlFrame {
    private CephBoolean compress;
    private CephList<Int32> preferredMethods;

    public boolean isCompress() {
        return compress.getValue();
    }

    public void setCompress(boolean compress) {
        this.compress = new CephBoolean(compress);
    }

    public List<Int32> getPreferredMethods() {
        return preferredMethods.getValues();
    }

    public void setPreferredMethods(List<Int32> preferredMethods) {
        this.preferredMethods = new CephList<>(preferredMethods, Int32.class);
    }

    @Override
    public void encodeSegment1(ByteBuf byteBuf, boolean le) {
        compress.encode(byteBuf, le);
        preferredMethods.encode(byteBuf, le);
    }

    @Override
    public void decodeSegment1(ByteBuf byteBuf, boolean le) {
        compress = new CephBoolean();
        compress.decode(byteBuf, le);

        preferredMethods = new CephList<>(Int32.class);
        preferredMethods.decode(byteBuf, le);
    }

    @Override
    public MessageType getTag() {
        return MessageType.COMPRESSION_REQUEST;
    }
}
