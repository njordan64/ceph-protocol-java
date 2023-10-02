package ca.venom.ceph.protocol.types;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class CephString implements CephDataType {
    private String value;

    public CephString(String value) {
        this.value = value;
    }

    public static CephString read(ByteBuffer byteBuffer) {
        ByteOrder originalOrder = byteBuffer.order();
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        int length = byteBuffer.getInt();
        byteBuffer.order(originalOrder);

        byte[] bytes = new byte[length];
        byteBuffer.get(bytes);

        return new CephString(new String(bytes));
    }
    @Override
    public int getSize() {
        return 4 + value.length();
    }

    @Override
    public void encode(ByteArrayOutputStream outputStream) {
        byte[] bytes = new byte[4];
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putInt(value.length());
        outputStream.writeBytes(bytes);

        outputStream.writeBytes(value.getBytes());
    }

    @Override
    public void encode(ByteBuffer byteBuffer) {
        ByteOrder originalOrder = byteBuffer.order();
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putInt(value.length());
        byteBuffer.order(originalOrder);

        byteBuffer.put(value.getBytes());
    }
}
