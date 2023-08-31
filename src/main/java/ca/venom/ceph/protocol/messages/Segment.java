package ca.venom.ceph.protocol.messages;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public interface Segment {
    void encode(ByteArrayOutputStream stream) throws IOException;

    void decode(ByteBuffer byteBuffer);

    int getAlignment();
}
