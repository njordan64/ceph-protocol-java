package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.HexFunctions;
import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.UInt32;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TestAuthRequest {
    private static final String MESSAGE1_PATH = "authrequest1.bin";
    private byte[] message1Bytes;

    @Before
    public void setup() throws Exception {
        InputStream inputStream = TestAuthRequest.class.getClassLoader().getResourceAsStream(MESSAGE1_PATH);
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
        AuthRequest parsedMessage = new AuthRequest();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(message1Bytes, 1, message1Bytes.length - 1);
        parsedMessage.decode(inputStream);

        assertEquals(MessageType.AUTH_REQUEST, parsedMessage.getTag());
        assertEquals(0, parsedMessage.getFlags().cardinality());

        assertEquals(2L, parsedMessage.getAuthMethod().getValue());

        assertEquals(2, parsedMessage.getPreferredModes().size());
        assertEquals(2, parsedMessage.getPreferredModes().get(0).getValue());
        assertEquals(1, parsedMessage.getPreferredModes().get(1).getValue());

        byte[] expectedAuthPayload = new byte[22];
        System.arraycopy(message1Bytes, 52, expectedAuthPayload, 0, 22);
        assertArrayEquals(expectedAuthPayload, parsedMessage.getAuthPayload());
    }

    @Test
    public void testEncodeMessage1() throws Exception {
        AuthRequest authRequest = new AuthRequest();

        authRequest.setAuthMethod(new UInt32(2));

        List<UInt32> preferredModes = new ArrayList<>();
        preferredModes.add(new UInt32(2));
        preferredModes.add(new UInt32(1));
        authRequest.setPreferredModes(preferredModes);

        byte[] authPayload = new byte[22];
        System.arraycopy(message1Bytes, 52, authPayload, 0, 22);
        authRequest.setAuthPayload(authPayload);

        assertArrayEquals(message1Bytes, authRequest.encode());
    }
}
