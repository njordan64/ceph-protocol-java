package ca.venom.ceph.protocol.types;

import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.*;

public class TestUInt32 {
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
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[] {byte1, byte2, byte3, byte4});
        UInt32 uint32 = UInt32.read(byteBuffer);
        assertEquals(value, uint32.getValue());
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
        UInt32 uint32 = new UInt32(value);

        ByteBuffer byteBuffer = ByteBuffer.allocate(10);
        uint32.encode(byteBuffer);

        assertEquals(4, byteBuffer.position());
        byteBuffer.flip();

        assertEquals(byte1, byteBuffer.get());
        assertEquals(byte2, byteBuffer.get());
        assertEquals(byte3, byteBuffer.get());
        assertEquals(byte4, byteBuffer.get());
    }
}
