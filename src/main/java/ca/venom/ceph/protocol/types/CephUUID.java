package ca.venom.ceph.protocol.types;

import ca.venom.ceph.protocol.types.annotations.ByteOrderPreference;
import ca.venom.ceph.protocol.types.annotations.CephField;
import ca.venom.ceph.protocol.types.annotations.CephType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@CephType()
public class CephUUID {
    @Getter
    @Setter
    @CephField(order = 1, byteOrderPreference = ByteOrderPreference.BE)
    private byte[] bytes;

    public UUID getUUID() {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
        long high = byteBuf.getLongLE(0);
        long low = byteBuf.getLongLE(8);

        return new UUID(high, low);
    }
}
