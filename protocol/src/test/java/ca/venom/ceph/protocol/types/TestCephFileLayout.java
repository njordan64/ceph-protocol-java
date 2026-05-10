package ca.venom.ceph.protocol.types;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class TestCephFileLayout {
    private static final String MESSAGE_PATH = "sample_messages/types/CephFileLayout/";

    private List<CephFileLayout> expectedFromJson;
    private List<byte[]> binaryMessages;
    private List<Map<String, Object>> jsonMaps;

    @BeforeEach
    public void setup() throws Exception {
        expectedFromJson = new ArrayList<>();
        binaryMessages = new ArrayList<>();
        jsonMaps = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();

        for (int i = 1; i <= 5; i++) {
            String jsonPath = MESSAGE_PATH + "ceph_file_layout_" + i + ".json";
            String binPath = MESSAGE_PATH + "ceph_file_layout_" + i + ".bin";

            InputStream jsonInputStream = TestCephFileLayout.class.getClassLoader().getResourceAsStream(jsonPath);
            InputStream binInputStream = TestCephFileLayout.class.getClassLoader().getResourceAsStream(binPath);

            Map<String, Object> jsonMap = objectMapper.readValue(jsonInputStream, HashMap.class);
            jsonMaps.add(jsonMap);

            CephFileLayout fromJson = new CephFileLayout();
            fromJson.setFlStripeUnit(((Number) jsonMap.get("flStripeUnit")).intValue());
            fromJson.setFlStripeCount(((Number) jsonMap.get("flStripeCount")).intValue());
            fromJson.setFlObjectSize(((Number) jsonMap.get("flObjectSize")).intValue());
            if (jsonMap.containsKey("flCasHash")) {
                fromJson.setFlCasHash(((Number) jsonMap.get("flCasHash")).intValue());
            }
            if (jsonMap.containsKey("flObjectStripeUnit")) {
                fromJson.setFlObjectStripeUnit(((Number) jsonMap.get("flObjectStripeUnit")).intValue());
            }
            if (jsonMap.containsKey("flUnused")) {
                fromJson.setFlUnused(((Number) jsonMap.get("flUnused")).intValue());
            }
            fromJson.setFlPgPool(((Number) jsonMap.get("flPgPool")).intValue());
            expectedFromJson.add(fromJson);

            byte[] binaryData = binInputStream.readAllBytes();
            binaryMessages.add(binaryData);

            jsonInputStream.close();
            binInputStream.close();
        }
    }

    @Test
    public void testDecodeMessage1() throws Exception {
        testDecode(0);
    }

    @Test
    public void testDecodeMessage2() throws Exception {
        testDecode(1);
    }

    @Test
    public void testDecodeMessage3() throws Exception {
        testDecode(2);
    }

    @Test
    public void testDecodeMessage4() throws Exception {
        testDecode(3);
    }

    @Test
    public void testDecodeMessage5() throws Exception {
        testDecode(4);
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

    private void testDecode(int index) throws Exception {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessages.get(index));
        CephFileLayout decoded = CephDecoder.decode(byteBuf, true, new BitSet(64), CephFileLayout.class);

        verifyJsonFields(decoded, index);
    }

    private void testEncode(int index) throws Exception {
        CephFileLayout message = expectedFromJson.get(index);

        ByteBuf byteBuf = Unpooled.buffer();
        CephEncoder.encode(message, byteBuf, true, new BitSet(64));

        byte[] encodedBytes = new byte[byteBuf.writerIndex()];
        byteBuf.getBytes(0, encodedBytes);

        assertArrayEquals(binaryMessages.get(index), encodedBytes);
    }

    private void verifyJsonFields(CephFileLayout actual, int index) {
        Map<String, Object> jsonMap = jsonMaps.get(index);
        assertEquals(((Number) jsonMap.get("flStripeUnit")).intValue(), actual.getFlStripeUnit());
        assertEquals(((Number) jsonMap.get("flStripeCount")).intValue(), actual.getFlStripeCount());
        assertEquals(((Number) jsonMap.get("flObjectSize")).intValue(), actual.getFlObjectSize());
        assertEquals(((Number) jsonMap.get("flPgPool")).intValue(), actual.getFlPgPool());
        if (jsonMap.containsKey("flCasHash")) {
            assertEquals(((Number) jsonMap.get("flCasHash")).intValue(), actual.getFlCasHash());
        }
        if (jsonMap.containsKey("flObjectStripeUnit")) {
            assertEquals(((Number) jsonMap.get("flObjectStripeUnit")).intValue(), actual.getFlObjectStripeUnit());
        }
        if (jsonMap.containsKey("flUnused")) {
            assertEquals(((Number) jsonMap.get("flUnused")).intValue(), actual.getFlUnused());
        }
    }
}
