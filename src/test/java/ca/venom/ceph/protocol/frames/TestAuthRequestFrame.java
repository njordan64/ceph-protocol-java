package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.AuthMode;
import ca.venom.ceph.protocol.HexFunctions;
import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.CephString;
import ca.venom.ceph.protocol.types.UInt32;
import ca.venom.ceph.protocol.types.UInt64;
import ca.venom.ceph.protocol.types.auth.AuthRequestPayload;
import ca.venom.ceph.protocol.types.auth.EntityName;
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
    }

    @Test
    public void testDecodeMessage1() throws Exception {
        AuthRequestFrame parsedMessage = new AuthRequestFrame();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(message1Bytes, 1, message1Bytes.length - 1);
        parsedMessage.decode(inputStream);

        assertEquals(MessageType.AUTH_REQUEST, parsedMessage.getTag());
        assertEquals(0, parsedMessage.getFlags().cardinality());

        assertEquals(2L, parsedMessage.getAuthMethod().getValue());

        assertEquals(2, parsedMessage.getPreferredModes().size());
        assertEquals(2, parsedMessage.getPreferredModes().get(0).getValue());
        assertEquals(1, parsedMessage.getPreferredModes().get(1).getValue());

        assertEquals(AuthMode.MON, parsedMessage.getPayload().getAuthMode());
        assertEquals(8L, parsedMessage.getPayload().getEntityName().getType().getValue());
        assertEquals("admin", parsedMessage.getPayload().getEntityName().getEntityName().getValue());
        assertEquals(new BigInteger("0"), parsedMessage.getPayload().getGlobalId().getValue());
    }

    @Test
    public void testEncodeMessage1() throws Exception {
        AuthRequestFrame authRequestFrame = new AuthRequestFrame();

        authRequestFrame.setAuthMethod(new UInt32(2));

        List<UInt32> preferredModes = new ArrayList<>();
        preferredModes.add(new UInt32(2));
        preferredModes.add(new UInt32(1));
        authRequestFrame.setPreferredModes(preferredModes);

        AuthRequestPayload payload = new AuthRequestPayload();
        payload.setAuthMode(AuthMode.MON);
        EntityName entityName = new EntityName(new UInt32(8), new CephString("admin"));
        payload.setEntityName(entityName);
        payload.setGlobalId(new UInt64(new BigInteger("0")));
        authRequestFrame.setPayload(payload);

        assertArrayEquals(message1Bytes, authRequestFrame.encode());
    }
}
