package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.CephProtocolContext;
import ca.venom.ceph.protocol.NodeType;
import ca.venom.ceph.protocol.types.AddrIPv4;
import ca.venom.ceph.utils.HexFunctions;
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

        assertEquals(NodeType.MON, parsedMessage.getSegment1().getNodeType());

        AddrIPv4 addr = (AddrIPv4) parsedMessage.getSegment1().getAddr();
        assertEquals(0, addr.getNonce());

        assertEquals(60832, addr.getPort() & 0xffff);
        assertArrayEquals(
                new byte[] {(byte) 192, (byte) 168, (byte) 122, (byte) 227},
                addr.getAddrBytes()
        );
        assertEquals((byte) 192, addr.getAddrBytes()[0]);
    }

    @Test
    public void testEncodeMessage1() throws Exception {
        HelloFrame helloFrame = new HelloFrame();
        helloFrame.setSegment1(new HelloFrame.Segment1());

        helloFrame.getSegment1().setNodeType(NodeType.MON);

        AddrIPv4 addr = new AddrIPv4();
        addr.setNonce(0);

        addr.setPort((short) 60832);
        addr.setAddrBytes(new byte[] {(byte) 192, (byte) 168, (byte) 122, (byte) 227});

        helloFrame.getSegment1().setAddr(addr);

        byte[] expectedSegment = new byte[message1Bytes.length - 36];
        System.arraycopy(message1Bytes, 32, expectedSegment, 0, message1Bytes.length - 36);
        ByteBuf byteBuf = Unpooled.buffer();
        helloFrame.encodeSegment1(byteBuf, true);

        byte[] actualSegment = new byte[byteBuf.writerIndex()];
        System.arraycopy(byteBuf.array(), 0, actualSegment, 0, byteBuf.writerIndex());
        assertArrayEquals(expectedSegment, actualSegment);
    }
}
