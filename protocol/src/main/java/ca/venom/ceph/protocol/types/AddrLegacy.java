package ca.venom.ceph.protocol.types;

import ca.venom.ceph.encoding.annotations.CephEncodingSize;
import ca.venom.ceph.encoding.annotations.CephField;
import ca.venom.ceph.encoding.annotations.CephType;
import ca.venom.ceph.encoding.annotations.CephTypeVersionConstant;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Setter;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@CephType
@CephTypeVersionConstant(version = 0)
public class AddrLegacy extends AddrBase {
    @Getter
    @Setter
    @CephField
    private int nonce;

    @Getter
    @Setter
    @CephField(order = 2)
    @CephEncodingSize(128)
    private byte[] socketStorage;

    @Override
    public short getPort() {
        return ByteBuffer.wrap(socketStorage, 2, socketStorage.length - 2).order(ByteOrder.BIG_ENDIAN).asShortBuffer().get();
    }

    @Override
    public InetAddress getAddress() {
        short family = ByteBuffer.wrap(socketStorage).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get();
        if (family == 2) {
            byte[] bytes = new byte[4];
            System.arraycopy(socketStorage, 4, bytes, 0, 4);
            try {
                return InetAddress.getByAddress(bytes);
            } catch (UnknownHostException uhe) {
                return null;
            }
        } else if (family == 10) {
            byte[] bytes = new byte[16];
            int scopeId = ByteBuffer.wrap(socketStorage, 24, socketStorage.length - 24).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get();
            try {
                return Inet6Address.getByAddress(null, bytes, scopeId);
            } catch (UnknownHostException uhe) {
                return null;
            }
        }

        return null;
    }

    @Override
    public int getIPv6FlowInfo() {
        short family = ByteBuffer.wrap(socketStorage).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get();
        if (family != 10) {
            return 0;
        }

        return ByteBuffer.wrap(socketStorage, 4, socketStorage.length - 4).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get();
    }

    @Override
    public void setIPv4AddrWithPort(Inet4Address addr, short port) {
        socketStorage = new byte[128];
        ByteBuf byteBuf = Unpooled.wrappedBuffer(socketStorage);
        byteBuf.writerIndex(0);
        byteBuf.writeShortLE(2);
        byteBuf.writeShort(port);
        byteBuf.writeBytes(addr.getAddress());
    }

    @Override
    public void setIPv6AddrWithPort(Inet6Address addr, short port, int flowInfo) {
        socketStorage = new byte[28];
        ByteBuf byteBuf = Unpooled.wrappedBuffer(socketStorage);
        byteBuf.writerIndex(0);
        byteBuf.writeShortLE(10);
        byteBuf.writeShort(port);
        byteBuf.writeIntLE(flowInfo);
        byteBuf.writeBytes(addr.getAddress());
        byteBuf.writeIntLE(addr.getScopeId());
    }
}
