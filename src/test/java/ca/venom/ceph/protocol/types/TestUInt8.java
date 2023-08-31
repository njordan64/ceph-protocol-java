package ca.venom.ceph.protocol.types;

import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.*;

public class TestUInt8 {
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
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[] {(byte) value});
        UInt8 uint8 = UInt8.read(byteBuffer);
        assertEquals(value, uint8.getValue());
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
        UInt8 uint8 = new UInt8(value);

        ByteBuffer byteBuffer = ByteBuffer.allocate(10);
        uint8.encode(byteBuffer);

        assertEquals(1, byteBuffer.position());
        byteBuffer.flip();

        assertEquals((byte) value, byteBuffer.get());
    }
}
