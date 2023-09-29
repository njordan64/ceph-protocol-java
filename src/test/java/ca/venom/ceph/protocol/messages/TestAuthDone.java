package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.UInt32;
import ca.venom.ceph.protocol.types.UInt64;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigInteger;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TestAuthDone {
    private static final String MESSAGE1_PATH = "authdone1.bin";
    private byte[] message1Bytes;

    @Before
    public void setup() throws Exception {
        InputStream inputStream = TestAuthDone.class.getClassLoader().getResourceAsStream(MESSAGE1_PATH);
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
        AuthDone parsedMessage = new AuthDone();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(message1Bytes, 1, message1Bytes.length - 1);
        parsedMessage.decode(inputStream);

        assertEquals(MessageType.AUTH_DONE, parsedMessage.getTag());
        assertEquals(0, parsedMessage.getFlags().cardinality());
        assertEquals(new UInt64(new BigInteger("154220")), parsedMessage.getGlobalId());
        assertEquals(new UInt32(2L), parsedMessage.getConnectionMode());

        byte[] expectedAuthMethodPayload = new byte[274];
        System.arraycopy(message1Bytes, 48, expectedAuthMethodPayload, 0, 274);
        assertArrayEquals(expectedAuthMethodPayload, parsedMessage.getAuthMethodPayload());
    }

    @Test
    public void testEncodeMessage1() throws Exception {
        AuthDone authDone = new AuthDone();

        authDone.setGlobalId(new UInt64(new BigInteger("154220")));
        authDone.setConnectionMode(new UInt32(2L));
        byte[] authMethodPayload = new byte[274];
        System.arraycopy(message1Bytes, 48, authMethodPayload, 0, 274);
        authDone.setAuthMethodPayload(authMethodPayload);

        assertArrayEquals(message1Bytes, authDone.encode());
    }
}
