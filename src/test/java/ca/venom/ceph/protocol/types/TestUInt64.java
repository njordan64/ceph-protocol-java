package ca.venom.ceph.protocol.types;

import org.junit.Test;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import static org.junit.Assert.*;

public class TestUInt64 {
    @Test
    public void testParseValue0() {
        valueTest((byte) 0, (byte) 0, (byte) 0, (byte) 0,
                  (byte) 0, (byte) 0, (byte) 0, (byte) 0,
                  new BigInteger("0"));
    }

    @Test
    public void testParseValue1() {
        valueTest((byte) 0, (byte) 0, (byte) 0, (byte) 0,
                  (byte) 0, (byte) 0, (byte) 0, (byte) 129,
                  new BigInteger("9295429630892703744"));
    }

    @Test
    public void testParseValue2() {
        valueTest((byte) 255, (byte) 255, (byte) 255, (byte) 255,
                  (byte) 255, (byte) 255, (byte) 255, (byte) 255,
                  new BigInteger("18446744073709551615"));
    }

    private void valueTest(byte byte1, byte byte2, byte byte3, byte byte4,
                           byte byte5, byte byte6, byte byte7, byte byte8,
                           BigInteger value) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[] {byte1, byte2, byte3, byte4,
                                                            byte5, byte6, byte7, byte8});
        UInt64 uint64 = new UInt64(byteBuffer);
        assertEquals(value, uint64.getValue());
    }

    @Test
    public void testEncode0() {
        encodeTest(new BigInteger("0"),
                   (byte) 0, (byte) 0, (byte) 0, (byte) 0,
                   (byte) 0, (byte) 0, (byte) 0, (byte) 0);
    }

    @Test
    public void testEncode1() {
        encodeTest(new BigInteger("9295429630892703744"),
                   (byte) 0, (byte) 0, (byte) 0, (byte) 0,
                   (byte) 0, (byte) 0, (byte) 0, (byte) 129);
    }

    @Test
    public void testEncode2() {
        encodeTest(new BigInteger("18446744073709551615"),
                   (byte) 255, (byte) 255, (byte) 255, (byte) 255,
                   (byte) 255, (byte) 255, (byte) 255, (byte) 255);
    }

    private void encodeTest(BigInteger value,
                            byte byte1, byte byte2, byte byte3, byte byte4,
                            byte byte5, byte byte6, byte byte7, byte byte8) {
        UInt64 uint64 = UInt64.fromValue(value);

        ByteBuffer byteBuffer = ByteBuffer.allocate(10);
        uint64.encode(byteBuffer);

        assertEquals(8, byteBuffer.position());
        byteBuffer.flip();

        assertEquals(byte1, byteBuffer.get());
        assertEquals(byte2, byteBuffer.get());
        assertEquals(byte3, byteBuffer.get());
        assertEquals(byte4, byteBuffer.get());
        assertEquals(byte5, byteBuffer.get());
        assertEquals(byte6, byteBuffer.get());
        assertEquals(byte7, byteBuffer.get());
        assertEquals(byte8, byteBuffer.get());
    }
}
