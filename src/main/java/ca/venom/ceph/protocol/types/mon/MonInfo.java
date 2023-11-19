package ca.venom.ceph.protocol.types.mon;

import ca.venom.ceph.protocol.types.AddrVec;
import ca.venom.ceph.protocol.types.CephDataType;
import ca.venom.ceph.protocol.types.CephMap;
import ca.venom.ceph.protocol.types.CephString;
import ca.venom.ceph.protocol.types.Int16;
import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

public class MonInfo implements CephDataType {
    private static final byte VERSION = 5;
    private static final byte MIN_VERSION = 1;

    private CephString name;
    private AddrVec publicAddrs;
    private Int16 priority;
    private Int16 weight;
    private CephMap<CephString, CephString> crushLoc;

    public String getName() {
        return name.getValue();
    }

    public void setName(String name) {
        this.name = new CephString(name);
    }

    public AddrVec getPublicAddrs() {
        return publicAddrs;
    }

    public void setPublicAddrs(AddrVec publicAddrs) {
        this.publicAddrs = publicAddrs;
    }

    public short getPriority() {
        return priority.getValue();
    }

    public void setPriority(short priority) {
        this.priority = new Int16(priority);
    }

    public short getWeight() {
        return weight.getValue();
    }

    public void setWeight(short weight) {
        this.weight = new Int16(weight);
    }

    public Map<String, String> getCrushLoc() {
        Map<String, String> simplified = new HashMap<>();
        for (Map.Entry<CephString, CephString> entry : crushLoc.getMap().entrySet()) {
            simplified.put(entry.getKey().getValue(), entry.getValue().getValue());
        }

        return simplified;
    }

    public void setCrushLoc(Map<String, String> crushLoc) {
        this.crushLoc = new CephMap<>(CephString.class, CephString.class);
        Map<CephString, CephString> values = new HashMap<>();
        this.crushLoc.setMap(values);

        for (Map.Entry<String, String> entry : crushLoc.entrySet()) {
            values.put(new CephString(entry.getKey()), new CephString(entry.getValue()));
        }
    }

    @Override
    public int getSize() {
        return 6 +
                name.getSize() +
                publicAddrs.getSize() +
                priority.getSize() +
                weight.getSize() +
                crushLoc.getSize();
    }

    @Override
    public void encode(ByteBuf byteBuf, boolean le) {
        byteBuf.writeByte(VERSION);
        byteBuf.writeByte(MIN_VERSION);
        if (le) {
            byteBuf.writeIntLE(getSize() - 6);
        } else {
            byteBuf.writeInt(getSize() - 6);
        }

        name.encode(byteBuf, le);
        publicAddrs.encode(byteBuf, le);
        priority.encode(byteBuf, le);
        weight.encode(byteBuf, le);
        crushLoc.encode(byteBuf, le);
    }

    @Override
    public void decode(ByteBuf byteBuf, boolean le) {
        byte version = byteBuf.readByte();
        byte minVersion = byteBuf.readByte();
        int size = le ? byteBuf.readIntLE() : byteBuf.readInt();

        name = new CephString();
        name.decode(byteBuf, le);
        publicAddrs = new AddrVec();
        publicAddrs.decode(byteBuf, le);
        priority = new Int16();
        priority.decode(byteBuf, le);
        weight = new Int16();
        weight.decode(byteBuf, le);
        crushLoc = new CephMap<>(CephString.class, CephString.class);
        crushLoc.decode(byteBuf, le);
    }
}
