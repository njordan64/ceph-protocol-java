package ca.venom.ceph.protocol;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

public interface Payload {
    void writeToStream(OutputStream ostream) throws IOException;

    void readFromStream(InputStream istream) throws IOException;
}
