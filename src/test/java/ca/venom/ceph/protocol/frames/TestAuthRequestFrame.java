package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.AuthMode;
import ca.venom.ceph.protocol.CephProtocolContext;
import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.CephString;
import ca.venom.ceph.protocol.types.Int32;
import ca.venom.ceph.protocol.types.Int64;
import ca.venom.ceph.protocol.types.auth.AuthRequestPayload;
import ca.venom.ceph.protocol.types.auth.EntityName;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TestAuthRequestFrame {
    private static final String MESSAGE1_PATH = "authrequest1.bin";
    private byte[] message1Bytes;
    private CephProtocolContext ctx;

    @Before
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

        assertEquals(2L, parsedMessage.getAuthMethod().getValue());

        assertEquals(2, parsedMessage.getPreferredModes().size());
        assertEquals(2, parsedMessage.getPreferredModes().get(0).getValue());
        assertEquals(1, parsedMessage.getPreferredModes().get(1).getValue());

        assertEquals(AuthMode.MON, parsedMessage.getPayload().getAuthMode());
        assertEquals(8L, parsedMessage.getPayload().getEntityName().getType().getValue());
        assertEquals("admin", parsedMessage.getPayload().getEntityName().getEntityName().getValue());
        assertEquals(0L, parsedMessage.getPayload().getGlobalId().getValue());
    }

    @Test
    public void testEncodeMessage1() throws Exception {
        AuthRequestFrame authRequestFrame = new AuthRequestFrame();

        authRequestFrame.setAuthMethod(new Int32(2));

        List<Int32> preferredModes = new ArrayList<>();
        preferredModes.add(new Int32(2));
        preferredModes.add(new Int32(1));
        authRequestFrame.setPreferredModes(preferredModes);

        AuthRequestPayload payload = new AuthRequestPayload();
        payload.setAuthMode(AuthMode.MON);
        EntityName entityName = new EntityName();
        entityName.setType(new Int32(8));
        entityName.setEntityName(new CephString("admin"));
        payload.setEntityName(entityName);
        payload.setGlobalId(new Int64(0));
        authRequestFrame.setPayload(payload);

        byte[] expectedSegment = new byte[message1Bytes.length - 36];
        System.arraycopy(message1Bytes, 32, expectedSegment, 0, message1Bytes.length - 36);
        ByteBuf byteBuf = Unpooled.buffer();
        authRequestFrame.encodeSegment1(byteBuf, true);

        byte[] actualSegment = new byte[byteBuf.writerIndex()];
        byteBuf.readBytes(actualSegment);
        assertArrayEquals(expectedSegment, actualSegment);
    }
}
