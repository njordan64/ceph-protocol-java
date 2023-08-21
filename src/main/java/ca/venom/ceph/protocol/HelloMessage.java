package ca.venom.ceph.protocol;

import ca.venom.ceph.protocol.types.UInt8;
import ca.venom.ceph.CephCRC32C;
import ca.venom.ceph.protocol.types.UInt16;
import ca.venom.ceph.protocol.types.UInt32;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class HelloMessage {
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

    public static class Segment {
        private UInt8 nodeType;
        private boolean msgAddr2;
        private UInt32 type;
        private UInt32 nonce;
        private Addr addr;

        public UInt8 getNodeType() {
            return nodeType;
        }

        public void setNodeType(UInt8 nodeType) {
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

        public int getSize() {
            return 20 + addr.getSize();
        }

        public void decode(ByteBuffer byteBuffer) throws IOException {
            nodeType = new UInt8(byteBuffer);
            msgAddr2 = new UInt8(byteBuffer).getValue() > 0;
            byteBuffer.position(byteBuffer.position() + 6);
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

        public void encode(ByteBuffer byteBuffer) throws IOException {
            nodeType.encode(byteBuffer);
            byteBuffer.put(msgAddr2 ? (byte) 1 : (byte) 0);
            byteBuffer.put((byte) 1);
            byteBuffer.put((byte) 1);
            UInt32.fromValue(12 + addr.getSize()).encode(byteBuffer);
            type.encode(byteBuffer);
            nonce.encode(byteBuffer);
            UInt32.fromValue(addr.getSize()).encode(byteBuffer);
            addr.encode(byteBuffer);
        }
    }

    private UInt8 tag;
    private UInt8 flags;
    private List<Segment> segments;

    public UInt8 getTag() {
        return tag;
    }

    public void setTag(UInt8 tag) {
        this.tag = tag;
    }

    public UInt8 getFlags() {
        return flags;
    }

    public void setFlags(UInt8 flags) {
        this.flags = flags;
    }

    public List<Segment> getSegments() {
        return segments;
    }

    public void setSegments(List<Segment> segments) {
        if (segments == null || segments.size() > 4) {
            throw new IllegalArgumentException("Invalid segments list");
        }

        this.segments = segments;
    }

    public void decode(InputStream stream) throws IOException {
        byte[] bytes = new byte[32];
        stream.read(bytes);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);

        tag = new UInt8(byteBuffer);
        int numSegments = new UInt8(byteBuffer).getValue();
        
        int[] segmentsLengths = new int[4];
        for (int i = 0; i < 4; i++) {
            segmentsLengths[i] = (int) new UInt32(byteBuffer).getValue();
            byteBuffer.get();
            byteBuffer.get();
        }

        flags = new UInt8(byteBuffer);
        byteBuffer.get();
        UInt32 crc = new UInt32(byteBuffer);

        CephCRC32C crc32Generator = new CephCRC32C();
        crc32Generator.update(bytes, 0, 28);
        byte[] b = new byte[28];
        System.arraycopy(bytes, 0, b, 0, 28);
        if (crc.getValue() != crc32Generator.getValue()) {
            throw new IllegalArgumentException(String.format("Invalid CRC32 -- %d, %d", crc.getValue(), crc32Generator.getValue()));
        }

        segments = new ArrayList<>();
        for (int i = 0; i < numSegments; i++) {
            bytes = new byte[segmentsLengths[i]];
            stream.read(bytes);
            byteBuffer = ByteBuffer.wrap(bytes);
            Segment segment = new Segment();
            segment.decode(byteBuffer);

            segments.add(segment);
        }

        bytes = new byte[17];
        stream.read(bytes);
    }

    public void encode(OutputStream stream) throws IOException {
        int messageSize = 36;
        for (Segment segment : segments) {
            messageSize += segment.getSize();
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(messageSize);
        tag.encode(byteBuffer);
        byteBuffer.put((byte) segments.size());

        for (int i = 0; i < 4; i++) {
            if (segments.size() > i) {
                UInt32.fromValue(segments.get(i).getSize()).encode(byteBuffer);
                UInt16.fromValue(8).encode(byteBuffer);
            } else {
                byteBuffer.put(new byte[6]);
            }
        }

        flags.encode(byteBuffer);
        byteBuffer.put((byte) 0);

        CephCRC32C crc32c = new CephCRC32C();
        crc32c.update(byteBuffer.array(), byteBuffer.arrayOffset(), 28);
        UInt32.fromValue(crc32c.getValue()).encode(byteBuffer);

        for (Segment segment : segments) {
            segment.encode(byteBuffer);
        }

        //lateFlags.encode(byteBuffer);
        crc32c.reset(-1);
        UInt32.fromValue(crc32c.update(byteBuffer.array(), byteBuffer.arrayOffset() + 32, 36)).encode(byteBuffer);

        stream.write(byteBuffer.array());
    }
}