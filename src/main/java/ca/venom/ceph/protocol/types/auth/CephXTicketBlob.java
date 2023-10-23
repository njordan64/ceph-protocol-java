package ca.venom.ceph.protocol.types.auth;

import ca.venom.ceph.protocol.types.*;
import io.netty.buffer.ByteBuf;

public class CephXTicketBlob implements CephDataType {
    private CephRawByte version = new CephRawByte((byte) 1);
    private Int64 secretId;
    private CephBytes blob;

    public Int64 getSecretId() {
        return secretId;
    }

    public void setSecretId(Int64 secretId) {
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
    public void encode(ByteBuf byteBuf, boolean le) {
        version.encode(byteBuf, le);
        secretId.encode(byteBuf, le);
        blob.encode(byteBuf, le);
    }

    @Override
    public void decode(ByteBuf byteBuf, boolean le) {
        byte versionValue = byteBuf.readByte();
        if (versionValue != version.getValue()) {
            throw new IllegalArgumentException("Unsupported version (" + versionValue + ") only 1 is supported");
        }

        secretId = new Int64();
        secretId.decode(byteBuf, le);

        blob = new CephBytes();
        blob.decode(byteBuf, le);
    }
}
