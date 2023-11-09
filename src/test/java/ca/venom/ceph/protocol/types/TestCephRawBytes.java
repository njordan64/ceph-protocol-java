package ca.venom.ceph.protocol.types;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class TestCephRawBytes {
    @Test
    public void testEncodeLE() {
        CephRawBytes val = new CephRawBytes(new byte[] {1, 2});
        byte[] encoded = new byte[2];
        ByteBuf encodedByteBuf = Unpooled.wrappedBuffer(encoded);
        encodedByteBuf.writerIndex(0);

        val.encode(encodedByteBuf, true);
        assertArrayEquals(new byte[] {1, 2},  encoded);
    }

    @Test
    public void testEncodeBE() {
        CephRawBytes val = new CephRawBytes(new byte[] {1, 2});
        byte[] encoded = new byte[2];
        ByteBuf encodedByteBuf = Unpooled.wrappedBuffer(encoded);
        encodedByteBuf.writerIndex(0);

        val.encode(encodedByteBuf, false);
        assertArrayEquals(new byte[] {1, 2},  encoded);
    }

    @Test
    public void testDecodeLE() {
        byte[] encoded = new byte[] {1, 2};
        ByteBuf encodedByteBuf = Unpooled.wrappedBuffer(encoded);

        CephRawBytes val = new CephRawBytes(2);
        val.decode(encodedByteBuf, true);

        assertArrayEquals(new byte[] {1, 2}, val.getValue());
    }

    @Test
    public void testDecodeBE() {
        byte[] encoded = new byte[] {1, 2};
        ByteBuf encodedByteBuf = Unpooled.wrappedBuffer(encoded);

        CephRawBytes val = new CephRawBytes(2);
        val.decode(encodedByteBuf, false);

        assertArrayEquals(new byte[] {1, 2}, val.getValue());
    }
}
