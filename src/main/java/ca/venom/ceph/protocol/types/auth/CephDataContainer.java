package ca.venom.ceph.protocol.types.auth;

import ca.venom.ceph.protocol.types.CephDataType;
import ca.venom.ceph.protocol.types.UInt32;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public abstract class CephDataContainer implements CephDataType {
    protected CephDataContainer() {
    }

    protected CephDataContainer(ByteBuffer byteBuffer) {
        UInt32 length = UInt32.read(byteBuffer);
    }

    public int getSize() {
        return 4 + getPayloadSize();
    }

    protected abstract int getPayloadSize();

    public void encode(ByteArrayOutputStream stream) {
        new UInt32(getPayloadSize()).encode(stream);
        encodePayload(stream);
    }

    protected abstract void encodePayload(ByteArrayOutputStream stream);

    @Override
    public void encode(ByteBuffer byteBuffer) {
        ByteOrder originalOrder = byteBuffer.order();
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putInt(getPayloadSize());
        byteBuffer.order(originalOrder);

        encodePayload(byteBuffer);
    }

    protected abstract void encodePayload(ByteBuffer byteBuffer);
}
