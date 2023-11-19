package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.frames.MessageFrame;
import ca.venom.ceph.protocol.types.CephBitSet;
import ca.venom.ceph.protocol.types.Int16;
import ca.venom.ceph.protocol.types.Int64;
import ca.venom.ceph.protocol.types.mon.MessagePriority;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public abstract class MessageBase {
    private Int64 seq;
    private Int64 tid;
    private Int16 priority;
    private Int16 version;
    private Int64 ackSeq;
    private CephBitSet flags;
    private Int16 compatVersion;
    private Int16 reserved;

    public long getSeq() {
        return seq.getValue();
    }

    public void setSeq(long seq) {
        this.seq = new Int64(seq);
    }

    public long getTid() {
        return tid.getValue();
    }

    public void setTid(long tid) {
        this.tid = new Int64(tid);
    }

    protected abstract MessageType getType();

    public MessagePriority getPriority() {
        return MessagePriority.getFromValue(priority.getValue());
    }

    public void setPriority(MessagePriority priority) {
        this.priority = new Int16(priority.getValue());
    }

    public Int16 getVersion() {
        return version;
    }

    public void setVersion(Int16 version) {
        this.version = version;
    }

    public Int64 getAckSeq() {
        return ackSeq;
    }

    public void setAckSeq(Int64 ackSeq) {
        this.ackSeq = ackSeq;
    }

    public CephBitSet getFlags() {
        return flags;
    }

    public void setFlags(CephBitSet flags) {
        this.flags = flags;
    }

    public Int16 getCompatVersion() {
        return compatVersion;
    }

    public void setCompatVersion(Int16 compatVersion) {
        this.compatVersion = compatVersion;
    }

    public Int16 getReserved() {
        return reserved;
    }

    public void setReserved(Int16 reserved) {
        this.reserved = reserved;
    }

    public void encode(MessageFrame messageFrame) {
        ByteBuf byteBuf = Unpooled.buffer(41);
        seq.encode(byteBuf, true);
        tid.encode(byteBuf, true);
        new Int16(getType().getTagNum()).encode(byteBuf, true);
        priority.encode(byteBuf, true);
        version.encode(byteBuf, true);

        byteBuf.writeZero(6);

        ackSeq.encode(byteBuf, true);
        flags.encode(byteBuf, true);
        compatVersion.encode(byteBuf, true);
        reserved.encode(byteBuf, true);

        messageFrame.setHead(byteBuf);
        messageFrame.setHeadLE(true);
    }

    public void decode(MessageFrame messageFrame) {
        ByteBuf byteBuf = messageFrame.getHead();
        boolean le = messageFrame.isHeadLE();

        seq = new Int64();
        seq.decode(byteBuf, le);
        tid = new Int64();
        tid.decode(byteBuf, le);
        byteBuf.skipBytes(2);
        priority = new Int16();
        priority.decode(byteBuf, le);
        version = new Int16();
        version.decode(byteBuf, le);

        int dataPrePaddingLen;
        short dataOff;
        if (le) {
            dataPrePaddingLen = byteBuf.readIntLE();
            dataOff = byteBuf.readShortLE();
        } else {
            dataPrePaddingLen = byteBuf.readInt();
            dataOff = byteBuf.readShort();
        }

        ackSeq = new Int64();
        ackSeq.decode(byteBuf, le);
        flags = new CephBitSet(1);
        flags.decode(byteBuf, le);
        compatVersion = new Int16();
        compatVersion.decode(byteBuf, le);
        reserved = new Int16();
        reserved.decode(byteBuf, le);
    }

    protected void encodePayload(MessageFrame messageFrame) {
    }

    protected void decodePayload(MessageFrame messageFrame) {
    }
}
