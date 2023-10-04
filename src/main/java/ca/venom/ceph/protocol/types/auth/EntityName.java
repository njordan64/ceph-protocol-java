package ca.venom.ceph.protocol.types.auth;

import ca.venom.ceph.protocol.types.CephDataType;
import ca.venom.ceph.protocol.types.CephString;
import ca.venom.ceph.protocol.types.UInt32;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class EntityName implements CephDataType {
    private UInt32 type;
    private CephString entityName;

    public EntityName(UInt32 type, CephString entityName) {
        this.type = type;
        this.entityName = entityName;
    }

    public static EntityName read(ByteBuffer byteBuffer) {
        UInt32 type = UInt32.read(byteBuffer);
        CephString entityName = CephString.read(byteBuffer);
        return new EntityName(type, entityName);
    }

    public UInt32 getType() {
        return type;
    }

    public void setType(UInt32 type) {
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
    public void encode(ByteArrayOutputStream stream) {
        type.encode(stream);
        entityName.encode(stream);
    }

    @Override
    public void encode(ByteBuffer byteBuffer) {
        type.encode(byteBuffer);
        entityName.encode(byteBuffer);
    }
}
