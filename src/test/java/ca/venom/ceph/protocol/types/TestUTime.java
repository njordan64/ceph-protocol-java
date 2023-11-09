package ca.venom.ceph.protocol.types;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestUTime {
    @Test
    public void testEncodeLE() {
        UTime val = new UTime(1L, 1L);
        byte[] encoded = new byte[8];
        ByteBuf encodedByteBuf = Unpooled.wrappedBuffer(encoded);
        encodedByteBuf.writerIndex(0);

        val.encode(encodedByteBuf, true);
        assertArrayEquals(new byte[] {1, 0, 0, 0, 1, 0, 0, 0}, encoded);
    }

    @Test
    public void testEncodeBE() {
        UTime val = new UTime(1L, 1L);
        byte[] encoded = new byte[8];
        ByteBuf encodedByteBuf = Unpooled.wrappedBuffer(encoded);
        encodedByteBuf.writerIndex(0);

        val.encode(encodedByteBuf, false);
        assertArrayEquals(new byte[] {0, 0, 0, 1, 0, 0, 0, 1}, encoded);
    }

    @Test
    public void testDecodeLE() {
        byte[] encoded = new byte[] {1, 0, 0, 0, 1, 0, 0, 0};
        ByteBuf encodedByteBuf = Unpooled.wrappedBuffer(encoded);

        UTime val = new UTime();
        val.decode(encodedByteBuf, true);

        assertEquals(1L, val.getTime());
        assertEquals(1L, val.getNanoSeconds());
    }

    @Test
    public void testDecodeBE() {
        byte[] encoded = new byte[] {0, 0, 0, 1, 0, 0, 0, 1};
        ByteBuf encodedByteBuf = Unpooled.wrappedBuffer(encoded);

        UTime val = new UTime();
        val.decode(encodedByteBuf, false);

        assertEquals(1L, val.getTime());
        assertEquals(1L, val.getNanoSeconds());
    }
}
