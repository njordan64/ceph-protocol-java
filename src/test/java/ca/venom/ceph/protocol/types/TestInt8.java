package ca.venom.ceph.protocol.types;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestInt8 {
    @Test
    public void testParseValue0() {
        valueTest(0);
    }

    @Test
    public void testParseValue129() {
        valueTest(129);
    }

    @Test
    public void testParseValue255() {
        valueTest(255);
    }

    private void valueTest(int value) {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(new byte[] {(byte) value});
        Int8 int8 = new Int8();
        int8.decode(byteBuf, true);
        assertEquals(value, int8.getValueUnsigned());
    }

    @Test
    public void testEncode0() {
        encodeTest(0);
    }

    @Test
    public void testEncode129() {
        encodeTest(129);
    }

    @Test
    public void testEncode255() {
        encodeTest(255);
    }

    private void encodeTest(int value) {
        Int8 uint8 = new Int8(value);

        ByteBuf byteBuf = Unpooled.buffer(10);
        uint8.encode(byteBuf, true);

        assertEquals(1, byteBuf.writerIndex());
        assertEquals((byte) value, byteBuf.readByte());
    }
}
