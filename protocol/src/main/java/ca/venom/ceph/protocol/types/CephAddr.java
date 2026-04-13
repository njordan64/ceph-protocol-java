/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.protocol.types;

import ca.venom.ceph.encoding.annotations.CephField;
import ca.venom.ceph.encoding.annotations.CephFieldDecode;
import ca.venom.ceph.encoding.annotations.CephFieldEncode;
import ca.venom.ceph.encoding.annotations.CephType;
import ca.venom.ceph.protocol.CephDecoder;
import ca.venom.ceph.protocol.CephEncoder;
import ca.venom.ceph.protocol.CephFeatures;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.BitSet;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/1d146b4afffae5eb9031693f85cd9eabfc308679/src/msg/msg_types.h#L240
 */
@EqualsAndHashCode
@CephType
public class CephAddr implements Comparable<CephAddr> {
    public enum AddrType {
        NONE(0),
        LEGACY(1),
        MSGR2(2),
        ANY(3),
        CIDR(4);

        @Getter
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
    }

    @Getter
    @Setter
    private AddrType type;

    @Getter
    @Setter
    @CephField(order = 4)
    private int nonce;

    private byte[] data;

    private boolean isLegacy;

    @CephFieldEncode
    public void encodeHeader(ByteBuf byteBuf, boolean le, BitSet features) {
        if (!CephFeatures.MSG_ADDR2.isEnabled(features)) {
            byteBuf.writeZero(4);
        } else {
            byteBuf.writeByte(1);
            byteBuf.writeByte(1);
            byteBuf.writeByte(1);
        }
    }

    @CephFieldDecode
    public void decodeHeader(ByteBuf byteBuf, boolean le, BitSet features) {
        final byte initial = byteBuf.readByte();
        if (initial == 0) {
            isLegacy = true;
            byteBuf.readerIndex(byteBuf.readerIndex() + 3);
        } else {
            final byte version = byteBuf.readByte();
            final byte compat = byteBuf.readByte();
        }
    }

    @CephFieldEncode(order = 2)
    public void encodeSize(ByteBuf byteBuf, boolean le, BitSet features) {
        if (CephFeatures.MSG_ADDR2.isEnabled(features)) {
            final short family = getFamily();
            switch (family) {
                case 2:
                case 10:
                    CephEncoder.encode(12 + data.length, byteBuf, le);
                    break;
                default:
                    byteBuf.writeZero(12);
                    break;
            }
        }
    }

    @CephFieldDecode(order = 2)
    public void decodeSize(ByteBuf byteBuf, boolean le, BitSet features) {
        if (!isLegacy) {
            byteBuf.readerIndex(byteBuf.readerIndex() + 4);
        }
    }

    @CephFieldEncode(order = 3)
    public void encodeType(ByteBuf byteBuf, boolean le, BitSet features) {
        if (!CephFeatures.MSG_ADDR2.isEnabled(features)) {
            return;
        }

        if (CephFeatures.SERVER_NAUTILUS.isEnabled(features)) {
            CephEncoder.encode(type.getValue(), byteBuf, le);
        } else {
            if (type == AddrType.ANY) {
                CephEncoder.encode(AddrType.LEGACY.getValue(), byteBuf, le);
            } else {
                CephEncoder.encode(type.getValue(), byteBuf, le);
            }
        }
    }

    @CephFieldDecode(order = 3)
    public void decodeType(ByteBuf byteBuf, boolean le, BitSet features) {
        if (isLegacy) {
            return;
        }

        type = AddrType.getFromValueInt(CephDecoder.decodeInt(byteBuf, le));
    }

    @CephFieldEncode(order = 5)
    public void encodeData(ByteBuf byteBuf, boolean le, BitSet features) {
        if (!CephFeatures.MSG_ADDR2.isEnabled(features)) {
            byteBuf.writeBytes(data);
            byteBuf.writeZero(128 - data.length);
        } else {
            final short family = getFamily();
            if (family != 2  && family != 10) {
                byteBuf.writeZero(4);
            } else {
                CephEncoder.encode(data.length, byteBuf, le);
                byteBuf.writeBytes(data);
            }
        }
    }

    @CephFieldDecode(order = 5)
    public void decodeData(ByteBuf byteBuf, boolean le, BitSet features) {
        if (isLegacy) {
            final short family = byteBuf.getShortLE(byteBuf.readerIndex());
            final byte[] bytesRead = new byte[128];
            byteBuf.readBytes(bytesRead);

            switch (family) {
                case 2:
                    data = new byte[16];
                    System.arraycopy(bytesRead, 0, data, 0, 16);
                    break;
                case 10:
                    data = new byte[28];
                    System.arraycopy(bytesRead, 0, data, 0, 28);
                    break;
                default:
                    data = new byte[2];
                    break;
            }
        } else {
            final int length = CephDecoder.decodeInt(byteBuf, le);
            data = new byte[length];
            byteBuf.readBytes(data);
        }
    }

    private short getFamily() {
        final ByteBuf byteBuf = Unpooled.wrappedBuffer(data);
        return byteBuf.getShortLE(0);
    }

    public int getPort() {
        final short family = getFamily();
        if (family == 2 || family == 10) {
            final byte[] intBytes = {0, 0, data[2], data[3]};
            final ByteBuf byteBuf = Unpooled.wrappedBuffer(intBytes);
            return byteBuf.getInt(0);
        }

        return 0;
    }

    public InetSocketAddress getSocketAddress() {
        final short family = getFamily();

        if (family == 2) {
            final ByteBuf byteBuf = Unpooled.wrappedBuffer(data);
            final int port = getPort();
            final byte[] addrBytes = new byte[4];
            byteBuf.getBytes(4, addrBytes);
            try {
                return new InetSocketAddress(
                        InetAddress.getByAddress(addrBytes),
                        port
                );
            } catch (UnknownHostException uhe) {
                return null;
            }
        } else if (family == 10) {
            final ByteBuf byteBuf = Unpooled.wrappedBuffer(data);
            final int port = getPort();
            final byte[] addrBytes = new byte[16];
            byteBuf.getBytes(8, addrBytes);
            final int scopeId = byteBuf.getIntLE(24);
            try {
                return new InetSocketAddress(
                        Inet6Address.getByAddress(null, addrBytes, scopeId),
                        port
                );
            } catch (UnknownHostException uhe) {
                return null;
            }
        }

        return null;
    }

    public int getIPv6FlowInfo() {
        final short family = getFamily();
        if (family != 10) {
            return 0;
        }

        return ByteBuffer.wrap(data, 4, data.length - 4).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().get();
    }

    public void setSocketAddress(InetSocketAddress addr) {
        if (addr == null) {
            type = AddrType.NONE;
            data = new byte[2];
        }

        type = AddrType.MSGR2;
        if (addr.getAddress() instanceof Inet4Address inet4Address) {
            data = new byte[16];
            final ByteBuf byteBuf = Unpooled.wrappedBuffer(data);
            byteBuf.writerIndex(0);
            byteBuf.writeShortLE(2);
            byteBuf.writeShort(addr.getPort());
            byteBuf.writeBytes(inet4Address.getAddress());
        } else if (addr.getAddress() instanceof Inet6Address inet6Address) {
            data = new byte[28];
            final ByteBuf byteBuf = Unpooled.wrappedBuffer(data);
            byteBuf.writerIndex(0);
            byteBuf.writeShortLE(10);
            byteBuf.writeShort(addr.getPort());
            byteBuf.writeZero(4);
            byteBuf.writeBytes(inet6Address.getAddress());
            byteBuf.writeIntLE(inet6Address.getScopeId());
        } else {
            data = new byte[2];
        }
    }

    public void setIPv6FlowInfo(int flowInfo) {
        final short family = getFamily();
        if (family == 10) {
            final ByteBuf byteBuf = Unpooled.wrappedBuffer(data);
            byteBuf.setIntLE(4, flowInfo);
        }
    }

    @Override
    public int compareTo(CephAddr cephAddr) {
        return Arrays.compare(data, cephAddr.data);
    }

    @Override
    public String toString() {
        return getSocketAddress().toString();
    }

    public CephAddr duplicate() {
        final CephAddr toReturn = new CephAddr();
        toReturn.type = type;
        toReturn.nonce = nonce;
        toReturn.data = data;
        toReturn.isLegacy = isLegacy;

        return toReturn;
    }
}
