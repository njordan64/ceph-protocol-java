package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.Addr;
import ca.venom.ceph.protocol.types.UInt64;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.List;

public class ClientIdent extends ControlFrame {
    private List<Addr> myAddresses;
    private Addr targetAddress;
    private long globalId;
    private UInt64 globalSeq;
    private BitSet supportedFeatures;
    private BitSet requiredFeatures;
    private BitSet flags;
    private UInt64 clientCookie;

    public List<Addr> getMyAddresses() {
        return myAddresses;
    }

    public void setMyAddresses(List<Addr> myAddresses) {
        this.myAddresses = myAddresses;
    }

    public Addr getTargetAddress() {
        return targetAddress;
    }

    public void setTargetAddress(Addr targetAddress) {
        this.targetAddress = targetAddress;
    }

    public long getGlobalId() {
        return globalId;
    }

    public void setGlobalId(long globalId) {
        this.globalId = globalId;
    }

    public UInt64 getGlobalSeq() {
        return globalSeq;
    }

    public void setGlobalSeq(UInt64 globalSeq) {
        this.globalSeq = globalSeq;
    }

    public BitSet getSupportedFeatures() {
        return supportedFeatures;
    }

    public void setSupportedFeatures(BitSet supportedFeatures) {
        this.supportedFeatures = supportedFeatures;
    }

    public BitSet getRequiredFeatures() {
        return requiredFeatures;
    }

    public void setRequiredFeatures(BitSet requiredFeatures) {
        this.requiredFeatures = requiredFeatures;
    }

    public BitSet getFlags() {
        return flags;
    }

    public void setFlags(BitSet flags) {
        this.flags = flags;
    }

    public UInt64 getClientCookie() {
        return clientCookie;
    }

    public void setClientCookie(UInt64 clientCookie) {
        this.clientCookie = clientCookie;
    }

    @Override
    public MessageType getTag() {
        return MessageType.CLIENT_IDENT;
    }

    @Override
    protected int encodeSegmentBody(int index, ByteArrayOutputStream outputStream) {
        if (index == 0) {
            write(myAddresses, outputStream, Addr.class);
            write(targetAddress, outputStream);
            write(globalId, outputStream);
            write(globalSeq, outputStream);
            write(supportedFeatures, outputStream, 8);
            write(requiredFeatures, outputStream, 8);
            write(flags, outputStream, 8);
            write(clientCookie, outputStream);

            return 8;
        } else {
            return 0;
        }
    }

    @Override
    protected void decodeSegmentBody(int index, ByteBuffer byteBuffer, int alignment) {
        if (index == 0) {
            myAddresses = readList(byteBuffer, Addr.class);
            targetAddress = readAddr(byteBuffer);
            globalId = readLong(byteBuffer);
            globalSeq = readUInt64(byteBuffer);
            supportedFeatures = readBitSet(byteBuffer, 8);
            requiredFeatures = readBitSet(byteBuffer, 8);
            flags = readBitSet(byteBuffer, 8);
            clientCookie = readUInt64(byteBuffer);
        }
    }
}
