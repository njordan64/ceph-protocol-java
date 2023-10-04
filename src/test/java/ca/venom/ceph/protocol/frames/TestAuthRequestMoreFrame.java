package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.CephBytes;
import ca.venom.ceph.protocol.types.UInt16;
import ca.venom.ceph.protocol.types.UInt32;
import ca.venom.ceph.protocol.types.UInt64;
import ca.venom.ceph.protocol.types.auth.AuthRequestMorePayload;
import ca.venom.ceph.protocol.types.auth.CephXAuthenticate;
import ca.venom.ceph.protocol.types.auth.CephXRequestHeader;
import ca.venom.ceph.protocol.types.auth.CephXTicketBlob;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigInteger;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class TestAuthRequestMoreFrame {
    private static final String MESSAGE1_PATH = "authrequestmore1.bin";
    private byte[] message1Bytes;

    @Before
    public void setup() throws Exception {
        InputStream inputStream = TestAuthRequestMoreFrame.class.getClassLoader().getResourceAsStream(MESSAGE1_PATH);
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
        AuthRequestMoreFrame parsedMessage = new AuthRequestMoreFrame();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(message1Bytes, 1, message1Bytes.length - 1);
        parsedMessage.decode(inputStream);

        assertEquals(MessageType.AUTH_REQUEST_MORE, parsedMessage.getTag());
        assertEquals(0, parsedMessage.getFlags().cardinality());

        assertEquals(0x100, parsedMessage.getPayload().getRequestHeader().getRequestType().getValue());
        assertEquals(new BigInteger("12579700140716606503"), parsedMessage.getPayload().getAuthenticate().getClientChallenge().getValue());
        assertEquals(new BigInteger("1723130686256915203"), parsedMessage.getPayload().getAuthenticate().getKey().getValue());
        assertEquals(new BigInteger("0"), parsedMessage.getPayload().getAuthenticate().getOldTicket().getSecretId().getValue());
        assertArrayEquals(new byte[0], parsedMessage.getPayload().getAuthenticate().getOldTicket().getBlob().getValue());
        assertEquals(32L, parsedMessage.getPayload().getAuthenticate().getOtherKeys().getValue());
    }

    @Test
    public void testEncodeMessage1() throws Exception {
        AuthRequestMoreFrame authRequest = new AuthRequestMoreFrame();
        AuthRequestMorePayload payload = new AuthRequestMorePayload();
        authRequest.setPayload(payload);
        CephXRequestHeader header = new CephXRequestHeader(new UInt16(0x100));
        payload.setRequestHeader(header);

        UInt64 clientChallenge = new UInt64(new BigInteger("12579700140716606503"));
        UInt64 key = new UInt64(new BigInteger("1723130686256915203"));
        UInt64 secretId = new UInt64(new BigInteger("0"));
        CephXTicketBlob ticketBlob = new CephXTicketBlob(secretId, new CephBytes(new byte[0]));
        UInt32 oldTicket = new UInt32(32L);
        payload.setAuthenticate(new CephXAuthenticate(clientChallenge, key, ticketBlob, oldTicket));

        assertArrayEquals(message1Bytes, authRequest.encode());
    }
}
