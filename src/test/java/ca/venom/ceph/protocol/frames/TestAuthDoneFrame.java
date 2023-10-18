package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.CephProtocolContext;
import ca.venom.ceph.protocol.HexFunctions;
import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.CephBoolean;
import ca.venom.ceph.protocol.types.CephBytes;
import ca.venom.ceph.protocol.types.CephList;
import ca.venom.ceph.protocol.types.Int32;
import ca.venom.ceph.protocol.types.UInt16;
import ca.venom.ceph.protocol.types.UInt32;
import ca.venom.ceph.protocol.types.UInt64;
import ca.venom.ceph.protocol.types.auth.AuthDonePayload;
import ca.venom.ceph.protocol.types.auth.CephXResponseHeader;
import ca.venom.ceph.protocol.types.auth.CephXTicketInfo;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Base64;
import java.util.Collections;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TestAuthDoneFrame {
    private static final String MESSAGE1_PATH = "authdone1.bin";
    private static final byte[] SERVICE_TICKET = Base64.getDecoder().decode("91f6n78UK8Lv9tZ0tY6EdZ/YlITihBSgI6SnVOhGvwUNDTWKiSF4AlNv88v7sy93");
    private static final byte[] TICKET = Base64.getDecoder().decode("AQYAAAAAAAAAYAAAAL4rUVLyUZ6U8uLKn2q2KX1BA2ClpaT4dU57vFvBokLJw8tEHY3RpNAN989DhBRrJsctXotAfx9FjlNRddQcpg+B/FOtWmMzPPzJ2ucidvQKtirpr2ovH/IMcLRHnMUEdQ==");
    private static final byte[] ENCRYPTED_SECRET = Base64.getDecoder().decode("UAAAAIsAjSn+TV0HSZPTB58SJ09igR/NHzbUmrNPYuzOshS/FbTTnjaPEOv+L+5kqYn1OEp8DvxFt3St0kq7leB0X1w2jpyupNNY2CnOnTXinu9Y");

    private byte[] message1Bytes;
    private CephProtocolContext ctx;

    @Before
    public void setup() throws Exception {
        InputStream inputStream = TestAuthDoneFrame.class.getClassLoader().getResourceAsStream(MESSAGE1_PATH);
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
        AuthDoneFrame parsedMessage = new AuthDoneFrame();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(message1Bytes, 1, message1Bytes.length - 1);
        parsedMessage.decode(inputStream, ctx);

        assertEquals(MessageType.AUTH_DONE, parsedMessage.getTag());
        assertEquals(0, parsedMessage.getFlags().cardinality());
        assertEquals(new UInt64(new BigInteger("154220")), parsedMessage.getGlobalId());
        assertEquals(new UInt32(2L), parsedMessage.getConnectionMode());

        assertEquals(0x100, parsedMessage.getPayload().getResponseHeader().getResponseType().getValue());
        assertEquals(0, parsedMessage.getPayload().getResponseHeader().getStatus().getValue());
        assertEquals(1, parsedMessage.getPayload().getTicketInfos().getValues().size());
        assertEquals(32L, parsedMessage.getPayload().getTicketInfos().getValues().get(0).getServiceId().getValue());
        assertArrayEquals(SERVICE_TICKET, parsedMessage.getPayload().getTicketInfos().getValues().get(0).getServiceTicket().getValue());
        assertFalse(parsedMessage.getPayload().getTicketInfos().getValues().get(0).getEncrypted().getValue());
        assertArrayEquals(TICKET, parsedMessage.getPayload().getTicketInfos().getValues().get(0).getTicket().getValue());
        assertArrayEquals(ENCRYPTED_SECRET, parsedMessage.getPayload().getEncryptedSecret().getValue());
        assertArrayEquals(new byte[0], parsedMessage.getPayload().getExtra().getValue());
    }

    @Test
    public void testEncodeMessage1() throws Exception {
        AuthDoneFrame authDoneFrame = new AuthDoneFrame();
        authDoneFrame.setGlobalId(new UInt64(new BigInteger("154220")));
        authDoneFrame.setConnectionMode(new UInt32(2L));

        AuthDonePayload payload = new AuthDonePayload();
        authDoneFrame.setPayload(payload);
        CephXResponseHeader responseHeader = new CephXResponseHeader(
                new UInt16(0x100),
                new Int32(0)
        );
        payload.setResponseHeader(responseHeader);

        CephXTicketInfo ticketInfo = new CephXTicketInfo(
                new UInt32(32L),
                new CephBytes(SERVICE_TICKET),
                new CephBoolean(false),
                new CephBytes(TICKET)
        );
        payload.setTicketInfos(new CephList<>(Collections.singletonList(ticketInfo)));

        payload.setEncryptedSecret(new CephBytes(ENCRYPTED_SECRET));
        payload.setExtra(new CephBytes(new byte[0]));

        assertArrayEquals(message1Bytes, authDoneFrame.encode(ctx));
    }
}
