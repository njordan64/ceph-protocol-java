package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.UInt32;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TestAuthReplyMore {
    private static final String MESSAGE1_PATH = "authreplymore1.bin";
    private byte[] message1Bytes;

    @Before
    public void setup() throws Exception {
        InputStream inputStream = TestAuthReplyMore.class.getClassLoader().getResourceAsStream(MESSAGE1_PATH);
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
        AuthReplyMore parsedMessage = new AuthReplyMore();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(message1Bytes, 1, message1Bytes.length - 1);
        parsedMessage.decode(inputStream);

        assertEquals(MessageType.AUTH_REPLY_MORE, parsedMessage.getTag());
        assertEquals(0, parsedMessage.getFlags().cardinality());

        byte[] expectedAuthPayload = new byte[9];
        System.arraycopy(message1Bytes, 36, expectedAuthPayload, 0, 9);
        assertArrayEquals(expectedAuthPayload, parsedMessage.getAuthPayload());
    }

    @Test
    public void testEncodeMessage1() throws Exception {
        AuthReplyMore authRequest = new AuthReplyMore();

        byte[] authPayload = new byte[9];
        System.arraycopy(message1Bytes, 36, authPayload, 0, 9);
        authRequest.setAuthPayload(authPayload);

        assertArrayEquals(message1Bytes, authRequest.encode());
    }
}
