package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.ControlFrameType;
import ca.venom.ceph.protocol.types.*;
import io.netty.buffer.ByteBuf;

import java.util.BitSet;
import java.util.List;

public class ClientIdentFrame extends ControlFrame {
    private CephRawByte version = new CephRawByte((byte) 2);
    private CephList<Addr> myAddresses;
    private Addr targetAddress;
    private Int64 globalId;
    private Int64 globalSeq;
    private CephBitSet supportedFeatures;
    private CephBitSet requiredFeatures;
    private CephBitSet flags;
    private Int64 clientCookie;

    public List<Addr> getMyAddresses() {
        return myAddresses.getValues();
    }

    public void setMyAddresses(List<Addr> myAddresses) {
        this.myAddresses = new CephList<>(myAddresses, Addr.class);
    }

    public Addr getTargetAddress() {
        return targetAddress;
    }

    public void setTargetAddress(Addr targetAddress) {
        this.targetAddress = targetAddress;
    }

    public long getGlobalId() {
        return globalId.getValue();
    }

    public void setGlobalId(long globalId) {
        this.globalId = new Int64(globalId);
    }

    public long getGlobalSeq() {
        return globalSeq.getValue();
    }

    public void setGlobalSeq(long globalSeq) {
        this.globalSeq = new Int64(globalSeq);
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

    public long getClientCookie() {
        return clientCookie.getValue();
    }

    public void setClientCookie(long clientCookie) {
        this.clientCookie = new Int64(clientCookie);
    }

    @Override
    public void encodeSegment1(ByteBuf byteBuf, boolean le) {
        version.encode(byteBuf, le);
        myAddresses.encode(byteBuf, le);
        targetAddress.encode(byteBuf, le);
        globalId.encode(byteBuf, le);
        globalSeq.encode(byteBuf, le);
        supportedFeatures.encode(byteBuf, le);
        requiredFeatures.encode(byteBuf, le);
        flags.encode(byteBuf, le);
        clientCookie.encode(byteBuf, le);
    }

    @Override
    public void decodeSegment1(ByteBuf byteBuf, boolean le) {
        byte version = byteBuf.readByte();
        myAddresses = new CephList<>(Addr.class);
        myAddresses.decode(byteBuf, le);

        targetAddress = new Addr();
        targetAddress.decode(byteBuf, le);

        globalId = new Int64();
        globalId.decode(byteBuf, le);

        globalSeq = new Int64();
        globalSeq.decode(byteBuf, le);

        supportedFeatures = new CephBitSet(8);
        supportedFeatures.decode(byteBuf, le);

        requiredFeatures = new CephBitSet(8);
        requiredFeatures.decode(byteBuf, le);

        flags = new CephBitSet(8);
        flags.decode(byteBuf, le);

        clientCookie = new Int64();
        clientCookie.decode(byteBuf, le);
    }

    @Override
    public ControlFrameType getTag() {
        return ControlFrameType.CLIENT_IDENT;
    }
}
