package ca.venom.ceph.protocol;

import ca.venom.ceph.protocol.types.annotations.CephEncodingSize;
import ca.venom.ceph.protocol.types.annotations.CephField;
import ca.venom.ceph.protocol.types.annotations.CephType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class TestCephEncoder {
    @CephType
    public static class EncodeByteTest {
        @Getter
        @Setter
        @CephField
        private byte value;
    }

    @CephType
    public static class EncodeShortTest {
        @Getter
        @Setter
        @CephField
        private short value;
    }

    @CephType
    public static class EncodeIntTest {
        @Getter
        @Setter
        @CephField
        private int value;
    }

    @CephType
    public static class EncodeLongTest {
        @Getter
        @Setter
        @CephField
        private long value;
    }

    @CephType
    public static class EncodeStringTest {
        @Getter
        @Setter
        @CephField
        private String value;
    }

    @CephType
    public static class EncodeRawBytesTest {
        @Getter
        @Setter
        @CephField
        private byte[] value;
    }

    @CephType
    public static class EncodeBytesTest {
        @Getter
        @Setter
        @CephField(includeSize = true)
        private byte[] value;
    }

    @CephType
    public static class EncodeBytesFixedTest {
        @Getter
        @Setter
        @CephField
        @CephEncodingSize(8)
        private byte[] value;
    }

    @CephType
    public static class EncodeEnum1Byte {
        @Getter
        @Setter
        @CephField
        @CephEncodingSize
        private NodeType value;
    }

    @CephType
    public static class EncodeEnum2Byte {
        @Getter
        @Setter
        @CephField
        @CephEncodingSize(2)
        private NodeType value;
    }

    @CephType
    public static class EncodeEnum4Byte {
        @Getter
        @Setter
        @CephField
        @CephEncodingSize(4)
        private NodeType value;
    }

    @CephType
    public static class EncodeBitSetTest {
        @Getter
        @Setter
        @CephField
        @CephEncodingSize(4)
        private BitSet value;
    }

    @CephType
    public static class EncodeSetTest {
        @Getter
        @Setter
        @CephField
        private Set<Integer> value;
    }

    @CephType
    public static class EncodeListTest {
        @Getter
        @Setter
        @CephField
        private List<Integer> value;
    }

    @CephType
    public static class EncodeMapTest {
        @Getter
        @Setter
        @CephField
        private Map<String, Integer> value;
    }

    @Test
    public void testEncodeByte() throws Exception {
        EncodeByteTest sample = new EncodeByteTest();
        sample.setValue((byte) 3);
        byte[] expectedBytes = new byte[] {(byte) 3};
        validateEncoding(sample, expectedBytes, true);
    }

    @Test
    public void testEncodeShort() throws Exception {
        EncodeShortTest sample = new EncodeShortTest();
        sample.setValue((short) 3);
        byte[] expectedBytes = new byte[] {(byte) 3, (byte) 0};
        validateEncoding(sample, expectedBytes, true);
    }

    @Test
    public void testEncodeInt() throws Exception {
        EncodeIntTest sample = new EncodeIntTest();
        sample.setValue(3);
        byte[] expectedBytes = new byte[] {(byte) 3, (byte) 0, (byte) 0, (byte) 0};
        validateEncoding(sample, expectedBytes, true);
    }

    @Test
    public void testEncodeLong() throws Exception {
        EncodeLongTest sample = new EncodeLongTest();
        sample.setValue(3L);
        byte[] expectedBytes = new byte[] {
                (byte) 3, (byte) 0, (byte) 0, (byte) 0,
                (byte) 0, (byte) 0, (byte) 0, (byte) 0
        };
        validateEncoding(sample, expectedBytes, true);
    }

    @Test
    public void testEncodeString() throws Exception {
        EncodeStringTest sample = new EncodeStringTest();
        sample.setValue("Hello");
        byte[] expectedBytes = new byte[] {
                (byte) 5, (byte) 0, (byte) 0, (byte) 0,
                (byte) 72, (byte) 101, (byte) 108, (byte) 108,
                (byte) 111
        };
        validateEncoding(sample, expectedBytes, true);
    }

    @Test
    public void testEncodeRawBytes() throws Exception {
        EncodeRawBytesTest sample = new EncodeRawBytesTest();
        sample.setValue(new byte[] {(byte) 1, (byte) 2, (byte) 3, (byte) 4});
        byte[] expectedBytes = new byte[] {
                (byte) 1, (byte) 2, (byte) 3, (byte) 4
        };
        validateEncoding(sample, expectedBytes, true);
    }

    @Test
    public void testEncodeBytesWithSize() throws Exception {
        EncodeBytesTest sample = new EncodeBytesTest();
        sample.setValue(new byte[] {(byte) 1, (byte) 2, (byte) 3, (byte) 4});
        byte[] expectedBytes = new byte[] {
                (byte) 4, (byte) 0, (byte) 0, (byte) 0,
                (byte) 1, (byte) 2, (byte) 3, (byte) 4
        };
        validateEncoding(sample, expectedBytes, true);
    }

    @Test
    public void testEncodeBytesFixed() throws Exception {
        EncodeBytesFixedTest sample = new EncodeBytesFixedTest();
        byte[] expectedBytes = new byte[] {
                (byte) 0, (byte) 0, (byte) 0, (byte) 0,
                (byte) 0, (byte) 0, (byte) 0, (byte) 0
        };
        validateEncoding(sample, expectedBytes, true);
    }

    @Test
    public void testEncodeEnum1Byte() throws Exception {
        EncodeEnum1Byte sample = new EncodeEnum1Byte();
        sample.value = NodeType.MON;
        byte[] expectedBytes = new byte[] {(byte) 1};
        validateEncoding(sample, expectedBytes, true);
    }

    @Test
    public void testEncodeEnum2Byte() throws Exception {
        EncodeEnum2Byte sample = new EncodeEnum2Byte();
        sample.value = NodeType.MON;
        byte[] expectedBytes = new byte[] {(byte) 1, (byte) 0};
        validateEncoding(sample, expectedBytes, true);
    }

    @Test
    public void testEncodeEnum4Byte() throws Exception {
        EncodeEnum4Byte sample = new EncodeEnum4Byte();
        sample.value = NodeType.MON;
        byte[] expectedBytes = new byte[] {(byte) 1, (byte) 0, (byte) 0, (byte) 0};
        validateEncoding(sample, expectedBytes, true);
    }

    @Test
    public void testEncodeBitSet() throws Exception {
        EncodeBitSetTest sample = new EncodeBitSetTest();
        sample.value = new BitSet();
        sample.value.set(0, true);
        byte[] expectedBytes = new byte[] {(byte) 1, (byte) 0, (byte) 0, (byte) 0};
        validateEncoding(sample, expectedBytes, true);
    }

    @Test
    public void testEncodeSet() throws Exception {
        EncodeSetTest sample = new EncodeSetTest();
        sample.value = new HashSet<>();
        sample.value.add(1);
        sample.value.add(2);
        byte[] expectedBytes = new byte[] {
                (byte) 2, (byte) 0, (byte) 0, (byte) 0,
                (byte) 1, (byte) 0, (byte) 0, (byte) 0,
                (byte) 2, (byte) 0, (byte) 0, (byte) 0
        };
        validateEncoding(sample, expectedBytes, true);
    }

    @Test
    public void testEncodeList() throws Exception {
        EncodeListTest sample = new EncodeListTest();
        sample.value = new ArrayList<>();
        sample.value.add(1);
        sample.value.add(2);
        byte[] expectedBytes = new byte[] {
                (byte) 2, (byte) 0, (byte) 0, (byte) 0,
                (byte) 1, (byte) 0, (byte) 0, (byte) 0,
                (byte) 2, (byte) 0, (byte) 0, (byte) 0
        };
        validateEncoding(sample, expectedBytes, true);
    }

    @Test
    public void testEncodeMap() throws Exception {
        EncodeMapTest sample = new EncodeMapTest();
        sample.value = new HashMap<>();
        sample.value.put("a", 1);
        sample.value.put("b", 2);
        byte[] expectedBytes = new byte[] {
                (byte) 2, (byte) 0, (byte) 0, (byte) 0,
                (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 97,
                (byte) 1, (byte) 0, (byte) 0, (byte) 0,
                (byte) 1, (byte) 0, (byte) 0, (byte) 0, (byte) 98,
                (byte) 2, (byte) 0, (byte) 0, (byte) 0
        };
        validateEncoding(sample, expectedBytes, true);
    }

    private void validateEncoding(Object toEncode, byte[] expectedBytes, boolean le) throws EncodingException {
        ByteBuf byteBuf = Unpooled.buffer();
        CephEncoder.encode(toEncode, byteBuf, le);

        byte[] actualBytes = new byte[byteBuf.writerIndex() - byteBuf.readerIndex()];
        byteBuf.readBytes(actualBytes);

        assertArrayEquals(expectedBytes, actualBytes);
    }
}
