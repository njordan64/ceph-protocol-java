package ca.venom.ceph.protocol.types;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestInt16 {
    @Test
    public void testEncodeLE() {
        Int16 val = new Int16(1);
        byte[] encoded = new byte[2];
        ByteBuf encodedByteBuf = Unpooled.wrappedBuffer(encoded);
        encodedByteBuf.writerIndex(0);

        val.encode(encodedByteBuf, true);
        assertArrayEquals(new byte[] {1, 0}, encoded);
    }

    @Test
    public void testEncodeBE() {
        Int16 val = new Int16(1);
        byte[] encoded = new byte[2];
        ByteBuf encodedByteBuf = Unpooled.wrappedBuffer(encoded);
        encodedByteBuf.writerIndex(0);

        val.encode(encodedByteBuf, false);
        assertArrayEquals(new byte[] {0, 1}, encoded);
    }

    @Test
    public void testDecodeLE() {
        byte[] encoded = new byte[] {1, 0};
        ByteBuf encodedByteBuf = Unpooled.wrappedBuffer(encoded);

        Int16 val = new Int16();
        val.decode(encodedByteBuf, true);

        assertEquals((short) 1, val.getValue());
    }

    @Test
    public void testDecodeBE() {
        byte[] encoded = new byte[] {0, 1};
        ByteBuf encodedByteBuf = Unpooled.wrappedBuffer(encoded);

        Int16 val = new Int16();
        val.decode(encodedByteBuf, false);

        assertEquals((short) 1, val.getValue());
    }

    @Test
    public void testLargeValue() {
        Int16 val = new Int16(Short.MAX_VALUE * 2 - 1);
        assertEquals((short)-3, val.getValue());
        assertEquals(Short.MAX_VALUE * 2 - 1, val.getValueUnsigned());
    }
}
