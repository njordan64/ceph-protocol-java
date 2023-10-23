package ca.venom.ceph.protocol.types;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

public class CephString implements CephDataType {
    private String value;

    public CephString() {
    }

    public CephString(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public int getSize() {
        return 4 + value.length();
    }

    @Override
    public void encode(ByteBuf byteBuf, boolean le) {
        int length = 0;
        byte[] bytes = null;
        if (value != null) {
            bytes = value.getBytes(StandardCharsets.UTF_8);
            length = bytes.length;
        }

        if (le) {
            byteBuf.writeIntLE(length);
        } else {
            byteBuf.writeInt(length);
        }

        if (length > 0) {
            byteBuf.writeBytes(bytes);
        }
    }

    @Override
    public void decode(ByteBuf byteBuf, boolean le) {
        int length;
        if (le) {
            length = byteBuf.readIntLE();
        } else {
            length = byteBuf.readInt();
        }

        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);

        value = new String(bytes, StandardCharsets.UTF_8);
    }
}
