package ca.venom.ceph.protocol.types.mds;

import ca.venom.ceph.protocol.CephDecoder;
import ca.venom.ceph.protocol.CephEncoder;
import com.fasterxml.jackson.databind.JsonNode;
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

public class TestCapsExportBody {
    private static final String MESSAGE_PATH = "sample_messages/types/mds/CapsExportBody/";

    private List<CapsExportBody> expectedMessages;
    private List<byte[]> binaryMessages;

    @BeforeEach
    public void setup() throws Exception {
        expectedMessages = new ArrayList<>();
        binaryMessages = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            String jsonPath = MESSAGE_PATH + "CapsExportBody_" + i + ".json";
            String binPath = MESSAGE_PATH + "CapsExportBody_" + i + ".bin";

            InputStream jsonInputStream = TestCapsExportBody.class.getClassLoader().getResourceAsStream(jsonPath);
            InputStream binInputStream = TestCapsExportBody.class.getClassLoader().getResourceAsStream(binPath);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonInputStream);
            JsonNode peerNode = jsonNode.get("peer");

            CapsExportBody message = new CapsExportBody();
            CapPeer peer = new CapPeer();

            peer.setCapId(peerNode.get("capId").decimalValue().longValue());
            peer.setIssueSeq(peerNode.get("issueSeq").intValue());
            peer.setMseq(peerNode.get("mseq").intValue());
            peer.setMds(peerNode.get("mds").intValue());
            peer.setFlags((byte) peerNode.get("flags").asInt());

            message.setPeer(peer);
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
        CapsExportBody decoded = CephDecoder.decode(byteBuf, true, new BitSet(64), CapsExportBody.class);

        verifyMessage(decoded, expectedMessages.get(0));
    }

    @Test
    public void testDecodeMessage2() throws Exception {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessages.get(1));
        CapsExportBody decoded = CephDecoder.decode(byteBuf, true, new BitSet(64), CapsExportBody.class);

        verifyMessage(decoded, expectedMessages.get(1));
    }

    @Test
    public void testDecodeMessage3() throws Exception {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessages.get(2));
        CapsExportBody decoded = CephDecoder.decode(byteBuf, true, new BitSet(64), CapsExportBody.class);

        verifyMessage(decoded, expectedMessages.get(2));
    }

    @Test
    public void testDecodeMessage4() throws Exception {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessages.get(3));
        CapsExportBody decoded = CephDecoder.decode(byteBuf, true, new BitSet(64), CapsExportBody.class);

        verifyMessage(decoded, expectedMessages.get(3));
    }

    @Test
    public void testDecodeMessage5() throws Exception {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessages.get(4));
        CapsExportBody decoded = CephDecoder.decode(byteBuf, true, new BitSet(64), CapsExportBody.class);

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
        CapsExportBody message = expectedMessages.get(index);

        ByteBuf byteBuf = Unpooled.buffer();
        CephEncoder.encode(message, byteBuf, true, new BitSet(64));

        byte[] encodedBytes = new byte[byteBuf.writerIndex()];
        byteBuf.getBytes(0, encodedBytes);

        assertArrayEquals(binaryMessages.get(index), encodedBytes);
    }

    private void verifyMessage(CapsExportBody actual, CapsExportBody expected) {
        assertEquals(expected.getPeer().getCapId(), actual.getPeer().getCapId());
        assertEquals(expected.getPeer().getIssueSeq(), actual.getPeer().getIssueSeq());
        assertEquals(expected.getPeer().getMseq(), actual.getPeer().getMseq());
        assertEquals(expected.getPeer().getMds(), actual.getPeer().getMds());
        assertEquals(expected.getPeer().getFlags(), actual.getPeer().getFlags());
    }
}
