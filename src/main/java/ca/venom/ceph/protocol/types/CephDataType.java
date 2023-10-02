package ca.venom.ceph.protocol.types;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public interface CephDataType {
    int getSize();

    void encode(ByteArrayOutputStream outputStream);

    void encode(ByteBuffer byteBuffer);
}
