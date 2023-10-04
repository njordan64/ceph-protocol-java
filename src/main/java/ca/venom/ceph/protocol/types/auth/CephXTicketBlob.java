package ca.venom.ceph.protocol.types.auth;

import ca.venom.ceph.protocol.types.*;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class CephXTicketBlob implements CephDataType {
    private CephRawByte version = new CephRawByte((byte) 1);
    private UInt64 secretId;
    private CephBytes blob;

    public CephXTicketBlob(UInt64 secretId, CephBytes blob) {
        this.secretId = secretId;
        this.blob = blob;
    }

    public static CephXTicketBlob read(ByteBuffer byteBuffer) {
        CephRawByte version = CephRawByte.read(byteBuffer);
        UInt64 secretId = UInt64.read(byteBuffer);
        CephBytes blob = CephBytes.read(byteBuffer);
        return new CephXTicketBlob(secretId, blob);
    }

    public UInt64 getSecretId() {
        return secretId;
    }

    public void setSecretId(UInt64 secretId) {
        this.secretId = secretId;
    }

    public CephBytes getBlob() {
        return blob;
    }

    public void setBlob(CephBytes blob) {
        this.blob = blob;
    }

    @Override
    public int getSize() {
        return 9 + blob.getSize();
    }

    @Override
    public void encode(ByteArrayOutputStream outputStream) {
        version.encode(outputStream);
        secretId.encode(outputStream);
        blob.encode(outputStream);
    }

    @Override
    public void encode(ByteBuffer byteBuffer) {
        version.encode(byteBuffer);
        secretId.encode(byteBuffer);
        blob.encode(byteBuffer);
    }
}
