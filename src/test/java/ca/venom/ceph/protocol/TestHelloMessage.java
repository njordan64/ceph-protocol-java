package ca.venom.ceph.protocol;

import org.junit.Before;
import org.junit.Test;

import ca.venom.ceph.protocol.types.UInt32;
import ca.venom.ceph.protocol.types.UInt8;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collections;

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
        ByteArrayInputStream inputStream = new ByteArrayInputStream(message1Bytes);
        parsedMessage.decode(inputStream);

        assertEquals(1, parsedMessage.getTag().getValue());
        assertEquals(0, parsedMessage.getFlags().getValue());

        assertEquals(1, parsedMessage.getSegments().size());
        HelloMessage.Segment segment = parsedMessage.getSegments().get(0);
        assertEquals(1, segment.getNodeType().getValue());
        assertTrue(segment.isMsgAddr2());
        assertEquals(2, segment.getType().getValue());
        assertEquals(0, segment.getNonce().getValue());

        HelloMessage.AddrIPv4 addr = (HelloMessage.AddrIPv4) segment.getAddr();
        assertEquals(50504, addr.getPort());
        assertEquals((byte) 192, addr.getAddrBytes()[0]);
        assertEquals((byte) 168, addr.getAddrBytes()[1]);
        assertEquals((byte) 122, addr.getAddrBytes()[2]);
        assertEquals((byte) 227, addr.getAddrBytes()[3]);
    }

    @Test
    public void testEncodeMessage1() throws Exception {
        HelloMessage helloMessage = new HelloMessage();
        helloMessage.setTag(UInt8.fromValue(1));
        helloMessage.setFlags(UInt8.fromValue(0));

        HelloMessage.Segment segment = new HelloMessage.Segment();
        segment.setNodeType(UInt8.fromValue(1));
        segment.setMsgAddr2(true);
        
        HelloMessage.AddrIPv4 addr = new HelloMessage.AddrIPv4();
        addr.setPort(50504);
        byte[] addrBytes = new byte[] {(byte) 192, (byte) 168, (byte) 122, (byte) 227};
        addr.setAddrBytes(addrBytes);
        
        segment.setAddr(addr);
        segment.setType(UInt32.fromValue(2));
        segment.setNonce(UInt32.fromValue(0));
        helloMessage.setSegments(Collections.singletonList(segment));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        helloMessage.encode(baos);
        assertArrayEquals(message1Bytes, baos.toByteArray());
    }
}
