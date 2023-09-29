package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.MessageType;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TestAuthRequestMore {
    private static final String MESSAGE1_PATH = "authrequestmore1.bin";
    private byte[] message1Bytes;

    @Before
    public void setup() throws Exception {
        InputStream inputStream = TestAuthRequestMore.class.getClassLoader().getResourceAsStream(MESSAGE1_PATH);
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
        AuthRequestMore parsedMessage = new AuthRequestMore();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(message1Bytes, 1, message1Bytes.length - 1);
        parsedMessage.decode(inputStream);

        assertEquals(MessageType.AUTH_REQUEST_MORE, parsedMessage.getTag());
        assertEquals(0, parsedMessage.getFlags().cardinality());

        byte[] expectedAuthPayload = new byte[36];
        System.arraycopy(message1Bytes, 36, expectedAuthPayload, 0, 36);
        assertArrayEquals(expectedAuthPayload, parsedMessage.getAuthPayload());
    }

    @Test
    public void testEncodeMessage1() throws Exception {
        AuthRequestMore authRequest = new AuthRequestMore();

        byte[] authPayload = new byte[36];
        System.arraycopy(message1Bytes, 36, authPayload, 0, 36);
        authRequest.setAuthPayload(authPayload);

        assertArrayEquals(message1Bytes, authRequest.encode());
    }
}
