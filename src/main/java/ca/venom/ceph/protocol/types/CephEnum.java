package ca.venom.ceph.protocol.types;

import ca.venom.ceph.TypeNumEnum;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class CephEnum<T extends Enum<?> & TypeNumEnum> implements CephDataType {
    private T value;

    public CephEnum(T value) {
        this.value = value;
    }

    public static <T extends Enum<?> & TypeNumEnum> CephEnum<T> read(ByteBuffer byteBuffer, Class<T> clazz) {
        int valueNum = UInt8.read(byteBuffer).getValue();
        T[] values = clazz.getEnumConstants();
        for (T value : values) {
            if (valueNum == value.getValueInt()) {
                return new CephEnum<>(value);
            }
        }

        return new CephEnum<>(null);
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public int getSize() {
        return 1;
    }

    @Override
    public void encode(ByteArrayOutputStream outputStream) {
        outputStream.write(value.getValueInt());
    }

    @Override
    public void encode(ByteBuffer byteBuffer) {
        byteBuffer.put((byte) (0xff & value.getValueInt()));
    }
}
