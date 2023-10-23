package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.NodeType;
import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.Addr;
import ca.venom.ceph.protocol.types.CephBoolean;
import ca.venom.ceph.protocol.types.CephEnum;
import ca.venom.ceph.protocol.types.CephRawByte;
import io.netty.buffer.ByteBuf;

public class HelloFrame extends ControlFrame {
    private CephEnum<NodeType> nodeType;
    private CephBoolean msgAddr2;
    private CephRawByte constant1 = new CephRawByte((byte) 1);
    private CephRawByte constant2 = new CephRawByte((byte) 1);
    private Addr addr;

    public NodeType getNodeType() {
        return nodeType.getValue();
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = new CephEnum<>(nodeType);
    }

    public boolean isMsgAddr2() {
        return msgAddr2.getValue();
    }

    public void setMsgAddr2(boolean msgAddr2) {
        this.msgAddr2 = new CephBoolean(msgAddr2);
    }

    public Addr getAddr() {
        return addr;
    }

    public void setAddr(Addr addr) {
        this.addr = addr;
    }

    @Override
    public void encodeSegment1(ByteBuf byteBuf, boolean le) {
        nodeType.encode(byteBuf, le);
        msgAddr2.encode(byteBuf, le);
        constant1.encode(byteBuf, le);
        constant2.encode(byteBuf, le);
        addr.encode(byteBuf, le);
    }

    @Override
    public void decodeSegment1(ByteBuf byteBuf, boolean le) {
        nodeType = new CephEnum<>(NodeType.class);
        nodeType.decode(byteBuf, le);

        msgAddr2 = new CephBoolean();
        msgAddr2.decode(byteBuf, le);

        byteBuf.skipBytes(2);

        addr = new Addr();
        addr.decode(byteBuf, le);
    }

    @Override
    public MessageType getTag() {
        return MessageType.HELLO;
    }
}
