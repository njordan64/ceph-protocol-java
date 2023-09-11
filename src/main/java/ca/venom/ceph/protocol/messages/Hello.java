package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.NodeType;
import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.Addr;
import ca.venom.ceph.protocol.types.UInt8;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class Hello extends ControlFrame {
    private NodeType nodeType;
    private boolean msgAddr2;
    private Addr addr;

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public boolean isMsgAddr2() {
        return msgAddr2;
    }

    public void setMsgAddr2(boolean msgAddr2) {
        this.msgAddr2 = msgAddr2;
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
            write(new UInt8(nodeType.getTypeNum()), outputStream);
            write(msgAddr2 ? (byte) 1 : (byte) 0, outputStream);
            write((byte) 1, outputStream);
            write((byte) 1, outputStream);
            write(addr, outputStream);

            return 8;
        } else {
            return 0;
        }
    }

    @Override
    protected void decodeSegmentBody(int segmentIndex, ByteBuffer byteBuffer, int alignment) {
        if (segmentIndex == 0) {
            nodeType = NodeType.getFromTypeNum(byteBuffer.get());
            msgAddr2 = byteBuffer.get() > 0;

            byteBuffer.position(byteBuffer.position() + 2);
            addr = readAddr(byteBuffer);
        }
    }
}
