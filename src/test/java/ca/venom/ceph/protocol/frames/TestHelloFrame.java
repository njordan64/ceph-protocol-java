package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.NodeType;
import ca.venom.ceph.protocol.CephProtocolContext;
import ca.venom.ceph.protocol.types.Addr;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestHelloFrame {
    private static final String MESSAGE1_PATH = "frames/hello1.bin";
    private byte[] message1Bytes;
    private CephProtocolContext ctx;

    @BeforeEach
    public void setup() throws Exception {
        InputStream inputStream = TestHelloFrame.class.getClassLoader().getResourceAsStream(MESSAGE1_PATH);
        message1Bytes = inputStream.readAllBytes();
        inputStream.close();

        ctx = new CephProtocolContext();
        ctx.setRev1(true);
        ctx.setSecureMode(CephProtocolContext.SecureMode.CRC);
    }

    @Test
    public void testDecodeMessage1() throws Exception {
        HelloFrame parsedMessage = new HelloFrame();
        ByteBuf byteBuf = Unpooled.wrappedBuffer(message1Bytes);
        byteBuf.skipBytes(32);
        parsedMessage.decodeSegment1(byteBuf, true);

        assertEquals(NodeType.MON, parsedMessage.getNodeType());

        Addr addr = parsedMessage.getAddr();
        assertArrayEquals(new byte[4], addr.getNonce());

        Addr.Ipv4Details details = (Addr.Ipv4Details) addr.getAddrDetails();
        assertEquals(60832, details.getPort());
        assertEquals((byte) 192, details.getAddrBytes()[0]);
        assertEquals((byte) 168, details.getAddrBytes()[1]);
        assertEquals((byte) 122, details.getAddrBytes()[2]);
        assertEquals((byte) 227, details.getAddrBytes()[3]);
    }

    @Test
    public void testEncodeMessage1() throws Exception {
        HelloFrame helloFrame = new HelloFrame();

        helloFrame.setNodeType(NodeType.MON);

        Addr addr = new Addr();
        addr.setType(2);
        addr.setNonce(new byte[4]);

        Addr.Ipv4Details details = new Addr.Ipv4Details();
        addr.setAddrDetails(details);
        details.setPort((short) 60832);
        details.setAddrBytes(new byte[] {(byte) 192, (byte) 168, (byte) 122, (byte) 227});

        helloFrame.setAddr(addr);

        byte[] expectedSegment = new byte[message1Bytes.length - 36];
        System.arraycopy(message1Bytes, 32, expectedSegment, 0, message1Bytes.length - 36);
        ByteBuf byteBuf = Unpooled.buffer();
        helloFrame.encodeSegment1(byteBuf, true);

        byte[] actualSegment = new byte[byteBuf.writerIndex()];
        System.arraycopy(byteBuf.array(), 0, actualSegment, 0, byteBuf.writerIndex());
        assertArrayEquals(expectedSegment, actualSegment);
    }
}
