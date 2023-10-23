package ca.venom.ceph.protocol.types.auth;

import ca.venom.ceph.AuthMode;
import ca.venom.ceph.protocol.types.CephEnum;
import ca.venom.ceph.protocol.types.Int64;
import io.netty.buffer.ByteBuf;

public class AuthRequestPayload extends CephDataContainer {
    private CephEnum<AuthMode> authMode;
    private EntityName entityName;
    private Int64 globalId;

    public AuthMode getAuthMode() {
        return authMode.getValue();
    }

    public void setAuthMode(AuthMode authMode) {
        this.authMode = new CephEnum<>(authMode);
    }

    public EntityName getEntityName() {
        return entityName;
    }

    public void setEntityName(EntityName entityName) {
        this.entityName = entityName;
    }

    public Int64 getGlobalId() {
        return globalId;
    }

    public void setGlobalId(Int64 globalId) {
        this.globalId = globalId;
    }

    @Override
    protected int getPayloadSize() {
        return 8 + authMode.getSize() + entityName.getSize();
    }

    @Override
    protected void encodePayload(ByteBuf byteBuf, boolean le) {
        authMode.encode(byteBuf, le);
        entityName.encode(byteBuf, le);
        globalId.encode(byteBuf, le);
    }

    @Override
    protected void decodePayload(ByteBuf byteBuf, boolean le) {
        authMode = new CephEnum<>(AuthMode.class);
        authMode.decode(byteBuf, le);

        entityName = new EntityName();
        entityName.decode(byteBuf, le);

        globalId = new Int64();
        globalId.decode(byteBuf, le);
    }
}
