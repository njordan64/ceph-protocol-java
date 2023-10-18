package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.NodeType;
import ca.venom.ceph.protocol.CephProtocolContext;
import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.AddrIPv4;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestHelloFrame {
    private static final String MESSAGE1_PATH = "hello1.bin";
    private byte[] message1Bytes;
    private CephProtocolContext ctx;

    @Before
    public void setup() throws Exception {
        InputStream inputStream = TestHelloFrame.class.getClassLoader().getResourceAsStream(MESSAGE1_PATH);
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
        HelloFrame parsedMessage = new HelloFrame();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(message1Bytes, 1, message1Bytes.length - 1);
        parsedMessage.decode(inputStream, ctx);

        assertEquals(MessageType.HELLO, parsedMessage.getTag());
        assertEquals(0, parsedMessage.getFlags().cardinality());

        assertEquals(NodeType.MON, parsedMessage.getNodeType());
        assertTrue(parsedMessage.isMsgAddr2());

        AddrIPv4 addr = (AddrIPv4) parsedMessage.getAddr();
        assertEquals(0, addr.getNonce().getValue());
        assertEquals(50504, addr.getPort());
        assertEquals((byte) 192, addr.getAddrBytes()[0]);
        assertEquals((byte) 168, addr.getAddrBytes()[1]);
        assertEquals((byte) 122, addr.getAddrBytes()[2]);
        assertEquals((byte) 227, addr.getAddrBytes()[3]);
    }

    @Test
    public void testEncodeMessage1() throws Exception {
        HelloFrame helloFrame = new HelloFrame();

        helloFrame.setNodeType(NodeType.MON);
        helloFrame.setMsgAddr2(true);

        AddrIPv4 addr = new AddrIPv4();
        addr.setPort(50504);
        byte[] addrBytes = new byte[] {(byte) 192, (byte) 168, (byte) 122, (byte) 227};
        addr.setAddrBytes(addrBytes);

        helloFrame.setAddr(addr);

        assertArrayEquals(message1Bytes, helloFrame.encode(ctx));
    }
}
