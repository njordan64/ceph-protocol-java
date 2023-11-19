package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.NodeType;
import ca.venom.ceph.protocol.ControlFrameType;
import ca.venom.ceph.protocol.types.Addr;
import ca.venom.ceph.protocol.types.CephEnum;
import ca.venom.ceph.protocol.types.CephRawByte;
import io.netty.buffer.ByteBuf;

public class HelloFrame extends ControlFrame {
    private CephEnum<NodeType> nodeType;
    private CephRawByte constant1 = new CephRawByte((byte) 1);
    private CephRawByte constant2 = new CephRawByte((byte) 1);
    private Addr addr;

    public NodeType getNodeType() {
        return nodeType.getValue();
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = new CephEnum<>(nodeType);
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
        addr.encode(byteBuf, le);
    }

    @Override
    public void decodeSegment1(ByteBuf byteBuf, boolean le) {
        nodeType = new CephEnum<>(NodeType.class);
        nodeType.decode(byteBuf, le);

        addr = new Addr();
        addr.decode(byteBuf, le);
    }

    @Override
    public ControlFrameType getTag() {
        return ControlFrameType.HELLO;
    }
}
