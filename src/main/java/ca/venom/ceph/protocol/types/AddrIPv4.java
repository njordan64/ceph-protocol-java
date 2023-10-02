package ca.venom.ceph.protocol.types;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class AddrIPv4 extends Addr {
    public static final UInt16 TYPE = new UInt16(2);

    private byte[] port;
    private byte[] addrBytes;

    public AddrIPv4() {
        port = new byte[2];
        addrBytes = new byte[4];
    }

    public AddrIPv4(ByteBuffer byteBuffer) {
        super(byteBuffer);
    }

    public int getPort() {
        return ((port[0] & 255) << 8) |
                (port[1] & 255);
    }

    public void setPort(int portInt) {
        port = new byte[2];
        port[0] = (byte) ((portInt & 0xff00) >> 8);
        port[1] = (byte) (portInt & 0xff);
    }

    public byte[] getAddrBytes() {
        return addrBytes;
    }

    public void setAddrBytes(byte[] addrBytes) {
        this.addrBytes = addrBytes;
    }

    @Override
    protected UInt16 getType() {
        return TYPE;
    }

    @Override
    public int getSize() {
        return 14;
    }

    @Override
    protected void encodeDetails(ByteArrayOutputStream outputStream) {
        outputStream.writeBytes(port);
        outputStream.writeBytes(addrBytes);
        outputStream.writeBytes(new byte[8]);
    }

    @Override
    protected void encodeDetails(ByteBuffer byteBuffer) {
        byteBuffer.put(port);
        byteBuffer.put(addrBytes);
        byteBuffer.put(new byte[8]);
    }

    @Override
    protected void decodeDetails(ByteBuffer byteBuffer) {
        port = new byte[2];
        byteBuffer.get(port);
        addrBytes = new byte[4];
        byteBuffer.get(addrBytes);
        byteBuffer.position(byteBuffer.position() + 8);
    }
}
