package ca.venom.ceph.protocol.types.auth;

import ca.venom.ceph.AuthMode;
import ca.venom.ceph.protocol.types.CephEnum;
import ca.venom.ceph.protocol.types.UInt64;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class AuthRequestPayload extends CephDataContainer {
    private CephEnum<AuthMode> authMode;
    private EntityName entityName;
    private UInt64 globalId;

    public AuthRequestPayload() {
        super();
    }

    public AuthRequestPayload(ByteBuffer byteBuffer) {
        super(byteBuffer);

        authMode = CephEnum.read(byteBuffer, AuthMode.class);
        entityName = EntityName.read(byteBuffer);
        globalId = UInt64.read(byteBuffer);
    }

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

    public UInt64 getGlobalId() {
        return globalId;
    }

    public void setGlobalId(UInt64 globalId) {
        this.globalId = globalId;
    }

    @Override
    protected int getPayloadSize() {
        return 8 + authMode.getSize() + entityName.getSize();
    }

    @Override
    protected void encodePayload(ByteArrayOutputStream stream) {
        authMode.encode(stream);
        entityName.encode(stream);
        globalId.encode(stream);
    }

    @Override
    protected void encodePayload(ByteBuffer byteBuffer) {
        authMode.encode(byteBuffer);
        entityName.encode(byteBuffer);
        globalId.encode(byteBuffer);
    }
}
