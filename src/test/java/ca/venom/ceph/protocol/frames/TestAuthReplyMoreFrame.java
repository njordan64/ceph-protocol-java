package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.CephProtocolContext;
import ca.venom.ceph.protocol.types.EncodingException;
import ca.venom.ceph.protocol.types.auth.AuthReplyMorePayload;
import ca.venom.ceph.protocol.types.auth.CephXServerChallenge;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class TestAuthReplyMoreFrame {
    private static final String MESSAGE1_PATH = "frames/authreplymore1.bin";
    private byte[] message1Bytes;
    private CephProtocolContext ctx;

    @BeforeEach
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

        ctx = new CephProtocolContext();
        ctx.setRev1(true);
        ctx.setSecureMode(CephProtocolContext.SecureMode.CRC);
    }

    @Test
    public void testDecodeMessage1() throws Exception {
        AuthReplyMoreFrame parsedMessage = new AuthReplyMoreFrame();
        ByteBuf byteBuf = Unpooled.wrappedBuffer(message1Bytes);
        byteBuf.skipBytes(32);
        parsedMessage.decodeSegment1(byteBuf, true);

        byte[] serverChallenge = new byte[] {
                (byte) 0x2b, (byte) 0x33, (byte) 0x2f, (byte) 0x91,
                (byte) 0xd0, (byte) 0x47, (byte) 0xbc, (byte) 0xad
        };
        assertArrayEquals(serverChallenge, parsedMessage.getPayload().getServerChallenge().getServerChallenge());
    }

    @Test
    public void testEncodeMessage1() throws EncodingException {
        AuthReplyMoreFrame authReplyMore = new AuthReplyMoreFrame();
        AuthReplyMorePayload payload = new AuthReplyMorePayload();
        authReplyMore.setPayload(payload);
        byte[] serverChallengeBytes = new byte[] {
                (byte) 0x2b, (byte) 0x33, (byte) 0x2f, (byte) 0x91,
                (byte) 0xd0, (byte) 0x47, (byte) 0xbc, (byte) 0xad
        };
        CephXServerChallenge serverChallenge = new CephXServerChallenge();
        serverChallenge.setServerChallenge(serverChallengeBytes);
        payload.setServerChallenge(serverChallenge);

        byte[] expectedSegment = new byte[message1Bytes.length - 36];
        System.arraycopy(message1Bytes, 32, expectedSegment, 0, message1Bytes.length - 36);

        ByteBuf byteBuf = Unpooled.buffer();
        authReplyMore.encodeSegment1(byteBuf, true);
        byte[] actualSegment = new byte[byteBuf.writerIndex()];
        byteBuf.readBytes(actualSegment, 0, byteBuf.writerIndex());
        assertArrayEquals(expectedSegment, actualSegment);
    }
}
