package ca.venom.ceph.protocol.types;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestInt32 {
    @Test
    public void testParseValue0() {
        valueTest((byte) 0, (byte) 0, (byte) 0, (byte) 0, 0L);
    }

    @Test
    public void testParseValue1() {
        valueTest((byte) 0, (byte) 0, (byte) 0, (byte) 129, 2164260864L);
    }

    @Test
    public void testParseValue2() {
        valueTest((byte) 255, (byte) 255, (byte) 255, (byte) 255, 4294967295L);
    }

    private void valueTest(byte byte1, byte byte2, byte byte3, byte byte4, long value) {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(new byte[] {byte1, byte2, byte3, byte4});
        Int32 int32 = new Int32();
        int32.decode(byteBuf, true);
        assertEquals(value, int32.getValueUnsigned());
    }

    @Test
    public void testEncode0() {
        encodeTest(0L, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
    }

    @Test
    public void testEncode1() {
        encodeTest(2164260864L, (byte) 0, (byte) 0, (byte) 0, (byte) 129);
    }

    @Test
    public void testEncode2() {
        encodeTest(4294967295L, (byte) 255, (byte) 255, (byte) 255, (byte) 255);
    }

    private void encodeTest(long value, byte byte1, byte byte2, byte byte3, byte byte4) {
        Int32 int32 = new Int32(value);

        ByteBuf byteBuf = Unpooled.buffer(10);
        int32.encode(byteBuf, true);

        assertEquals(4, byteBuf.writerIndex());

        assertEquals(byte1, byteBuf.readByte());
        assertEquals(byte2, byteBuf.readByte());
        assertEquals(byte3, byteBuf.readByte());
        assertEquals(byte4, byteBuf.readByte());
    }
}
