package ca.venom.ceph.protocol.types.auth;

import ca.venom.ceph.protocol.types.CephDataType;
import ca.venom.ceph.protocol.types.CephRawByte;
import ca.venom.ceph.protocol.types.CephRawBytes;
import ca.venom.ceph.protocol.types.UInt64;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class CephXServerChallenge implements CephDataType {
    private CephRawByte constant1 = new CephRawByte((byte) 1);
    private CephRawBytes serverChallenge;

    public CephXServerChallenge(CephRawBytes serverChallenge) {
        this.serverChallenge = serverChallenge;
    }

    public static CephXServerChallenge read(ByteBuffer byteBuffer) {
        CephRawByte version = CephRawByte.read(byteBuffer);
        return new CephXServerChallenge(CephRawBytes.read(byteBuffer, 8));
    }

    public CephRawBytes getServerChallenge() {
        return serverChallenge;
    }

    public void setServerChallenge(CephRawBytes serverChallenge) {
        this.serverChallenge = serverChallenge;
    }

    @Override
    public int getSize() {
        return 9;
    }

    @Override
    public void encode(ByteArrayOutputStream stream) {
        constant1.encode(stream);
        serverChallenge.encode(stream);
    }

    @Override
    public void encode(ByteBuffer byteBuffer) {
        constant1.encode(byteBuffer);
        serverChallenge.encode(byteBuffer);
    }
}
