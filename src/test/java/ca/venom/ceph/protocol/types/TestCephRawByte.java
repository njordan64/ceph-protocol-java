package ca.venom.ceph.protocol.types;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestCephRawByte {
    @Test
    public void testEncodeLE() {
        CephRawByte val = new CephRawByte((byte) 1);
        byte[] encoded = new byte[1];
        ByteBuf encodedByteBuf = Unpooled.wrappedBuffer(encoded);
        encodedByteBuf.writerIndex(0);

        val.encode(encodedByteBuf, true);
        assertArrayEquals(new byte[] {1},  encoded);
    }

    @Test
    public void testEncodeBE() {
        CephRawByte val = new CephRawByte((byte) 1);
        byte[] encoded = new byte[1];
        ByteBuf encodedByteBuf = Unpooled.wrappedBuffer(encoded);
        encodedByteBuf.writerIndex(0);

        val.encode(encodedByteBuf, false);
        assertArrayEquals(new byte[] {1},  encoded);
    }

    @Test
    public void testDecodeLE() {
        byte[] encoded = new byte[] {1};
        ByteBuf encodedByteBuf = Unpooled.wrappedBuffer(encoded);

        CephRawByte val = new CephRawByte();
        val.decode(encodedByteBuf, true);

        assertEquals((byte) 1, val.getValue());
    }

    @Test
    public void testDecodeBE() {
        byte[] encoded = new byte[] {1};
        ByteBuf encodedByteBuf = Unpooled.wrappedBuffer(encoded);

        CephRawByte val = new CephRawByte();
        val.decode(encodedByteBuf, false);

        assertEquals((byte) 1, val.getValue());
    }
}
