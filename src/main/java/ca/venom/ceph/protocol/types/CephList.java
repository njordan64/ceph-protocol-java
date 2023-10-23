package ca.venom.ceph.protocol.types;

import io.netty.buffer.ByteBuf;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class CephList<T extends CephDataType> implements CephDataType {
    private List<T> values;
    private final Class<T> clazz;

    public CephList(Class<T> clazz) {
        this.clazz = clazz;
    }

    public CephList(List<T> values, Class<T> clazz) {
        this.values = values;
        this.clazz = clazz;
    }

    public List<T> getValues() {
        return values;
    }

    public void setValues(List<T> values) {
        this.values = values;
    }

    @Override
    public int getSize() {
        int size = 4;
        for (T value : values) {
            size += value.getSize();
        }

        return size;
    }

    @Override
    public void encode(ByteBuf byteBuf, boolean le) {
        if (le) {
            byteBuf.writeIntLE(values.size());
        } else {
            byteBuf.writeInt(values.size());
        }

        values.forEach(v -> v.encode(byteBuf, le));
    }

    @Override
    public void decode(ByteBuf byteBuf, boolean le) {
        int count;
        if (le) {
            count = byteBuf.readIntLE();
        } else {
            count = byteBuf.readInt();
        }

        try {
            values = new ArrayList<>();
            Constructor<T> constructor = clazz.getConstructor();
            for (int i = 0; i < count; i++) {
                T value = constructor.newInstance();
                value.decode(byteBuf, le);

                values.add(value);
            }
        } catch (Exception e) {
            //
        }
    }
}
