package ca.venom.ceph.protocol.types.mds;

import ca.venom.ceph.protocol.CephDecoder;
import ca.venom.ceph.protocol.CephEncoder;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestCapsHead {
    private static final String MESSAGE_PATH = "sample_messages/types/mds/CapsHead/";

    private List<CapsHead> expectedMessages;
    private List<byte[]> binaryMessages;

    @BeforeEach
    public void setup() throws Exception {
        expectedMessages = new ArrayList<>();
        binaryMessages = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            String jsonPath = MESSAGE_PATH + "CapsHead_" + i + ".json";
            String binPath = MESSAGE_PATH + "CapsHead_" + i + ".bin";

            InputStream jsonInputStream = TestCapsHead.class.getClassLoader().getResourceAsStream(jsonPath);
            InputStream binInputStream = TestCapsHead.class.getClassLoader().getResourceAsStream(binPath);

            ObjectMapper objectMapper = new ObjectMapper();
            CapsHead message = objectMapper.readValue(jsonInputStream, CapsHead.class);
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
        CapsHead decoded = CephDecoder.decode(byteBuf, true, new BitSet(64), CapsHead.class);

        verifyMessage(decoded, expectedMessages.get(0));
    }

    @Test
    public void testDecodeMessage2() throws Exception {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessages.get(1));
        CapsHead decoded = CephDecoder.decode(byteBuf, true, new BitSet(64), CapsHead.class);

        verifyMessage(decoded, expectedMessages.get(1));
    }

    @Test
    public void testDecodeMessage3() throws Exception {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessages.get(2));
        CapsHead decoded = CephDecoder.decode(byteBuf, true, new BitSet(64), CapsHead.class);

        verifyMessage(decoded, expectedMessages.get(2));
    }

    @Test
    public void testDecodeMessage4() throws Exception {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessages.get(3));
        CapsHead decoded = CephDecoder.decode(byteBuf, true, new BitSet(64), CapsHead.class);

        verifyMessage(decoded, expectedMessages.get(3));
    }

    @Test
    public void testDecodeMessage5() throws Exception {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessages.get(4));
        CapsHead decoded = CephDecoder.decode(byteBuf, true, new BitSet(64), CapsHead.class);

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
        CapsHead message = expectedMessages.get(index);

        ByteBuf byteBuf = Unpooled.buffer();
        CephEncoder.encode(message, byteBuf, true, new BitSet(64));

        byte[] encodedBytes = new byte[byteBuf.writerIndex()];
        byteBuf.getBytes(0, encodedBytes);

        assertArrayEquals(binaryMessages.get(index), encodedBytes);
    }

    private void verifyMessage(CapsHead actual, CapsHead expected) {
        assertEquals(expected.getOp(), actual.getOp());
        assertEquals(expected.getIno(), actual.getIno());
        assertEquals(expected.getRealm(), actual.getRealm());
        assertEquals(expected.getCapId(), actual.getCapId());
        assertEquals(expected.getSeq(), actual.getSeq());
        assertEquals(expected.getIssueSeq(), actual.getIssueSeq());
        assertEquals(expected.getCaps(), actual.getCaps());
        assertEquals(expected.getWanted(), actual.getWanted());
        assertEquals(expected.getDirty(), actual.getDirty());
        assertEquals(expected.getMigrateSeq(), actual.getMigrateSeq());
        assertEquals(expected.getSnapFollows(), actual.getSnapFollows());
        assertEquals(expected.getSnapTraceLen(), actual.getSnapTraceLen());
        assertEquals(expected.getUid(), actual.getUid());
        assertEquals(expected.getGid(), actual.getGid());
        assertEquals(expected.getMode(), actual.getMode());
        assertEquals(expected.getNlink(), actual.getNlink());
        assertEquals(expected.getXattrLen(), actual.getXattrLen());
        assertEquals(expected.getXattrVersion(), actual.getXattrVersion());
    }
}
