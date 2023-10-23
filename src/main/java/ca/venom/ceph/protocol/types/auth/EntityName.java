package ca.venom.ceph.protocol.types.auth;

import ca.venom.ceph.protocol.types.CephDataType;
import ca.venom.ceph.protocol.types.CephString;
import ca.venom.ceph.protocol.types.Int32;
import io.netty.buffer.ByteBuf;

public class EntityName implements CephDataType {
    private Int32 type;
    private CephString entityName;

    public Int32 getType() {
        return type;
    }

    public void setType(Int32 type) {
        this.type = type;
    }

    public CephString getEntityName() {
        return entityName;
    }

    public void setEntityName(CephString entityName) {
        this.entityName = entityName;
    }

    @Override
    public int getSize() {
        return type.getSize() + entityName.getSize();
    }

    @Override
    public void encode(ByteBuf byteBuf, boolean le) {
        type.encode(byteBuf, le);
        entityName.encode(byteBuf, le);
    }

    @Override
    public void decode(ByteBuf byteBuf, boolean le) {
        type = new Int32();
        type.decode(byteBuf, le);

        entityName = new CephString();
        entityName.decode(byteBuf, le);
    }
}
