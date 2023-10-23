package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.CephProtocolContext;
import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.CephBoolean;
import ca.venom.ceph.protocol.types.CephBytes;
import ca.venom.ceph.protocol.types.CephList;
import ca.venom.ceph.protocol.types.Int16;
import ca.venom.ceph.protocol.types.Int32;
import ca.venom.ceph.protocol.types.Int64;
import ca.venom.ceph.protocol.types.auth.AuthDonePayload;
import ca.venom.ceph.protocol.types.auth.CephXResponseHeader;
import ca.venom.ceph.protocol.types.auth.CephXTicketInfo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

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
        ByteBuf byteBuf = Unpooled.wrappedBuffer(message1Bytes);
        byteBuf.skipBytes(32);
        parsedMessage.decodeSegment1(byteBuf, true);

        assertEquals(MessageType.AUTH_DONE, parsedMessage.getTag());
        assertEquals(new Int64(new BigInteger("154220")), parsedMessage.getGlobalId());
        assertEquals(new Int32(2), parsedMessage.getConnectionMode());

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
    public void testEncodeMessage1() {
        AuthDoneFrame authDoneFrame = new AuthDoneFrame();
        authDoneFrame.setGlobalId(new Int64(new BigInteger("154220")));
        authDoneFrame.setConnectionMode(new Int32(2));

        AuthDonePayload payload = new AuthDonePayload();
        authDoneFrame.setPayload(payload);
        CephXResponseHeader responseHeader = new CephXResponseHeader();
        responseHeader.setResponseType(new Int16((short) 0x100));
        responseHeader.setStatus(new Int32(0));
        payload.setResponseHeader(responseHeader);

        CephXTicketInfo ticketInfo = new CephXTicketInfo();
        ticketInfo.setServiceId(new Int32(32));
        ticketInfo.setServiceTicket(new CephBytes(SERVICE_TICKET));
        ticketInfo.setEncrypted(new CephBoolean(false));
        ticketInfo.setTicket(new CephBytes(TICKET));
        List<CephXTicketInfo> ticketInfoList = new ArrayList<>();
        ticketInfoList.add(ticketInfo);
        CephList<CephXTicketInfo> ticketInfoCephList = new CephList<>(CephXTicketInfo.class);
        ticketInfoCephList.setValues(ticketInfoList);
        payload.setTicketInfos(ticketInfoCephList);

        payload.setEncryptedSecret(new CephBytes(ENCRYPTED_SECRET));
        payload.setExtra(new CephBytes(new byte[0]));

        byte[] expectedPayload = new byte[message1Bytes.length - 36];
        System.arraycopy(message1Bytes, 32, expectedPayload, 0, message1Bytes.length - 36);

        ByteBuf byteBuf = Unpooled.buffer();
        authDoneFrame.encodeSegment1(byteBuf, true);
        byte[] actualPayload = new byte[byteBuf.writerIndex()];
        byteBuf.readBytes(actualPayload);
        assertArrayEquals(expectedPayload, actualPayload);
    }
}
