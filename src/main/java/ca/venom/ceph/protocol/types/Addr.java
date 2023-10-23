package ca.venom.ceph.protocol.types;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class Addr implements CephDataType {
    public interface Details {
        void encode(ByteBuf byteBuf, boolean le);

        void decode(ByteBuf byteBuf, boolean le);
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

    private Details addrDetails;
    private CephRawBytes nonce;

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
        int type = 0;
        if (addrDetails instanceof Ipv4Details) {
            type = 2;
        } else if (addrDetails instanceof Ipv6Details) {
            type = 10;
        }

        if (le) {
            byteBuf.writeIntLE(14 + getSize());
            byteBuf.writeIntLE(type);
        } else {
            byteBuf.writeInt(14 + getSize());
            byteBuf.writeInt(type);
        }

        nonce.encode(byteBuf, le);

        if (le) {
            byteBuf.writeIntLE(getSize() + 2);
            byteBuf.writeShortLE(type);
        } else {
            byteBuf.writeInt(getSize() + 2);
            byteBuf.writeShort(type);
        }

        addrDetails.encode(byteBuf, le);
    }

    @Override
    public void decode(ByteBuf byteBuf, boolean le) {
        byteBuf.skipBytes(4);
        int type;
        if (le) {
            type = byteBuf.readIntLE();
        } else {
            type = byteBuf.readInt();
        }

        nonce = new CephRawBytes(4);
        nonce.decode(byteBuf, le);

        if (type == 2) {
            addrDetails = new Ipv4Details();
        } else if (type == 10) {
            addrDetails = new Ipv6Details();
        }

        int detailsLength;
        if (le) {
            detailsLength = byteBuf.readIntLE();
        } else {
            detailsLength = byteBuf.readInt();
        }

        int endIndex = byteBuf.readerIndex() + detailsLength;
        byteBuf.skipBytes(2);
        addrDetails.decode(byteBuf, le);
        byteBuf.readerIndex(endIndex);
    }

    @Override
    public int getSize() {
        if (addrDetails instanceof Ipv4Details) {
            return 14;
        } else if (addrDetails instanceof Ipv6Details) {
            return 28;
        }

        return 0;
    }
}
