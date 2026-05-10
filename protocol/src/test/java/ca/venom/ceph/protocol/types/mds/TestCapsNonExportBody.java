package ca.venom.ceph.protocol.types.mds;

import ca.venom.ceph.protocol.CephDecoder;
import ca.venom.ceph.protocol.CephEncoder;
import ca.venom.ceph.protocol.types.CephFileLayout;
import ca.venom.ceph.protocol.types.TimeSpec;
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

public class TestCapsNonExportBody {
    private static final String MESSAGE_PATH = "sample_messages/types/mds/CapsNonExportBody/";

    private List<CapsNonExportBody> expectedMessages;
    private List<byte[]> binaryMessages;

    @BeforeEach
    public void setup() throws Exception {
        expectedMessages = new ArrayList<>();
        binaryMessages = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            String jsonPath = MESSAGE_PATH + "CapsNonExportBody_" + String.format("%02d", i) + ".json";
            String binPath = MESSAGE_PATH + "CapsNonExportBody_" + String.format("%02d", i) + ".bin";

            InputStream jsonInputStream = TestCapsNonExportBody.class.getClassLoader().getResourceAsStream(jsonPath);
            InputStream binInputStream = TestCapsNonExportBody.class.getClassLoader().getResourceAsStream(binPath);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonInputStream);

            CapsNonExportBody message = new CapsNonExportBody();
            message.setSize(jsonNode.get("size").longValue());
            message.setMaxSize(jsonNode.get("maxSize") != null ? jsonNode.get("maxSize").longValue() : jsonNode.get("max_Size").longValue());
            message.setTruncateSize(jsonNode.get("truncateSize").longValue());
            message.setTruncateSeq(jsonNode.get("truncateSeq").intValue());

            JsonNode mTimeNode = jsonNode.get("mTime");
            if (mTimeNode == null) {
                mTimeNode = jsonNode.get("mtime");
            }
            TimeSpec mTime = new TimeSpec();
            mTime.setTvSec(mTimeNode.get("tvSec").intValue());
            mTime.setTvNSec(mTimeNode.get("tvNSec").intValue());
            message.setMTime(mTime);

            JsonNode aTimeNode = jsonNode.get("atime");
            TimeSpec aTime = new TimeSpec();
            aTime.setTvSec(aTimeNode.get("tvSec").intValue());
            aTime.setTvNSec(aTimeNode.get("tvNSec").intValue());
            message.setATime(aTime);

            JsonNode cTimeNode = jsonNode.get("ctime");
            TimeSpec cTime = new TimeSpec();
            cTime.setTvSec(cTimeNode.get("tvSec").intValue());
            cTime.setTvNSec(cTimeNode.get("tvNSec").intValue());
            message.setCTime(cTime);

            JsonNode layoutNode = jsonNode.get("layout");
            CephFileLayout layout = new CephFileLayout();
            layout.setFlStripeUnit(layoutNode.get("flStripeUnit").intValue());
            layout.setFlStripeCount(layoutNode.get("flStripeCount").intValue());
            layout.setFlObjectSize(layoutNode.get("flObjectSize").intValue());
            layout.setFlCasHash(layoutNode.get("flCasHash").intValue());
            layout.setFlObjectStripeUnit(layoutNode.get("flObjectStripeUnit").intValue());
            layout.setFlUnused(layoutNode.get("flUnused").intValue());
            layout.setFlPgPool(layoutNode.get("flPgPool").intValue());
            message.setLayout(layout);

            message.setTimeWarpSeq(jsonNode.get("timeWarpSeq").intValue());

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
        CapsNonExportBody decoded = CephDecoder.decode(byteBuf, true, new BitSet(64), CapsNonExportBody.class);

        verifyMessage(decoded, expectedMessages.get(0));
    }

    @Test
    public void testDecodeMessage2() throws Exception {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessages.get(1));
        CapsNonExportBody decoded = CephDecoder.decode(byteBuf, true, new BitSet(64), CapsNonExportBody.class);

        verifyMessage(decoded, expectedMessages.get(1));
    }

    @Test
    public void testDecodeMessage3() throws Exception {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessages.get(2));
        CapsNonExportBody decoded = CephDecoder.decode(byteBuf, true, new BitSet(64), CapsNonExportBody.class);

        verifyMessage(decoded, expectedMessages.get(2));
    }

    @Test
    public void testDecodeMessage4() throws Exception {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessages.get(3));
        CapsNonExportBody decoded = CephDecoder.decode(byteBuf, true, new BitSet(64), CapsNonExportBody.class);

        verifyMessage(decoded, expectedMessages.get(3));
    }

    @Test
    public void testDecodeMessage5() throws Exception {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessages.get(4));
        CapsNonExportBody decoded = CephDecoder.decode(byteBuf, true, new BitSet(64), CapsNonExportBody.class);

        verifyMessage(decoded, expectedMessages.get(4));
    }

    @Test
    public void testDecodeMessage6() throws Exception {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessages.get(5));
        CapsNonExportBody decoded = CephDecoder.decode(byteBuf, true, new BitSet(64), CapsNonExportBody.class);

        verifyMessage(decoded, expectedMessages.get(5));
    }

    @Test
    public void testDecodeMessage7() throws Exception {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessages.get(6));
        CapsNonExportBody decoded = CephDecoder.decode(byteBuf, true, new BitSet(64), CapsNonExportBody.class);

        verifyMessage(decoded, expectedMessages.get(6));
    }

    @Test
    public void testDecodeMessage8() throws Exception {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessages.get(7));
        CapsNonExportBody decoded = CephDecoder.decode(byteBuf, true, new BitSet(64), CapsNonExportBody.class);

        verifyMessage(decoded, expectedMessages.get(7));
    }

    @Test
    public void testDecodeMessage9() throws Exception {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessages.get(8));
        CapsNonExportBody decoded = CephDecoder.decode(byteBuf, true, new BitSet(64), CapsNonExportBody.class);

        verifyMessage(decoded, expectedMessages.get(8));
    }

    @Test
    public void testDecodeMessage10() throws Exception {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessages.get(9));
        CapsNonExportBody decoded = CephDecoder.decode(byteBuf, true, new BitSet(64), CapsNonExportBody.class);

        verifyMessage(decoded, expectedMessages.get(9));
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

    @Test
    public void testEncodeMessage6() throws Exception {
        testEncode(5);
    }

    @Test
    public void testEncodeMessage7() throws Exception {
        testEncode(6);
    }

    @Test
    public void testEncodeMessage8() throws Exception {
        testEncode(7);
    }

    @Test
    public void testEncodeMessage9() throws Exception {
        testEncode(8);
    }

    @Test
    public void testEncodeMessage10() throws Exception {
        testEncode(9);
    }

    private void testEncode(int index) throws Exception {
        CapsNonExportBody message = expectedMessages.get(index);

        ByteBuf byteBuf = Unpooled.buffer();
        CephEncoder.encode(message, byteBuf, true, new BitSet(64));

        byte[] encodedBytes = new byte[byteBuf.writerIndex()];
        byteBuf.getBytes(0, encodedBytes);

        assertArrayEquals(binaryMessages.get(index), encodedBytes);
    }

    private void verifyMessage(CapsNonExportBody actual, CapsNonExportBody expected) {
        assertEquals(expected.getSize(), actual.getSize());
        assertEquals(expected.getMaxSize(), actual.getMaxSize());
        assertEquals(expected.getTruncateSize(), actual.getTruncateSize());
        assertEquals(expected.getTruncateSeq(), actual.getTruncateSeq());

        assertEquals(expected.getMTime().getTvSec(), actual.getMTime().getTvSec());
        assertEquals(expected.getMTime().getTvNSec(), actual.getMTime().getTvNSec());

        assertEquals(expected.getATime().getTvSec(), actual.getATime().getTvSec());
        assertEquals(expected.getATime().getTvNSec(), actual.getATime().getTvNSec());

        assertEquals(expected.getCTime().getTvSec(), actual.getCTime().getTvSec());
        assertEquals(expected.getCTime().getTvNSec(), actual.getCTime().getTvNSec());

        assertEquals(expected.getLayout().getFlStripeUnit(), actual.getLayout().getFlStripeUnit());
        assertEquals(expected.getLayout().getFlStripeCount(), actual.getLayout().getFlStripeCount());
        assertEquals(expected.getLayout().getFlObjectSize(), actual.getLayout().getFlObjectSize());
        assertEquals(expected.getLayout().getFlCasHash(), actual.getLayout().getFlCasHash());
        assertEquals(expected.getLayout().getFlObjectStripeUnit(), actual.getLayout().getFlObjectStripeUnit());
        assertEquals(expected.getLayout().getFlUnused(), actual.getLayout().getFlUnused());
        assertEquals(expected.getLayout().getFlPgPool(), actual.getLayout().getFlPgPool());

        assertEquals(expected.getTimeWarpSeq(), actual.getTimeWarpSeq());
    }
}
