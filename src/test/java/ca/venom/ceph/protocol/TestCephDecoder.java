package ca.venom.ceph.protocol;

import ca.venom.ceph.NodeType;
import ca.venom.ceph.protocol.types.annotations.CephEncodingSize;
import ca.venom.ceph.protocol.types.annotations.CephField;
import ca.venom.ceph.protocol.types.annotations.CephType;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestCephDecoder {
    @CephType
    public static class DecodeByteTest {
        @Getter
        @Setter
        @CephField
        private byte value;
    }

    @CephType
    public static class DecodeShortTest {
        @Getter
        @Setter
        @CephField
        private short value;
    }

    @CephType
    public static class DecodeIntTest {
        @Getter
        @Setter
        @CephField
        private int value;
    }

    @CephType
    public static class DecodeLongTest {
        @Getter
        @Setter
        @CephField
        private long value;
    }

    @CephType
    public static class DecodeStringTest {
        @Getter
        @Setter
        @CephField
        private String value;
    }

    @CephType
    public static class DecodeRawBytesTest {
        @Getter
        @Setter
        @CephField
        @CephEncodingSize(3)
        private byte[] value;
    }

    @CephType
    public static class DecodeBytesTest {
        @Getter
        @Setter
        @CephField(includeSize = true)
        private byte[] value;
    }

    @CephType
    public static class DecodeEnum1Byte {
        @Getter
        @Setter
        @CephField
        @CephEncodingSize
        private NodeType value;
    }

    @CephType
    public static class DecodeEnum2Byte {
        @Getter
        @Setter
        @CephField
        @CephEncodingSize(2)
        private NodeType value;
    }

    @CephType
    public static class DecodeEnum4Byte {
        @Getter
        @Setter
        @CephField
        @CephEncodingSize(4)
        private NodeType value;
    }

    @CephType
    public static class DecodeBitSetTest {
        @Getter
        @Setter
        @CephField
        @CephEncodingSize(4)
        private BitSet value;
    }

    @CephType
    public static class DecodeSetTest {
        @Getter
        @Setter
        @CephField
        private Set<Integer> value;
    }

    @CephType
    public static class DecodeListTest {
        @Getter
        @Setter
        @CephField
        private List<Integer> value;
    }

    @CephType
    public static class DecodeMapTest {
        @Getter
        @Setter
        @CephField
        private Map<String, Integer> value;
    }

    @CephType
    public static class DecodeNestedTest {
        @Getter
        @Setter
        @CephField
        private String strValue;

        @Getter
        @Setter
        @CephField(order = 2)
        private DecodeIntTest intValue;

        @Getter
        @Setter
        @CephField(order = 3)
        private List<Integer> listValue;
    }

    @Test
    public void testDecodeByte() throws Exception {
        byte[] bytes = new byte[] {(byte) 3};
        DecodeByteTest value = CephDecoder.decode(Unpooled.wrappedBuffer(bytes), true, DecodeByteTest.class);
        assertEquals((byte) 3, value.getValue());
    }

    @Test
    public void testDecodeShort() throws Exception {
        byte[] bytes = new byte[] {(byte) 3, (byte) 0};
        DecodeShortTest value = CephDecoder.decode(Unpooled.wrappedBuffer(bytes), true, DecodeShortTest.class);
        assertEquals((short) 3, value.getValue());
    }

    @Test
    public void testDecodeInt() throws Exception {
        byte[] bytes = new byte[] {(byte) 3, (byte) 0, (byte) 0, (byte) 0};
        DecodeIntTest value = CephDecoder.decode(Unpooled.wrappedBuffer(bytes), true, DecodeIntTest.class);
        assertEquals(3, value.getValue());
    }

    @Test
    public void testDecodeLong() throws Exception {
        byte[] bytes = new byte[] {
                (byte) 3, (byte) 0, (byte) 0, (byte) 0,
                (byte) 0, (byte) 0, (byte) 0, (byte) 0
        };
        DecodeLongTest value = CephDecoder.decode(Unpooled.wrappedBuffer(bytes), true, DecodeLongTest.class);
        assertEquals(3, value.getValue());
    }

    @Test
    public void testDecodeString() throws Exception {
        byte[] bytes = new byte[] {
                (byte) 1, (byte) 0, (byte) 0, (byte) 0,
                (byte) 97
        };
        DecodeStringTest value = CephDecoder.decode(Unpooled.wrappedBuffer(bytes), true, DecodeStringTest.class);
        assertEquals("a", value.getValue());
    }

    @Test
    public void testDecodeRawBytes() throws Exception {
        byte[] bytes = new byte[] {(byte) 3, (byte) 2, (byte) 1};
        DecodeRawBytesTest value = CephDecoder.decode(Unpooled.wrappedBuffer(bytes), true, DecodeRawBytesTest.class);
        assertArrayEquals(bytes, value.getValue());
    }

    @Test
    public void testDecodeBytes() throws Exception {
        byte[] bytes = new byte[] {
                (byte) 3, (byte) 0, (byte) 0, (byte) 0,
                (byte) 3, (byte) 2, (byte) 1
        };
        DecodeBytesTest value = CephDecoder.decode(Unpooled.wrappedBuffer(bytes), true, DecodeBytesTest.class);
        assertArrayEquals(new byte[] {(byte) 3, (byte) 2, (byte) 1}, value.getValue());
    }

    @Test
    public void testDecodeEnum1Byte() throws Exception {
        byte[] bytes = new byte[] {(byte) NodeType.MDS.getValueInt()};
        DecodeEnum1Byte value = CephDecoder.decode(Unpooled.wrappedBuffer(bytes), true, DecodeEnum1Byte.class);
        assertEquals(NodeType.MDS, value.getValue());
    }

    @Test
    public void testDecodeEnum2Byte() throws Exception {
        byte[] bytes = new byte[] {(byte) NodeType.MDS.getValueInt(), (byte) 0};
        DecodeEnum2Byte value = CephDecoder.decode(Unpooled.wrappedBuffer(bytes), true, DecodeEnum2Byte.class);
        assertEquals(NodeType.MDS, value.getValue());
    }

    @Test
    public void testDecodeEnum4Byte() throws Exception {
        byte[] bytes = new byte[] {(byte) NodeType.MDS.getValueInt(), (byte) 0, (byte) 0, (byte) 0};
        DecodeEnum4Byte value = CephDecoder.decode(Unpooled.wrappedBuffer(bytes), true, DecodeEnum4Byte.class);
        assertEquals(NodeType.MDS, value.getValue());
    }

    @Test
    public void testDecodeBitSet() throws Exception {
        byte[] bytes = new byte[] {(byte) 5, (byte) 0, (byte) 0, (byte) 0};
        DecodeBitSetTest value = CephDecoder.decode(Unpooled.wrappedBuffer(bytes), true, DecodeBitSetTest.class);
        assertEquals(2, value.value.cardinality());
        assertTrue(value.value.get(0));
        assertTrue(value.value.get(2));
    }

    @Test
    public void testDecodeSet() throws Exception {
        byte[] bytes = new byte[] {
                (byte) 3, (byte) 0, (byte) 0, (byte) 0,
                (byte) 3, (byte) 0, (byte) 0, (byte) 0,
                (byte) 2, (byte) 0, (byte) 0, (byte) 0,
                (byte) 1, (byte) 0, (byte) 0, (byte) 0
        };
        DecodeSetTest value = CephDecoder.decode(Unpooled.wrappedBuffer(bytes), true, DecodeSetTest.class);
        assertEquals(3, value.value.size());
        assertTrue(value.value.contains(3));
        assertTrue(value.value.contains(2));
        assertTrue(value.value.contains(1));
    }

    @Test
    public void testDecodeList() throws Exception {
        byte[] bytes = new byte[] {
                (byte) 3, (byte) 0, (byte) 0, (byte) 0,
                (byte) 3, (byte) 0, (byte) 0, (byte) 0,
                (byte) 2, (byte) 0, (byte) 0, (byte) 0,
                (byte) 1, (byte) 0, (byte) 0, (byte) 0
        };
        DecodeListTest value = CephDecoder.decode(Unpooled.wrappedBuffer(bytes), true, DecodeListTest.class);
        assertEquals(3, value.value.size());
        assertEquals(3, value.value.get(0));
        assertEquals(2, value.value.get(1));
        assertEquals(1, value.value.get(2));
    }

    @Test
    public void testDecodeMap() throws Exception {
        byte[] bytes = new byte[] {
                (byte) 3, (byte) 0, (byte) 0, (byte) 0,
                (byte) 1, (byte) 0, (byte) 0, (byte) 0,
                (byte) 97,
                (byte) 3, (byte) 0, (byte) 0, (byte) 0,
                (byte) 1, (byte) 0, (byte) 0, (byte) 0,
                (byte) 98,
                (byte) 2, (byte) 0, (byte) 0, (byte) 0,
                (byte) 1, (byte) 0, (byte) 0, (byte) 0,
                (byte) 99,
                (byte) 1, (byte) 0, (byte) 0, (byte) 0
        };
        DecodeMapTest value = CephDecoder.decode(Unpooled.wrappedBuffer(bytes), true, DecodeMapTest.class);
        assertEquals(3, value.value.size());
        assertEquals(3, value.value.get("a"));
        assertEquals(2, value.value.get("b"));
        assertEquals(1, value.value.get("c"));
    }

    @Test
    public void testNested() throws Exception {
        byte[] bytes = new byte[] {
                (byte) 1, (byte) 0, (byte) 0, (byte) 0,
                (byte) 97,
                (byte) 7, (byte) 0, (byte) 0, (byte) 0,
                (byte) 2, (byte) 0, (byte) 0, (byte) 0,
                (byte) 5, (byte) 0, (byte) 0, (byte) 0,
                (byte) 4, (byte) 0, (byte) 0, (byte) 0
        };
        DecodeNestedTest value = CephDecoder.decode(Unpooled.wrappedBuffer(bytes), true, DecodeNestedTest.class);
        assertEquals("a", value.getStrValue());
        assertEquals(7, value.getIntValue().getValue());
        assertEquals(2, value.getListValue().size());
        assertEquals(5, value.getListValue().get(0));
        assertEquals(4, value.getListValue().get(1));
    }
}
