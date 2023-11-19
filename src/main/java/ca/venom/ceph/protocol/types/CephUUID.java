package ca.venom.ceph.protocol.types;

import io.netty.buffer.ByteBuf;

import java.util.UUID;

public class CephUUID implements CephDataType {
    private UUID value;

    public CephUUID() {
    }

    public CephUUID(UUID value) {
        this.value = value;
    }

    public UUID getValue() {
        return value;
    }

    public void setValue(UUID value) {
        this.value = value;
    }

    @Override
    public int getSize() {
        return 16;
    }

    @Override
    public void encode(ByteBuf byteBuf, boolean le) {
        if (le) {
            byteBuf.writeLongLE(value.getLeastSignificantBits());
            byteBuf.writeLongLE(value.getMostSignificantBits());
        } else {
            byteBuf.writeLong(value.getMostSignificantBits());
            byteBuf.writeLong(value.getLeastSignificantBits());
        }
    }

    @Override
    public void decode(ByteBuf byteBuf, boolean le) {
        long leastSignificant;
        long mostSignificant;
        if (le) {
            leastSignificant = byteBuf.readLongLE();
            mostSignificant = byteBuf.readLongLE();
        } else {
            mostSignificant = byteBuf.readLong();
            leastSignificant = byteBuf.readLong();
        }

        value = new UUID(mostSignificant, leastSignificant);
    }
}
