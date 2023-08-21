package ca.venom.ceph.protocol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.BitSet;

public class Banner implements Payload {
    private final BitSet featuresSupported = new BitSet(64);
    private final BitSet featuresRequired = new BitSet(64);

    private static final String BANNER_TEXT = "ceph v2\n";

    public Banner() {
    }

    public void writeToStream(OutputStream ostream) throws IOException {
        ostream.write(BANNER_TEXT.getBytes(), 0, BANNER_TEXT.length());
        ostream.write(16);
        ostream.write(0);

        for (int i = 0; i < 8; i++) {
            byte b = 0;
            for (int j = 0; j < 8; j++) {
                if (featuresSupported.get(i * 8 + j)) {
                    b += 1 << j;
                }
            }

            ostream.write(b);
        }

        for (int i = 0; i < 8; i++) {
            byte b = 0;
            for (int j = 0; j < 8; j++) {
                if (featuresRequired.get(i * 8 + j)) {
                    b += 1 << j;
                }
            }

            ostream.write(b);
        }
    }

    public void readFromStream(InputStream istream) throws IOException {
        final byte[] expectedBytes = BANNER_TEXT.getBytes();
        final byte[] bannerTextBytes = new byte[expectedBytes.length];
        int bytesRead = istream.read(bannerTextBytes);
        if (bytesRead != expectedBytes.length) {
            throw new IOException("Unknown prefix");
        }

        for (int i = 0; i < expectedBytes.length; i++) {
            if (expectedBytes[i] != bannerTextBytes[i]) {
                throw new IOException("Unknown prefix");
            }
        }

        final byte[] featureBytes = new byte[8];
        bytesRead = istream.read(featureBytes);
        if (bytesRead != 8) {
            throw new IOException("Banner message too short");
        }

        for (int i = 0; i < 64; i++) {
            featuresSupported.set(i, (featureBytes[i / 8] & (1 << (i % 8))) > 0);
        }

        bytesRead = istream.read(featureBytes);
        if (bytesRead != 8) {
            throw new IOException("Banner message too short");
        }

        for (int i = 0; i < 64; i++) {
            featuresRequired.set(i, (featureBytes[i / 8] & (1 << (i % 8))) > 0);
        }
    }

    public BitSet getFeaturesSupported() {
        return featuresSupported;
    }

    public BitSet getFeaturesRequired() {
        return featuresRequired;
    }
}
