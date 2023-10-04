package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.*;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.List;

public class ServerIdentFrame extends ControlFrame {
    private CephList<Addr> myAddresses;
    private Int64 globalId;
    private UInt64 globalSeq;
    private CephBitSet supportedFeatures;
    private CephBitSet requiredFeatures;
    private CephBitSet flags;
    private UInt64 clientCookie;

    public List<Addr> getMyAddresses() {
        return myAddresses.getValues();
    }

    public void setMyAddresses(List<Addr> myAddresses) {
        this.myAddresses = new CephList<>(myAddresses);
    }

    public long getGlobalId() {
        return globalId.getValue();
    }

    public void setGlobalId(long globalId) {
        this.globalId = new Int64(globalId);
    }

    public UInt64 getGlobalSeq() {
        return globalSeq;
    }

    public void setGlobalSeq(UInt64 globalSeq) {
        this.globalSeq = globalSeq;
    }

    public BitSet getSupportedFeatures() {
        return supportedFeatures.getValue();
    }

    public void setSupportedFeatures(BitSet supportedFeatures) {
        this.supportedFeatures = new CephBitSet(supportedFeatures, 8);
    }

    public BitSet getRequiredFeatures() {
        return requiredFeatures.getValue();
    }

    public void setRequiredFeatures(BitSet requiredFeatures) {
        this.requiredFeatures = new CephBitSet(requiredFeatures, 8);
    }

    public BitSet getFlags() {
        return flags.getValue();
    }

    public void setFlags(BitSet flags) {
        this.flags = new CephBitSet(flags, 8);
    }

    public UInt64 getClientCookie() {
        return clientCookie;
    }

    public void setClientCookie(UInt64 clientCookie) {
        this.clientCookie = clientCookie;
    }

    @Override
    public MessageType getTag() {
        return MessageType.SERVER_IDENT;
    }

    @Override
    protected int encodeSegmentBody(int index, ByteArrayOutputStream outputStream) {
        if (index == 0) {
            myAddresses.encode(outputStream);
            globalId.encode(outputStream);
            globalSeq.encode(outputStream);
            supportedFeatures.encode(outputStream);
            requiredFeatures.encode(outputStream);
            flags.encode(outputStream);
            clientCookie.encode(outputStream);

            return 8;
        } else {
            return 0;
        }
    }

    @Override
    protected void decodeSegmentBody(int index, ByteBuffer byteBuffer, int alignment) {
        if (index == 0) {
            myAddresses = CephList.read(byteBuffer, Addr.class);
            globalId = Int64.read(byteBuffer);
            globalSeq = UInt64.read(byteBuffer);
            supportedFeatures = CephBitSet.read(byteBuffer, 8);
            requiredFeatures = CephBitSet.read(byteBuffer, 8);
            flags = CephBitSet.read(byteBuffer, 8);
            clientCookie = UInt64.read(byteBuffer);
        }
    }
}
