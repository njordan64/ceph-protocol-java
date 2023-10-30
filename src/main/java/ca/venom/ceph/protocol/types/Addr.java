package ca.venom.ceph.protocol.types;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class Addr implements CephDataType {
    public interface Details {
        void encode(ByteBuf byteBuf, boolean le);

        void decode(ByteBuf byteBuf, boolean le);

        int getSize();

        int getType();
    }

    public static class Ipv4Details implements Details {
        private int port;
        private byte[] addrBytes;

        public Ipv4Details() {
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public byte[] getAddrBytes() {
            return addrBytes;
        }

        public void setAddrBytes(byte[] addrBytes) {
            this.addrBytes = addrBytes;
        }

        @Override
        public int getSize() {
            return 14;
        }

        @Override
        public int getType() {
            return 2;
        }

        @Override
        public void encode(ByteBuf byteBuf, boolean le) {
            byteBuf.writeShort(port);
            byteBuf.writeBytes(addrBytes);
            byteBuf.writeBytes(new byte[8]);
        }

        @Override
        public void decode(ByteBuf byteBuf, boolean le) {
            port = 0xffff & byteBuf.readShort();
            addrBytes = new byte[4];
            byteBuf.readBytes(addrBytes);
            byteBuf.skipBytes(8);
        }
    }

    public static class Ipv6Details implements Details {
        private int port;
        private Int32 flowInfo;
        private byte[] addrBytes;
        private Int32 scopeId;

        public Ipv6Details() {
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public Int32 getFlowInfo() {
            return flowInfo;
        }

        public void setFlowInfo(Int32 flowInfo) {
            this.flowInfo = flowInfo;
        }

        public byte[] getAddrBytes() {
            return addrBytes;
        }

        public void setAddrBytes(byte[] addrBytes) {
            this.addrBytes = addrBytes;
        }

        public Int32 getScopeId() {
            return scopeId;
        }

        public void setScopeId(Int32 scopeId) {
            this.scopeId = scopeId;
        }

        @Override
        public int getSize() {
            return 16;
        }

        @Override
        public int getType() {
            return 10;
        }

        @Override
        public void encode(ByteBuf byteBuf, boolean le) {
            byteBuf.writeShort(port);
            flowInfo.encode(byteBuf, false);
            byteBuf.writeBytes(addrBytes);
            scopeId.encode(byteBuf, false);
        }

        @Override
        public void decode(ByteBuf byteBuf, boolean le) {
            port = 0xffff & byteBuf.readShort();
            flowInfo = new Int32();
            flowInfo.decode(byteBuf, false);
            addrBytes = new byte[6];
            byteBuf.readBytes(addrBytes);
            scopeId = new Int32();
            scopeId.decode(byteBuf, false);
        }
    }

    private CephRawByte marker = new CephRawByte((byte) 1);
    private CephRawByte version = new CephRawByte((byte) 1);
    private CephRawByte minVersion = new CephRawByte((byte) 1);
    private Int32 type;
    private Details addrDetails;
    private CephRawBytes nonce;

    public int getType() {
        return type.getValue();
    }

    public void setType(int type) {
        this.type = new Int32(type);
    }

    public byte[] getNonce() {
        return nonce.getValue();
    }

    public void setNonce(byte[] nonce) {
        this.nonce = new CephRawBytes(nonce);
    }

    public Details getAddrDetails() {
        return addrDetails;
    }

    public void setAddrDetails(Details addrDetails) {
        this.addrDetails = addrDetails;
    }

    @Override
    public void encode(ByteBuf byteBuf, boolean le) {
        marker.encode(byteBuf, le);
        version.encode(byteBuf, le);
        minVersion.encode(byteBuf, le);

        if (le) {
            byteBuf.writeIntLE(14 + addrDetails.getSize());
        } else {
            byteBuf.writeInt(14 + addrDetails.getSize());
        }

        type.encode(byteBuf, le);
        nonce.encode(byteBuf, le);

        if (le) {
            byteBuf.writeIntLE(2 + addrDetails.getSize());
            byteBuf.writeShortLE(addrDetails.getType());
        } else {
            byteBuf.writeInt(2 + addrDetails.getSize());
            byteBuf.writeShort(addrDetails.getType());
        }

        addrDetails.encode(byteBuf, le);
    }

    @Override
    public void decode(ByteBuf byteBuf, boolean le) {
        byte marker = byteBuf.readByte();
        byte version = byteBuf.readByte();
        byte minVersion = byteBuf.readByte();
        int length = le ? byteBuf.readIntLE() : byteBuf.readInt();
        type = new Int32();
        type.decode(byteBuf, le);

        nonce = new CephRawBytes(4);
        nonce.decode(byteBuf, le);
        int innerLength = le ? byteBuf.readIntLE() : byteBuf.readInt();

        int encodedType = le ? byteBuf.readShortLE() : byteBuf.readShort();
        if (encodedType == 2) {
            addrDetails = new Ipv4Details();
        } else if (encodedType == 10) {
            addrDetails = new Ipv6Details();
        }
        addrDetails.decode(byteBuf, le);
    }

    @Override
    public int getSize() {
        return 21 + addrDetails.getSize();
    }
}
