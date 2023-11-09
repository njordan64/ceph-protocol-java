package ca.venom.ceph.protocol.types;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestCephString {
    @Test
    public void testEncodeLE() {
        CephString val = new CephString("hi");
        byte[] encoded = new byte[6];
        ByteBuf encodedByteBuf = Unpooled.wrappedBuffer(encoded);
        encodedByteBuf.writerIndex(0);

        val.encode(encodedByteBuf, true);
        assertArrayEquals(new byte[] {2, 0, 0, 0, 104, 105}, encoded);
    }

    @Test
    public void testEncodeBE() {
        CephString val = new CephString("hi");
        byte[] encoded = new byte[6];
        ByteBuf encodedByteBuf = Unpooled.wrappedBuffer(encoded);
        encodedByteBuf.writerIndex(0);

        val.encode(encodedByteBuf, false);
        assertArrayEquals(new byte[] {0, 0, 0, 2, 104, 105}, encoded);
    }

    @Test
    public void testDecodeLE() {
        byte[] encoded = new byte[] {2, 0, 0, 0, 104, 105};
        ByteBuf encodedByteBuf = Unpooled.wrappedBuffer(encoded);

        CephString val = new CephString();
        val.decode(encodedByteBuf, true);

        assertEquals("hi", val.getValue());
    }

    @Test
    public void testDecodeBE() {
        byte[] encoded = new byte[] {0, 0, 0, 2, 104, 105};
        ByteBuf encodedByteBuf = Unpooled.wrappedBuffer(encoded);

        CephString val = new CephString();
        val.decode(encodedByteBuf, false);

        assertEquals("hi", val.getValue());
    }
}
