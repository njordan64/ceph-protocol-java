package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.NodeType;
import ca.venom.ceph.protocol.HexFunctions;
import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.UInt32;
import ca.venom.ceph.protocol.types.UInt8;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.BitSet;

import static org.junit.Assert.*;

public class TestHelloMessage {
    private static final String MESSAGE1_PATH = "hello1.bin";
    private byte[] message1Bytes;

    @Before
    public void setup() throws Exception {
        InputStream inputStream = TestHelloMessage.class.getClassLoader().getResourceAsStream(MESSAGE1_PATH);
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
        HelloMessage parsedMessage = new HelloMessage();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(message1Bytes, 1, message1Bytes.length - 1);
        parsedMessage.decode(inputStream);

        assertEquals(MessageType.HELLO, parsedMessage.getTag());
        assertEquals(0, parsedMessage.getFlags().cardinality());

        HelloMessage.Segment1 segment1 = (HelloMessage.Segment1) parsedMessage.getSegment1();
        assertEquals(NodeType.MON, segment1.getNodeType());
        assertTrue(segment1.isMsgAddr2());
        assertEquals(2, segment1.getType().getValue());
        assertEquals(0, segment1.getNonce().getValue());

        HelloMessage.AddrIPv4 addr = (HelloMessage.AddrIPv4) segment1.getAddr();
        assertEquals(50504, addr.getPort());
        assertEquals((byte) 192, addr.getAddrBytes()[0]);
        assertEquals((byte) 168, addr.getAddrBytes()[1]);
        assertEquals((byte) 122, addr.getAddrBytes()[2]);
        assertEquals((byte) 227, addr.getAddrBytes()[3]);
    }

    @Test
    public void testEncodeMessage1() throws Exception {
        HelloMessage helloMessage = new HelloMessage();

        HelloMessage.Segment1 segment = (HelloMessage.Segment1) helloMessage.getSegment1();
        segment.setNodeType(NodeType.MON);
        segment.setMsgAddr2(true);

        HelloMessage.AddrIPv4 addr = new HelloMessage.AddrIPv4();
        addr.setPort(50504);
        byte[] addrBytes = new byte[] {(byte) 192, (byte) 168, (byte) 122, (byte) 227};
        addr.setAddrBytes(addrBytes);

        segment.setAddr(addr);
        segment.setType(new UInt32(2));
        segment.setNonce(new UInt32(0));

        assertArrayEquals(message1Bytes, helloMessage.encode());
    }
}
