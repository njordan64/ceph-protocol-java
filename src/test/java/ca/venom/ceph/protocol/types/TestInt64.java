package ca.venom.ceph.protocol.types;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestInt64 {
    @Test
    public void testEncodeLE() {
        Int64 val = new Int64(1);
        byte[] encoded = new byte[8];
        ByteBuf encodedByteBuf = Unpooled.wrappedBuffer(encoded);
        encodedByteBuf.writerIndex(0);

        val.encode(encodedByteBuf, true);
        assertArrayEquals(new byte[] {1, 0, 0, 0, 0, 0, 0, 0}, encoded);
    }

    @Test
    public void testEncodeBE() {
        Int64 val = new Int64(1);
        byte[] encoded = new byte[8];
        ByteBuf encodedByteBuf = Unpooled.wrappedBuffer(encoded);
        encodedByteBuf.writerIndex(0);

        val.encode(encodedByteBuf, false);
        assertArrayEquals(new byte[] {0, 0, 0, 0, 0, 0, 0, 1}, encoded);
    }

    @Test
    public void testDecodeLE() {
        byte[] encoded = new byte[] {1, 0, 0, 0, 0, 0, 0, 0};
        ByteBuf encodedByteBuf = Unpooled.wrappedBuffer(encoded);

        Int64 val = new Int64();
        val.decode(encodedByteBuf, true);

        assertEquals((short) 1, val.getValue());
    }

    @Test
    public void testDecodeBE() {
        byte[] encoded = new byte[] {0, 0, 0, 0, 0, 0, 0, 1};
        ByteBuf encodedByteBuf = Unpooled.wrappedBuffer(encoded);

        Int64 val = new Int64();
        val.decode(encodedByteBuf, false);

        assertEquals((short) 1, val.getValue());
    }

    @Test
    public void testLargeValue() {
        Int64 val = new Int64(new BigInteger("18446744073709551615"));
        assertEquals(-1, val.getValue());
        assertEquals(new BigInteger("18446744073709551615"), val.getValueUnsigned());
    }
}
