package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.NodeType;
import ca.venom.ceph.protocol.HexFunctions;
import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.AddrIPv4;
import ca.venom.ceph.protocol.types.UInt32;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestHello {
    private static final String MESSAGE1_PATH = "hello1.bin";
    private byte[] message1Bytes;

    @Before
    public void setup() throws Exception {
        InputStream inputStream = TestHello.class.getClassLoader().getResourceAsStream(MESSAGE1_PATH);
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
        Hello parsedMessage = new Hello();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(message1Bytes, 1, message1Bytes.length - 1);
        parsedMessage.decode(inputStream);

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
        Hello hello = new Hello();

        hello.setNodeType(NodeType.MON);
        hello.setMsgAddr2(true);

        AddrIPv4 addr = new AddrIPv4();
        addr.setPort(50504);
        byte[] addrBytes = new byte[] {(byte) 192, (byte) 168, (byte) 122, (byte) 227};
        addr.setAddrBytes(addrBytes);

        hello.setAddr(addr);

        assertArrayEquals(message1Bytes, hello.encode());
    }
}
