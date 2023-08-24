package ca.venom.ceph.protocol;

import ca.venom.ceph.protocol.types.UInt8;
import ca.venom.ceph.CephCRC32C;
import ca.venom.ceph.NodeType;
import ca.venom.ceph.protocol.types.UInt16;
import ca.venom.ceph.protocol.types.UInt32;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class HelloMessage extends MessageBase {
    public static final int ADDR4 = 2;
    public static final int ADDR6 = 10;

    public static interface Addr {
        public int getSize();

        void decode(ByteBuffer byteBuffer);

        void encode(ByteBuffer byteBuffer);
    }

    public static class AddrIPv4 implements Addr {
        private byte[] port;
        private byte[] addrBytes;

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
            if (addrBytes == null || addrBytes.length != 4) {
                throw new IllegalArgumentException("Invalid IPv4 address");
            }

            this.addrBytes = addrBytes;
        }

        public int getSize() {
            return 16;
        }

        public void decode(ByteBuffer byteBuffer) {
            byteBuffer.position(byteBuffer.position() + 2);
            port = new byte[2];
            port[0] = byteBuffer.get();
            port[1] = byteBuffer.get();
            addrBytes = new byte[] {
                byteBuffer.get(),
                byteBuffer.get(),
                byteBuffer.get(),
                byteBuffer.get()
            };
        }

        public void encode(ByteBuffer byteBuffer) {
            byteBuffer.put((byte) 2);
            byteBuffer.put((byte) 0);
            byteBuffer.put(port[0]);
            byteBuffer.put(port[1]);
            byteBuffer.put(addrBytes);
            byteBuffer.put(new byte[8]);
        }
    }

    public static class AddrIPV6 implements Addr {
        private byte[] port;
        private UInt32 flowInfo;
        private byte[] addrBytes;
        private UInt32 scopeId;

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

        public int getSize() {
            return 28;
        }

        public void decode(ByteBuffer byteBuffer) {
            byteBuffer.position(byteBuffer.position() + 2);
            port = new byte[2];
            port[0] = byteBuffer.get();
            port[1] = byteBuffer.get();
            flowInfo = new UInt32(byteBuffer);
            addrBytes = new byte[16];
            byteBuffer.get(addrBytes);
            scopeId = new UInt32(byteBuffer);
        }

        public void encode(ByteBuffer byteBuffer) {
            byteBuffer.put((byte) 10);
            byteBuffer.put((byte) 0);
            byteBuffer.put(port[0]);
            byteBuffer.put(port[1]);
            flowInfo.encode(byteBuffer);
            byteBuffer.put(addrBytes);
            scopeId.encode(byteBuffer);
        }
    }

    private NodeType nodeType;
    private boolean msgAddr2;
    private UInt32 type;
    private UInt32 nonce;
    private Addr addr;

    @Override
    protected UInt8 getTag() {
        return MessageType.HELLO.getTagNum();
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public boolean isMsgAddr2() {
        return msgAddr2;
    }

    public void setMsgAddr2(boolean msgAddr2) {
        this.msgAddr2 = msgAddr2;
    }

    public UInt32 getType() {
        return type;
    }

    public void setType(UInt32 type) {
        this.type = type;
    }

    public UInt32 getNonce() {
        return nonce;
    }

    public void setNonce(UInt32 nonce) {
        this.nonce = nonce;
    }

    public Addr getAddr() {
        return addr;
    }

    public void setAddr(Addr addr) {
        this.addr = addr;
    }

    @Override
    protected SectionMetadata[] getSectionMetadatas() {
        return new SectionMetadata[] {
            new SectionMetadata(20 + addr.getSize(), 8),
            new SectionMetadata(0, 0),
            new SectionMetadata(0, 0),
            new SectionMetadata(0, 0)
        };
    }

    @Override
    protected void encodeSection(int section, ByteBuffer byteBuffer) throws IOException {
        if (section > 0) {
            return;
        }

        UInt8.fromValue(nodeType.getTypeNum()).encode(byteBuffer);
        byteBuffer.put(msgAddr2 ? (byte) 1 : (byte) 0);
        
        // Constants???
        byteBuffer.put((byte) 1);
        byteBuffer.put((byte) 1);

        UInt32.fromValue(12 + addr.getSize()).encode(byteBuffer);
        type.encode(byteBuffer);
        nonce.encode(byteBuffer);
        UInt32.fromValue(addr.getSize()).encode(byteBuffer);
        addr.encode(byteBuffer);
    }

    @Override
    protected void decodeSection(int section, ByteBuffer byteBuffer) throws IOException {
        nodeType = NodeType.getFromTypeNum(byteBuffer.get() & 255);
        msgAddr2 = new UInt8(byteBuffer).getValue() > 0;

        // Skip constants
        byteBuffer.get();
        byteBuffer.get();

        // Skip first size field
        new UInt32(byteBuffer);

        type = new UInt32(byteBuffer);
        nonce = new UInt32(byteBuffer);
        byteBuffer.position(byteBuffer.position() + 4);

        if (type.getValue() == 2) {
            addr = new AddrIPv4();
            addr.decode(byteBuffer);
        } else if (type.getValue() == 10) {
            addr = new AddrIPV6();
            addr.decode(byteBuffer);
        }
    }
}