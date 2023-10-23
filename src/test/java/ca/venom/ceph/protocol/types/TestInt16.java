package ca.venom.ceph.protocol.types;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestInt16 {
    @Test
    public void testParseValue0() {
        valueTest((byte) 0, (byte) 0, 0);
    }

    @Test
    public void testParseValue1() {
        valueTest((byte) 0, (byte) 129, 33024);
    }

    @Test
    public void testParseValue2() {
        valueTest((byte) 255, (byte) 255, 65535);
    }

    private void valueTest(byte byte1, byte byte2, int value) {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(new byte[] {byte1, byte2});
        Int16 int16 = new Int16();
        int16.decode(byteBuf, true);
        assertEquals(value, int16.getValueUnsigned());
    }

    @Test
    public void testEncode0() {
        encodeTest(0, (byte) 0, (byte) 0);
    }

    @Test
    public void testEncode1() {
        encodeTest(33024, (byte) 0, (byte) 129);
    }

    @Test
    public void testEncode2() {
        encodeTest(65535, (byte) 255, (byte) 255);
    }

    private void encodeTest(int value, byte byte1, byte byte2) {
        Int16 uint16 = new Int16(value);

        ByteBuf byteBuf = Unpooled.buffer(10);
        uint16.encode(byteBuf, true);

        assertEquals(2, byteBuf.writerIndex());

        assertEquals(byte1, byteBuf.readByte());
        assertEquals(byte2, byteBuf.readByte());
    }
}
