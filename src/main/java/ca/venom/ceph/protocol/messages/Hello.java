package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.NodeType;
import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.Addr;
import ca.venom.ceph.protocol.types.CephBoolean;
import ca.venom.ceph.protocol.types.CephEnum;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class Hello extends ControlFrame {
    private CephEnum<NodeType> nodeType;
    private CephBoolean msgAddr2;
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
            outputStream.write(1);
            outputStream.write(1);
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

            byteBuffer.position(byteBuffer.position() + 2);
            addr = Addr.read(byteBuffer);
        }
    }
}
