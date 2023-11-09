package ca.venom.ceph.protocol.types;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestInt8 {
    @Test
    public void testEncodeLE() {
        Int8 val = new Int8(1);
        byte[] encoded = new byte[1];
        ByteBuf encodedByteBuf = Unpooled.wrappedBuffer(encoded);
        encodedByteBuf.writerIndex(0);

        val.encode(encodedByteBuf, true);
        assertArrayEquals(new byte[] {1}, encoded);
    }

    @Test
    public void testEncodeBE() {
        Int8 val = new Int8(1);
        byte[] encoded = new byte[1];
        ByteBuf encodedByteBuf = Unpooled.wrappedBuffer(encoded);
        encodedByteBuf.writerIndex(0);

        val.encode(encodedByteBuf, false);
        assertArrayEquals(new byte[] {1}, encoded);
    }

    @Test
    public void testDecodeLE() {
        byte[] encoded = new byte[] {1};
        ByteBuf encodedByteBuf = Unpooled.wrappedBuffer(encoded);

        Int8 val = new Int8();
        val.decode(encodedByteBuf, true);

        assertEquals((byte) 1, val.getValue());
    }

    @Test
    public void testDecodeBE() {
        byte[] encoded = new byte[] {1};
        ByteBuf encodedByteBuf = Unpooled.wrappedBuffer(encoded);

        Int8 val = new Int8();
        val.decode(encodedByteBuf, false);

        assertEquals((byte) 1, val.getValue());
    }

    @Test
    public void testLargeValue() {
        Int8 val = new Int8((byte) 200);
        assertEquals((byte)-56, val.getValue());
        assertEquals(200, val.getValueUnsigned());
    }
}
