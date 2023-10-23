package ca.venom.ceph.protocol.types;

import ca.venom.ceph.EnumWithIntValue;
import io.netty.buffer.ByteBuf;

public class CephEnum<T extends Enum<?> & EnumWithIntValue> implements CephDataType {
    private T value;
    private final Class<T> clazz;

    public CephEnum(Class<T> clazz) {
        this.clazz = clazz;
    }

    public CephEnum(T value) {
        this.value = value;
        this.clazz = (Class<T>) value.getClass();
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
    public void encode(ByteBuf byteBuf, boolean le) {
        byteBuf.writeByte(0xff & value.getValueInt());
    }

    @Override
    public void decode(ByteBuf byteBuf, boolean le) {
        this.value = null;

        int valueNum = 0xff & byteBuf.readByte();
        T[] values = clazz.getEnumConstants();
        for (T value : values) {
            if (valueNum == value.getValueInt()) {
                this.value = value;
            }
        }
    }
}
