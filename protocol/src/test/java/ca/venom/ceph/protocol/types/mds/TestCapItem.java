package ca.venom.ceph.protocol.types.mds;

import ca.venom.ceph.protocol.CephDecoder;
import ca.venom.ceph.protocol.CephEncoder;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class TestCapItem {
    private static final String MESSAGE_PATH = "sample_messages/types/mds/CapItem/";

    private List<CapItem> expectedMessages;
    private List<byte[]> binaryMessages;

    @BeforeEach
    public void setup() throws Exception {
        expectedMessages = new ArrayList<>();
        binaryMessages = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            String jsonPath = MESSAGE_PATH + "CapItem_" + i + ".json";
            String binPath = MESSAGE_PATH + "CapItem_" + i + ".bin";

            InputStream jsonInputStream = TestCapItem.class.getClassLoader().getResourceAsStream(jsonPath);
            InputStream binInputStream = TestCapItem.class.getClassLoader().getResourceAsStream(binPath);

            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> jsonMap = objectMapper.readValue(jsonInputStream, HashMap.class);

            CapItem message = new CapItem();
            message.setIno(((Number) jsonMap.get("ino")).longValue());
            message.setCapId(new BigInteger(jsonMap.get("capId").toString()).longValue());
            message.setMigrateSeq(((Number) jsonMap.get("migrateSeq")).intValue());
            message.setIssueSeq(((Number) jsonMap.get("issueSeq")).intValue());
            expectedMessages.add(message);

            byte[] binaryData = binInputStream.readAllBytes();
            binaryMessages.add(binaryData);

            jsonInputStream.close();
            binInputStream.close();
        }
    }

    @Test
    public void testDecodeMessage1() throws Exception {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessages.get(0));
        CapItem decoded = CephDecoder.decode(byteBuf, true, new BitSet(64), CapItem.class);

        verifyMessage(decoded, expectedMessages.get(0));
    }

    @Test
    public void testDecodeMessage2() throws Exception {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessages.get(1));
        CapItem decoded = CephDecoder.decode(byteBuf, true, new BitSet(64), CapItem.class);

        verifyMessage(decoded, expectedMessages.get(1));
    }

    @Test
    public void testDecodeMessage3() throws Exception {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessages.get(2));
        CapItem decoded = CephDecoder.decode(byteBuf, true, new BitSet(64), CapItem.class);

        verifyMessage(decoded, expectedMessages.get(2));
    }

    @Test
    public void testDecodeMessage4() throws Exception {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessages.get(3));
        CapItem decoded = CephDecoder.decode(byteBuf, true, new BitSet(64), CapItem.class);

        verifyMessage(decoded, expectedMessages.get(3));
    }

    @Test
    public void testDecodeMessage5() throws Exception {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessages.get(4));
        CapItem decoded = CephDecoder.decode(byteBuf, true, new BitSet(64), CapItem.class);

        verifyMessage(decoded, expectedMessages.get(4));
    }

    @Test
    public void testEncodeMessage1() throws Exception {
        testEncode(0);
    }

    @Test
    public void testEncodeMessage2() throws Exception {
        testEncode(1);
    }

    @Test
    public void testEncodeMessage3() throws Exception {
        testEncode(2);
    }

    @Test
    public void testEncodeMessage4() throws Exception {
        testEncode(3);
    }

    @Test
    public void testEncodeMessage5() throws Exception {
        testEncode(4);
    }

    private void testEncode(int index) throws Exception {
        CapItem message = expectedMessages.get(index);

        ByteBuf byteBuf = Unpooled.buffer();
        CephEncoder.encode(message, byteBuf, true, new BitSet(64));

        byte[] encodedBytes = new byte[byteBuf.writerIndex()];
        byteBuf.getBytes(0, encodedBytes);

        assertArrayEquals(binaryMessages.get(index), encodedBytes);
    }

    private void verifyMessage(CapItem actual, CapItem expected) {
        assertEquals(expected.getIno(), actual.getIno());
        assertEquals(expected.getCapId(), actual.getCapId());
        assertEquals(expected.getMigrateSeq(), actual.getMigrateSeq());
        assertEquals(expected.getIssueSeq(), actual.getIssueSeq());
    }
}
