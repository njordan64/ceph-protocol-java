package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.AuthMode;
import ca.venom.ceph.protocol.CephProtocolContext;
import ca.venom.ceph.protocol.types.auth.AuthRequestPayload;
import ca.venom.ceph.protocol.types.auth.EntityName;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestAuthRequestFrame {
    private static final String MESSAGE1_PATH = "frames/authrequest1.bin";
    private byte[] message1Bytes;
    private CephProtocolContext ctx;

    @BeforeEach
    public void setup() throws Exception {
        InputStream inputStream = TestAuthRequestFrame.class.getClassLoader().getResourceAsStream(MESSAGE1_PATH);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] buffer = new byte[4096];
        int bytesRead = inputStream.read(buffer);
        while (bytesRead > -1) {
            outputStream.write(buffer, 0, bytesRead);
            bytesRead = inputStream.read(buffer);
        }

        message1Bytes = outputStream.toByteArray();
        outputStream.close();
        inputStream.close();

        ctx = new CephProtocolContext();
        ctx.setRev1(true);
        ctx.setSecureMode(CephProtocolContext.SecureMode.CRC);
    }

    @Test
    public void testDecodeMessage1() throws Exception {
        AuthRequestFrame parsedMessage = new AuthRequestFrame();
        ByteBuf byteBuf = Unpooled.wrappedBuffer(message1Bytes);
        byteBuf.skipBytes(32);
        parsedMessage.decodeSegment1(byteBuf, true);

        assertEquals(2L, parsedMessage.getSegment1().getAuthMethod());

        assertEquals(2, parsedMessage.getSegment1().getPreferredModes().size());
        assertEquals(2, parsedMessage.getSegment1().getPreferredModes().get(0));
        assertEquals(1, parsedMessage.getSegment1().getPreferredModes().get(1));

        assertEquals(AuthMode.MON, parsedMessage.getSegment1().getPayload().getAuthMode());
        assertEquals(8L, parsedMessage.getSegment1().getPayload().getEntityName().getType());
        assertEquals("admin", parsedMessage.getSegment1().getPayload().getEntityName().getEntityName());
        assertEquals(0L, parsedMessage.getSegment1().getPayload().getGlobalId());
    }

    @Test
    public void testEncodeMessage1() throws Exception {
        AuthRequestFrame authRequestFrame = new AuthRequestFrame();
        authRequestFrame.setSegment1(new AuthRequestFrame.Segment1());

        authRequestFrame.getSegment1().setAuthMethod(2);

        List<Integer> preferredModes = new ArrayList<>();
        preferredModes.add(2);
        preferredModes.add(1);
        authRequestFrame.getSegment1().setPreferredModes(preferredModes);

        AuthRequestPayload payload = new AuthRequestPayload();
        payload.setAuthMode(AuthMode.MON);
        EntityName entityName = new EntityName();
        entityName.setType(8);
        entityName.setEntityName("admin");
        payload.setEntityName(entityName);
        payload.setGlobalId(0);
        authRequestFrame.getSegment1().setPayload(payload);

        byte[] expectedSegment = new byte[message1Bytes.length - 36];
        System.arraycopy(message1Bytes, 32, expectedSegment, 0, message1Bytes.length - 36);
        ByteBuf byteBuf = Unpooled.buffer();
        authRequestFrame.encodeSegment1(byteBuf, true);

        byte[] actualSegment = new byte[byteBuf.writerIndex()];
        byteBuf.readBytes(actualSegment);
        assertArrayEquals(expectedSegment, actualSegment);
    }
}
