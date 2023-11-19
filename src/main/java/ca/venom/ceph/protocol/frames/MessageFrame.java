package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.ControlFrameType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class MessageFrame extends ControlFrame {
    private ByteBuf head = Unpooled.buffer();
    private boolean headLE;
    private ByteBuf front = Unpooled.buffer();
    private boolean frontLE;
    private ByteBuf middle = Unpooled.buffer();
    private boolean middleLE;
    private ByteBuf data = Unpooled.buffer();
    private boolean dataLE;

    public ByteBuf getHead() {
        return head;
    }

    public void setHead(ByteBuf head) {
        this.head = head;
    }

    public boolean isHeadLE() {
        return headLE;
    }

    public void setHeadLE(boolean headLE) {
        this.headLE = headLE;
    }

    public ByteBuf getFront() {
        return front;
    }

    public void setFront(ByteBuf front) {
        this.front = front;
    }

    public boolean isFrontLE() {
        return frontLE;
    }

    public void setFrontLE(boolean frontLE) {
        this.frontLE = frontLE;
    }

    public ByteBuf getMiddle() {
        return middle;
    }

    public void setMiddle(ByteBuf middle) {
        this.middle = middle;
    }

    public boolean isMiddleLE() {
        return middleLE;
    }

    public void setMiddleLE(boolean middleLE) {
        this.middleLE = middleLE;
    }

    public ByteBuf getData() {
        return data;
    }

    public void setData(ByteBuf data) {
        this.data = data;
    }

    public boolean isDataLE() {
        return dataLE;
    }

    public void setDataLE(boolean dataLE) {
        this.dataLE = dataLE;
    }

    @Override
    public ControlFrameType getTag() {
        return ControlFrameType.MESSAGE;
    }

    @Override
    public void encodeSegment1(ByteBuf byteBuf, boolean le) {
        if (head.readableBytes() > 0) {
            head.writeBytes(byteBuf);
        }
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
        byteBuf.writeBytes(head);
        this.headLE = le;
    }

    @Override
    public void decodeSegment2(ByteBuf byteBuf, boolean le) {
        front.writeBytes(byteBuf);
        this.frontLE = le;
    }

    @Override
    public void decodeSegment3(ByteBuf byteBuf, boolean le) {
        middle.writeBytes(byteBuf);
        this.middleLE = le;
    }

    @Override
    public void decodeSegment4(ByteBuf byteBuf, boolean le) {
        data.writeBytes(byteBuf);
        this.dataLE = le;
    }
}
