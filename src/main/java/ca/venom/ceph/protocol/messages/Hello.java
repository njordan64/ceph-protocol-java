package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.NodeType;
import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.UInt32;
import ca.venom.ceph.protocol.types.UInt8;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class Hello extends ControlFrame {
    public static final int ADDR4 = 2;
    public static final int ADDR6 = 10;

    public interface Addr {
        int getSize();

        void decode(ByteBuffer byteBuffer);

        void encode(ByteArrayOutputStream outputStream);
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

        public void encode(ByteArrayOutputStream outputStream) {
            outputStream.write((byte) 2);
            outputStream.write((byte) 0);
            outputStream.write(port[0]);
            outputStream.write(port[1]);
            outputStream.writeBytes(addrBytes);
            outputStream.writeBytes(new byte[8]);
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
            flowInfo = UInt32.read(byteBuffer);
            addrBytes = new byte[16];
            byteBuffer.get(addrBytes);
            scopeId = UInt32.read(byteBuffer);
        }

        public void encode(ByteArrayOutputStream outputStream) {
            outputStream.write((byte) 10);
            outputStream.write((byte) 0);
            outputStream.write(port[0]);
            outputStream.write(port[1]);
            flowInfo.encode(outputStream);
            outputStream.writeBytes(addrBytes);
            scopeId.encode(outputStream);
        }
    }

    private NodeType nodeType;
    private boolean msgAddr2;
    private UInt32 type;
    private UInt32 nonce;
    private Addr addr;

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
    public MessageType getTag() {
        return MessageType.HELLO;
    }

    @Override
    protected int encodeSegmentBody(int segmentIndex, ByteArrayOutputStream outputStream) {
        if (segmentIndex == 0) {
            write(new UInt8(nodeType.getTypeNum()), outputStream);
            write(msgAddr2 ? (byte) 1 : (byte) 0, outputStream);
            write((byte) 1, outputStream);
            write((byte) 1, outputStream);

            write(new UInt32(12 + addr.getSize()), outputStream);
            write(type, outputStream);
            write(nonce, outputStream);
            write(new UInt32(addr.getSize()), outputStream);
            addr.encode(outputStream);

            return 8;
        } else {
            return 0;
        }
    }

    @Override
    protected void decodeSegmentBody(int segmentIndex, ByteBuffer byteBuffer, int alignment) {
        if (segmentIndex == 0) {
            nodeType = NodeType.getFromTypeNum(byteBuffer.get());
            msgAddr2 = byteBuffer.get() > 0;

            byteBuffer.position(byteBuffer.position() + 6);

            type = readUInt32(byteBuffer);
            nonce = readUInt32(byteBuffer);
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
}
