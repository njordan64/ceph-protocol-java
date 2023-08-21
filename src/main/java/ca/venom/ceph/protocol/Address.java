package ca.venom.ceph.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Address implements Payload {
    public static interface In {
        int getType();

        byte[] getData();
    }

    public static class In4 implements In {
        private int port;
        private final byte[] values = new byte[4];

        public In4(int port, byte byte1, byte byte2, byte byte3, byte byte4) {
            this.port = port;
            values[0] = byte1;
            values[1] = byte2;
            values[2] = byte3;
            values[3] = byte4;
        }

        public int getType() {
            return 2;
        }

        public byte[] getData() {
            return values;
        }
    }

    public static class In6 implements In {
        private int port;
        private final byte[] values = new byte[6];

        public In6(int port, byte byte1, byte byte2, byte byte3, byte byte4, byte byte5, byte byte6) {
            this.port = port;
            this.values[0] = byte1;
            this.values[1] = byte2;
            this.values[2] = byte3;
            this.values[3] = byte4;
            this.values[4] = byte5;
            this.values[5] = byte6;
        }

        public int getType() {
            return 10;
        }

        public byte[] getData() {
            return values;
        }
    }

    private byte nodeType;
    private boolean msgAddr2;
    private long nonce;
    private In in;

    public Address() {
    }

    public byte getNodeType() {
        return nodeType;
    }

    public void setNodeType(byte nodeType) {
        this.nodeType = nodeType;
    }

    public boolean isMsgAddr2() {
        return msgAddr2;
    }

    public void setMsgAddr2(boolean value) {
        msgAddr2 = value;
    }

    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }

    public In getIn() {
        return in;
    }

    public void setIn(In in) {
        this.in = in;
    }

    public void writeToStream(OutputStream ostream) throws IOException {
        //
    }

    public void readFromStream(InputStream istream) throws IOException {
        nodeType = (byte)istream.read();
        msgAddr2 = istream.read() > 0;
        istream.read(); // constant
        istream.read(); // constant

        byte[] bytes = new byte[4];
        istream.read(bytes);
        int payloadSize = HexFunctions.readUInt32(bytes, 0);

        istream.read(bytes);
        int type = HexFunctions.readUInt32(bytes, 0);

        istream.read(bytes);
        nonce = HexFunctions.readUInt32(bytes, 0);

        istream.read(bytes);
        int addrLength = HexFunctions.readUInt32(bytes, 0);

        byte[] addrBytes = new byte[addrLength];
        istream.read(addrBytes);

        switch (HexFunctions.readUInt16(addrBytes, 0)) {
            case 2:
            {
                int port = HexFunctions.readUInt16(addrBytes, 2);
                in = new In4(port, addrBytes[4], addrBytes[5], addrBytes[6], addrBytes[7]);
            }
        }
    }
}
