package ca.venom.ceph.protocol.types;

import io.netty.buffer.ByteBuf;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CephMap<K extends CephDataType, V extends CephDataType> implements CephDataType {
    private Map<K, V> map;
    private final Class<K> keyClass;
    private final Class<V> valueClass;

    public CephMap(Class<K> keyClass, Class<V> valueClass) {
        this.keyClass = keyClass;
        this.valueClass = valueClass;
    }

    public CephMap(Map<K, V> map, Class<K> keyClass, Class<V> valueClass) {
        this.map = map;
        this.keyClass = keyClass;
        this.valueClass = valueClass;
    }

    public Map<K, V> getMap() {
        return map;
    }

    public void setMap(Map<K, V> map) {
        this.map = map;
    }

    @Override
    public int getSize() {
        int size = 4;
        for (Map.Entry<K, V> entry : map.entrySet()) {
            size += entry.getKey().getSize();
            size += entry.getValue().getSize();
        }

        return size;
    }

    @Override
    public void encode(ByteBuf byteBuf, boolean le) {
        if (le) {
            byteBuf.writeIntLE(map.size());
        } else {
            byteBuf.writeInt(map.size());
        }

        for (Map.Entry<K, V> entry : map.entrySet()) {
            entry.getKey().encode(byteBuf, le);
            entry.getValue().encode(byteBuf, le);
        }
    }

    @Override
    public void decode(ByteBuf byteBuf, boolean le) {
        int entryCount;
        if (le) {
            entryCount = byteBuf.readIntLE();
        } else {
            entryCount = byteBuf.readInt();
        }

        if (entryCount == 0) {
            map = Collections.emptyMap();
        }

        map = new HashMap<>();
        try {
            Constructor<K> keyConstructor = keyClass.getConstructor();
            Constructor<V> valueConstructor = valueClass.getConstructor();
            for (int i = 0; i < entryCount; i++) {
                K key = keyConstructor.newInstance();
                key.decode(byteBuf, le);
                V value = valueConstructor.newInstance();
                value.decode(byteBuf, le);

                map.put(key, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
