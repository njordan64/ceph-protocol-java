package ca.venom.ceph.protocol.types;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestCephList {
    @Test
    public void testEncodeEmpty() {
        CephList<Int8> val = new CephList<>(Collections.emptyList(), Int8.class);
        byte[] encoded = new byte[4];
        ByteBuf encodedByteBuf = Unpooled.wrappedBuffer(encoded);
        encodedByteBuf.writerIndex(0);

        val.encode(encodedByteBuf, true);
        assertArrayEquals(new byte[] {0, 0, 0, 0}, encoded);
    }

    @Test
    public void testEncodeLE() {
        List<Int8> list = new ArrayList<>();
        list.add(new Int8(1));
        list.add(new Int8(2));
        CephList<Int8> val = new CephList<>(list, Int8.class);
        byte[] encoded = new byte[6];
        ByteBuf encodedByteBuf = Unpooled.wrappedBuffer(encoded);
        encodedByteBuf.writerIndex(0);

        val.encode(encodedByteBuf, true);
        assertArrayEquals(new byte[] {2, 0, 0, 0, 1, 2}, encoded);
    }

    @Test
    public void testEncodeBE() {
        List<Int8> list = new ArrayList<>();
        list.add(new Int8(1));
        list.add(new Int8(2));
        CephList<Int8> val = new CephList<>(list, Int8.class);
        byte[] encoded = new byte[6];
        ByteBuf encodedByteBuf = Unpooled.wrappedBuffer(encoded);
        encodedByteBuf.writerIndex(0);

        val.encode(encodedByteBuf, false);
        assertArrayEquals(new byte[] {0, 0, 0, 2, 1, 2}, encoded);
    }

    @Test
    public void testDecodeEmpty() {
        byte[] encoded = new byte[] {0, 0, 0, 0};
        ByteBuf encodedByteBuf = Unpooled.wrappedBuffer(encoded);

        CephList<Int8> val = new CephList<>(Int8.class);
        val.decode(encodedByteBuf, true);

        assertTrue(val.getValues().isEmpty());
    }

    @Test
    public void testDecodeLE() {
        byte[] encoded = new byte[] {2, 0, 0, 0, 1, 2};
        ByteBuf encodedByteBuf = Unpooled.wrappedBuffer(encoded);

        CephList<Int8> val = new CephList<>(Int8.class);
        val.decode(encodedByteBuf, true);

        assertEquals(2, val.getValues().size());
        assertEquals((byte) 1, val.getValues().get(0).getValue());
        assertEquals((byte) 2, val.getValues().get(1).getValue());
    }

    @Test
    public void testDecodeBE() {
        byte[] encoded = new byte[] {0, 0, 0, 2, 1, 2};
        ByteBuf encodedByteBuf = Unpooled.wrappedBuffer(encoded);

        CephList<Int8> val = new CephList<>(Int8.class);
        val.decode(encodedByteBuf, false);

        assertEquals(2, val.getValues().size());
        assertEquals((byte) 1, val.getValues().get(0).getValue());
        assertEquals((byte) 2, val.getValues().get(1).getValue());
    }
}
