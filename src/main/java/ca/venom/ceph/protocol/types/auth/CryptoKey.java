package ca.venom.ceph.protocol.types.auth;

import ca.venom.ceph.protocol.types.CephDataType;
import ca.venom.ceph.protocol.types.CephRawBytes;
import ca.venom.ceph.protocol.types.UInt16;
import ca.venom.ceph.protocol.types.UTime;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class CryptoKey implements CephDataType {
    private UInt16 type;
    private UTime created;
    private UInt16 secretLength;
    private CephRawBytes secret;

    public CryptoKey(UInt16 type, UTime created, CephRawBytes secret) {
        this.type = type;
        this.created = created;
        this.secretLength = new UInt16(secret.getSize());
        this.secret = secret;
    }

    public static CryptoKey read(ByteBuffer byteBuffer) {
        UInt16 type = UInt16.read(byteBuffer);
        UTime created = UTime.read(byteBuffer);

        int secretLength = UInt16.read(byteBuffer).getValue();
        byte[] secretBytes = new byte[secretLength];
        byteBuffer.get(secretBytes);

        return new CryptoKey(type, created, new CephRawBytes(secretBytes));
    }

    public UInt16 getType() {
        return type;
    }

    public void setType(UInt16 type) {
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
        this.secretLength = new UInt16(secret.getSize());
    }

    @Override
    public int getSize() {
        return type.getSize() + created.getSize() + 2 + secretLength.getValue();
    }

    @Override
    public void encode(ByteArrayOutputStream outputStream) {
        type.encode(outputStream);
        created.encode(outputStream);
        secretLength.encode(outputStream);
        secret.encode(outputStream);
    }

    @Override
    public void encode(ByteBuffer byteBuffer) {
        type.encode(byteBuffer);
        created.encode(byteBuffer);
        secretLength.encode(byteBuffer);
        secret.encode(byteBuffer);
    }
}
