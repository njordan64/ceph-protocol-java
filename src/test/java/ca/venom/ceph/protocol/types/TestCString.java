package ca.venom.ceph.protocol.types;

import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.*;

public class TestCString {
    @Test
    public void testEmptyString() {
        runStringTest("");
    }

    @Test
    public void testShortString() {
        runStringTest("hello");
    }

    @Test
    public void testLongerString() {
        runStringTest("asdfasdlkfhasdkfhasdkfjh askdjfhaskdfhaskdf hsadkjfskadjfaskdfhaskjdhaskdj fsakjdgakdsjfgaskdjha");
    }

    private void runStringTest(String value) {
        byte[] stringBytes = value.getBytes();
        byte[] bytes = new byte[stringBytes.length + 1];
        System.arraycopy(stringBytes, 0, bytes, 0, stringBytes.length);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);

        CString cString = new CString(byteBuffer);
        assertEquals(value.length() + 1, byteBuffer.position());

        byteBuffer.flip();
        for (int i = 0; i < value.length(); i++) {
            assertEquals(value.codePointAt(i), byteBuffer.get());
        }
        assertEquals(0, byteBuffer.get());
    }

    @Test
    public void testEncodeEmptyString() {
        runEncodeTest("");
    }

    @Test
    public void testEncodeShortString() {
        runEncodeTest("hello");
    }

    @Test
    public void testEncodeLongerString() {
        runEncodeTest("asdfasdlkfhasdkfhasdkfjh askdjfhaskdfhaskdf hsadkjfskadjfaskdfhaskjdhaskdj fsakjdgakdsjfgaskdjha");
    }

    private void runEncodeTest(String value) {
        CString cString = CString.fromValue(value);
        byte[] bytes = new byte[value.length() + 10];
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);

        cString.encode(byteBuffer);
        assertEquals(value.length() + 1, byteBuffer.position());

        byteBuffer.flip();
        for (int i = 0; i < value.length(); i++) {
            assertEquals(value.codePointAt(i), byteBuffer.get());
        }
        assertEquals(0, byteBuffer.get());
    }
}
