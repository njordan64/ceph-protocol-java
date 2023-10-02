package ca.venom.ceph.protocol.types;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class CephList<T extends CephDataType> implements CephDataType {
    private List<T> values;

    public CephList(List<T> values) {
        this.values = values;
    }

    public static <T extends  CephDataType> CephList<T> read(ByteBuffer byteBuffer, Class<T> clazz) {
        UInt32 count = UInt32.read(byteBuffer);
        List<T> values = new ArrayList<>();

        try {
            Method readMethod = clazz.getMethod("read", ByteBuffer.class);
            for (int i = 0; i < count.getValue(); i++) {
                values.add((T) readMethod.invoke(null, byteBuffer));
            }
        } catch (Exception e) {
            //
        }

        return new CephList<>(values);
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
    public void encode(ByteArrayOutputStream outputStream) {
        new UInt32(values.size()).encode(outputStream);
        values.forEach(v -> v.encode(outputStream));
    }

    @Override
    public void encode(ByteBuffer byteBuffer) {
        new UInt32(values.size()).encode(byteBuffer);
        values.forEach(v -> v.encode(byteBuffer));
    }
}
