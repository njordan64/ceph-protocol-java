package ca.venom.ceph.protocol.types;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class TestInt64 {
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

    @Test
    public void testParseValue3() {
        valueTest((byte) 0x2b, (byte) 0x33, (byte) 0x2f, (byte) 0x91,
                  (byte) 0xd0, (byte) 0x47, (byte) 0xbc, (byte) 0xad,
                  new BigInteger("12518960025297695531"));
    }

    private void valueTest(byte byte1, byte byte2, byte byte3, byte byte4,
                           byte byte5, byte byte6, byte byte7, byte byte8,
                           BigInteger value) {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(new byte[] {byte1, byte2, byte3, byte4,
                                                             byte5, byte6, byte7, byte8});
        Int64 int64 = new Int64();
        int64.decode(byteBuf, true);
        assertEquals(value, int64.getValueUnsigned());
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

    @Test
    public void testEncode3() {
        encodeTest(new BigInteger("12518960025297695531"),
                   (byte) 0x2b, (byte) 0x33, (byte) 0x2f, (byte) 0x91,
                   (byte) 0xd0, (byte) 0x47, (byte) 0xbc, (byte) 0xad);
    }

    private void encodeTest(BigInteger value,
                            byte byte1, byte byte2, byte byte3, byte byte4,
                            byte byte5, byte byte6, byte byte7, byte byte8) {
        Int64 int64 = new Int64(value);

        ByteBuf byteBuf = Unpooled.buffer(10);
        int64.encode(byteBuf, true);

        assertEquals(8, byteBuf.writerIndex());

        assertEquals(byte1, byteBuf.readByte());
        assertEquals(byte2, byteBuf.readByte());
        assertEquals(byte3, byteBuf.readByte());
        assertEquals(byte4, byteBuf.readByte());
        assertEquals(byte5, byteBuf.readByte());
        assertEquals(byte6, byteBuf.readByte());
        assertEquals(byte7, byteBuf.readByte());
        assertEquals(byte8, byteBuf.readByte());
    }
}
