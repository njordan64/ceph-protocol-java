package ca.venom.ceph.protocol.types;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class AddrIPv6 extends Addr {
    public static final UInt16 TYPE = new UInt16(10);

    private byte[] port;
    private UInt32 flowInfo;
    private byte[] addrBytes;
    private UInt32 scopeId;

    public AddrIPv6() {
        port = new byte[2];
        flowInfo = new UInt32(0);
        addrBytes = new byte[6];
        scopeId = new UInt32(0);
    }

    public AddrIPv6(ByteBuffer byteBuffer) {
        super(byteBuffer);
    }

    public int getPort() {
        return ((port[0] & 255) << 8) |
                (port[0] & 255);
    }

    public void setPort(int portInt) {
        port = new byte[2];
        port[0] = (byte) ((portInt & 0xff00) >> 8);
        port[1] = (byte) (portInt & 0xff);
    }

    public UInt32 getFlowInfo() {
        return flowInfo;
    }

    public void setFlowInfo(UInt32 flowInfo) {
        this.flowInfo = flowInfo;
    }

    public byte[] getAddrBytes() {
        return addrBytes;
    }

    public void setAddrBytes(byte[] addrBytes) {
        if (addrBytes == null || addrBytes.length != 6) {
            throw new IllegalArgumentException("Invalid IPv6 address");
        }

        this.addrBytes = addrBytes;
    }

    public UInt32 getScopeId() {
        return scopeId;
    }

    public void setScopeId(UInt32 scopeId) {
        this.scopeId = scopeId;
    }

    @Override
    protected UInt16 getType() {
        return TYPE;
    }

    @Override
    protected int getSize() {
        return 28;
    }

    @Override
    protected void encodeDetails(ByteArrayOutputStream outputStream) {
        outputStream.writeBytes(port);
        flowInfo.encode(outputStream);
        outputStream.writeBytes(addrBytes);
        scopeId.encode(outputStream);
    }

    @Override
    protected void encodeDetails(ByteBuffer byteBuffer) {
        byteBuffer.put(port);
        flowInfo.encode(byteBuffer);
        byteBuffer.put(addrBytes);
        scopeId.encode(byteBuffer);
    }

    @Override
    protected void decodeDetails(ByteBuffer byteBuffer) {
        port = new byte[2];
        byteBuffer.get(port);
        flowInfo = UInt32.read(byteBuffer);
        addrBytes = new byte[6];
        byteBuffer.get(addrBytes);
        scopeId = UInt32.read(byteBuffer);
    }
}
