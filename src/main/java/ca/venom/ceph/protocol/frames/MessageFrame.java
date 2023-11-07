package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.CephBitSet;
import ca.venom.ceph.protocol.types.Int16;
import ca.venom.ceph.protocol.types.Int32;
import ca.venom.ceph.protocol.types.Int64;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class MessageFrame extends ControlFrame {
    private Int64 seq;
    private Int64 tid;
    private Int16 type;
    private Int16 priority;
    private Int16 version;
    private Int32 dataPrePaddingLen;
    private Int16 dataOff;
    private Int64 ackSeq;
    private CephBitSet flags;
    private Int16 compatVersion;
    private Int16 reserved;

    private ByteBuf front = Unpooled.buffer();
    private ByteBuf middle = Unpooled.buffer();
    private ByteBuf data = Unpooled.buffer();

    public Int64 getSeq() {
        return seq;
    }

    public void setSeq(Int64 seq) {
        this.seq = seq;
    }

    public Int64 getTid() {
        return tid;
    }

    public void setTid(Int64 tid) {
        this.tid = tid;
    }

    public Int16 getType() {
        return type;
    }

    public void setType(Int16 type) {
        this.type = type;
    }

    public Int16 getPriority() {
        return priority;
    }

    public void setPriority(Int16 priority) {
        this.priority = priority;
    }

    public Int16 getVersion() {
        return version;
    }

    public void setVersion(Int16 version) {
        this.version = version;
    }

    public Int32 getDataPrePaddingLen() {
        return dataPrePaddingLen;
    }

    public void setDataPrePaddingLen(Int32 dataPrePaddingLen) {
        this.dataPrePaddingLen = dataPrePaddingLen;
    }

    public Int16 getDataOff() {
        return dataOff;
    }

    public void setDataOff(Int16 dataOff) {
        this.dataOff = dataOff;
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

    @Override
    public MessageType getTag() {
        return MessageType.MESSAGE;
    }

    @Override
    public void encodeSegment1(ByteBuf byteBuf, boolean le) {
        seq.encode(byteBuf, le);
        tid.encode(byteBuf, le);
        type.encode(byteBuf, le);
        priority.encode(byteBuf, le);
        version.encode(byteBuf, le);
        dataPrePaddingLen.encode(byteBuf, le);
        dataOff.encode(byteBuf, le);
        ackSeq.encode(byteBuf, le);
        flags.encode(byteBuf, le);
        compatVersion.encode(byteBuf, le);
        reserved.encode(byteBuf, le);
    }

    @Override
    public void encodeSegment2(ByteBuf byteBuf, boolean le) {
        if (front.readableBytes() > 0) {
            byteBuf.writeBytes(front);
        }
    }

    @Override
    public void encodeSegment3(ByteBuf byteBuf, boolean le) {
        if (middle.readableBytes() > 0) {
            byteBuf.writeBytes(middle);
        }
    }

    @Override
    public void encodeSegment4(ByteBuf byteBuf, boolean le) {
        if (data.readableBytes() > 0) {
            byteBuf.writeBytes(data);
        }
    }

    @Override
    public void decodeSegment1(ByteBuf byteBuf, boolean le) {
        seq = new Int64();
        seq.decode(byteBuf, le);
        tid = new Int64();
        tid.decode(byteBuf, le);
        type = new Int16();
        type.decode(byteBuf, le);
        priority = new Int16();
        priority.decode(byteBuf, le);
        version = new Int16();
        version.decode(byteBuf, le);
        dataPrePaddingLen = new Int32();
        dataPrePaddingLen.decode(byteBuf, le);
        dataOff = new Int16();
        dataOff.decode(byteBuf, le);
        ackSeq = new Int64();
        ackSeq.decode(byteBuf, le);
        flags = new CephBitSet(1);
        flags.decode(byteBuf, le);
        compatVersion = new Int16();
        compatVersion.decode(byteBuf, le);
        reserved = new Int16();
        reserved.decode(byteBuf, le);
    }

    @Override
    public void decodeSegment2(ByteBuf byteBuf, boolean le) {
        front.writeBytes(byteBuf);
    }

    @Override
    public void decodeSegment3(ByteBuf byteBuf, boolean le) {
        middle.writeBytes(byteBuf);
    }

    @Override
    public void decodeSegment4(ByteBuf byteBuf, boolean le) {
        data.writeBytes(byteBuf);
    }
}
