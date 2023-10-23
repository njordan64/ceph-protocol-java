package ca.venom.ceph.protocol.types.auth;

import ca.venom.ceph.protocol.types.CephDataType;
import ca.venom.ceph.protocol.types.CephRawBytes;
import ca.venom.ceph.protocol.types.Int16;
import ca.venom.ceph.protocol.types.UTime;
import io.netty.buffer.ByteBuf;

public class CryptoKey implements CephDataType {
    private Int16 type;
    private UTime created;
    private Int16 secretLength;
    private CephRawBytes secret;

    public Int16 getType() {
        return type;
    }

    public void setType(Int16 type) {
        this.type = type;
    }

    public UTime getCreated() {
        return created;
    }

    public void setCreated(UTime created) {
        this.created = created;
    }

    public CephRawBytes getSecret() {
        return secret;
    }

    public void setSecret(CephRawBytes secret) {
        this.secret = secret;
        this.secretLength = new Int16((short) secret.getSize());
    }

    @Override
    public int getSize() {
        return type.getSize() + created.getSize() + 2 + secretLength.getValue();
    }

    @Override
    public void encode(ByteBuf byteBuf, boolean le) {
        type.encode(byteBuf, le);
        created.encode(byteBuf, le);
        secretLength.encode(byteBuf, le);
        secret.encode(byteBuf, le);
    }

    @Override
    public void decode(ByteBuf byteBuf, boolean le) {
        type = new Int16();
        type.decode(byteBuf, le);

        created = new UTime();
        created.decode(byteBuf, le);

        secretLength = new Int16();
        secretLength.decode(byteBuf, le);

        secret = new CephRawBytes(secretLength.getValue());
        secret.decode(byteBuf, le);
    }
}
