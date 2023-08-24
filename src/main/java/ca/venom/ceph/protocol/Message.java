package ca.venom.ceph.protocol;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

public interface Message {
    void decode(InputStream stream) throws IOException;

    void encode(OutputStream stream) throws IOException;
}
