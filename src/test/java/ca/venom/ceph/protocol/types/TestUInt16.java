package ca.venom.ceph.protocol.types;

import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.*;

public class TestUInt16 {
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
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[] {byte1, byte2});
        UInt16 uint16 = new UInt16(byteBuffer);
        assertEquals(value, uint16.getValue());
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
        UInt16 uint16 = UInt16.fromValue(value);

        ByteBuffer byteBuffer = ByteBuffer.allocate(10);
        uint16.encode(byteBuffer);

        assertEquals(2, byteBuffer.position());
        byteBuffer.flip();

        assertEquals(byte1, byteBuffer.get());
        assertEquals(byte2, byteBuffer.get());
    }
}
