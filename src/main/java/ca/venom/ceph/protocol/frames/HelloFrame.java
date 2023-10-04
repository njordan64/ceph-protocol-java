package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.NodeType;
import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.Addr;
import ca.venom.ceph.protocol.types.CephBoolean;
import ca.venom.ceph.protocol.types.CephEnum;
import ca.venom.ceph.protocol.types.CephRawByte;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

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
    public MessageType getTag() {
        return MessageType.HELLO;
    }

    @Override
    protected int encodeSegmentBody(int segmentIndex, ByteArrayOutputStream outputStream) {
        if (segmentIndex == 0) {
            nodeType.encode(outputStream);
            msgAddr2.encode(outputStream);
            constant1.encode(outputStream);
            constant2.encode(outputStream);
            addr.encode(outputStream);

            return 8;
        } else {
            return 0;
        }
    }

    @Override
    protected void decodeSegmentBody(int segmentIndex, ByteBuffer byteBuffer, int alignment) {
        if (segmentIndex == 0) {
            nodeType = CephEnum.read(byteBuffer, NodeType.class);
            msgAddr2 = CephBoolean.read(byteBuffer);
            constant1 = CephRawByte.read(byteBuffer);
            constant2 = CephRawByte.read(byteBuffer);
            addr = Addr.read(byteBuffer);
        }
    }
}
