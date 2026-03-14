package ca.venom.ceph.protocol.types;

import ca.venom.ceph.encoding.annotations.*;
import ca.venom.ceph.protocol.CephFeatures;
import ca.venom.ceph.types.EnumWithIntValue;
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
import java.util.BitSet;

@CephType
@CephMarker(1)
@CephTypeVersionConstant(version = 1, compatVersion = 1)
public class Addr extends AddrBase {
    public enum AddrType implements EnumWithIntValue {
        NONE(0),
        LEGACY(1),
        MSGR2(2),
        ANY(3),
        CIDR(4);

        private int value;

        AddrType(int value) {
            this.value = value;
        }

        public static AddrType getFromValueInt(int value) {
            for (AddrType addrType : values()) {
                if (addrType.value == value) {
                    return addrType;
                }
            }

            return null;
        }

        @Override
        public int getValueInt() {
            return value;
        }
    }

    @Setter
    @CephField
    @CephEncodingSize(4)
    private AddrType type = AddrType.MSGR2;

    @Getter
    @Setter
    @CephField(order = 2)
    private int nonce;

    @Getter
    @Setter
    @CephField(order = 3, includeSize = true)
    private byte[] data;

    @CephFieldEncode
    public void encodeType(ByteBuf byteBuf, boolean le, BitSet features) {
        AddrType t = (!CephFeatures.SERVER_NAUTILUS.isEnabled(features) && type == AddrType.ANY) ? AddrType.LEGACY : type;
        if (le) {
            byteBuf.writeIntLE(t.getValueInt());
        } else {
            byteBuf.writeInt(t.getValueInt());
        }
    }

    @Override
    public short getPort() {
        return ByteBuffer.wrap(data, 2, data.length - 2).order(ByteOrder.BIG_ENDIAN).asShortBuffer().get();
    }

    @Override
    public InetAddress getAddress() {
        short family = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get();

        if (family == 2) {
            byte[] addrBytes = new byte[4];
            System.arraycopy(data, 4, addrBytes, 0, 4);
            try {
                return InetAddress.getByAddress(addrBytes);
            } catch (UnknownHostException uhe) {
                return null;
            }
        } else if (family == 10) {
            byte[] addrBytes = new byte[16];
            System.arraycopy(data, 8, addrBytes, 0, 16);
            int scopeId = ByteBuffer.wrap(data, 24, data.length - 24).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get();
            try {
                return Inet6Address.getByAddress(null, addrBytes, scopeId);
            } catch (UnknownHostException uhe) {
                return null;
            }
        }

        return null;
    }

    @Override
    public int getIPv6FlowInfo() {
        short family = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get();
        if (family != 10) {
            return 0;
        }

        return ByteBuffer.wrap(data, 4, data.length - 4).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get();
    }

    @Override
    public void setIPv4AddrWithPort(Inet4Address addr, short port) {
        data = new byte[16];
        ByteBuf byteBuf = Unpooled.wrappedBuffer(data);
        byteBuf.writerIndex(0);
        byteBuf.writeShortLE(2);
        byteBuf.writeShort(port);
        byteBuf.writeBytes(addr.getAddress());
    }

    @Override
    public void setIPv6AddrWithPort(Inet6Address addr, short port, int flowInfo) {
        data = new byte[28];
        ByteBuf byteBuf = Unpooled.wrappedBuffer(data);
        byteBuf.writerIndex(0);
        byteBuf.writeShortLE(10);
        byteBuf.writeShort(port);
        byteBuf.writeIntLE(flowInfo);
        byteBuf.writeBytes(addr.getAddress());
        byteBuf.writeIntLE(addr.getScopeId());
    }
}
