package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.HexFunctions;
import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.UInt64;
import ca.venom.ceph.protocol.types.auth.AuthReplyMorePayload;
import ca.venom.ceph.protocol.types.auth.CephXServerChallenge;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TestAuthReplyMoreFrame {
    private static final String MESSAGE1_PATH = "authreplymore1.bin";
    private byte[] message1Bytes;

    @Before
    public void setup() throws Exception {
        InputStream inputStream = TestAuthReplyMoreFrame.class.getClassLoader().getResourceAsStream(MESSAGE1_PATH);
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
        AuthReplyMoreFrame parsedMessage = new AuthReplyMoreFrame();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(message1Bytes, 1, message1Bytes.length - 1);
        parsedMessage.decode(inputStream);

        assertEquals(MessageType.AUTH_REPLY_MORE, parsedMessage.getTag());
        assertEquals(0, parsedMessage.getFlags().cardinality());

        assertEquals(new BigInteger("13567041724457626369"), parsedMessage.getPayload().getServerChallenge().getServerChallenge().getValue());
    }

    @Test
    public void testEncodeMessage1() throws Exception {
        AuthReplyMoreFrame authReplyMore = new AuthReplyMoreFrame();
        AuthReplyMorePayload payload = new AuthReplyMorePayload();
        authReplyMore.setPayload(payload);
        CephXServerChallenge serverChallenge = new CephXServerChallenge(new UInt64(new BigInteger("12518960025297695531")));
        payload.setServerChallenge(serverChallenge);
        assertArrayEquals(message1Bytes, authReplyMore.encode());
    }
}
