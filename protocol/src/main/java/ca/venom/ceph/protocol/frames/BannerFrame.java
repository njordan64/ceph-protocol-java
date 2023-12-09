package ca.venom.ceph.protocol.frames;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BannerFrame {
    private boolean revision1Supported;
    private boolean compressionSupported;
    private boolean revision1Required;
    private boolean compressionRequired;

    private static final String BANNER_TEXT = "ceph v2\n";
    private static final int EXPECTED_SIZE = 26;

    public BannerFrame() {
    }

    public void encode(OutputStream stream) throws IOException {
        stream.write(BANNER_TEXT.getBytes(), 0, BANNER_TEXT.length());
        stream.write(16);
        stream.write(0);

        byte[] flagBytes = new byte[8];
        flagBytes[0] |= (revision1Supported ? 1 : 0) | (compressionSupported ? 2 : 0);
        stream.write(flagBytes);

        flagBytes[0] = 0;
        flagBytes[0] |= (revision1Required ? 1 : 0) | (compressionRequired ? 2 : 0);
        stream.write(flagBytes);
    }

    public void decode(InputStream stream) throws IOException {
        byte[] bytes = new byte[EXPECTED_SIZE];
        int offset = 0;
        while (offset < EXPECTED_SIZE) {
            int bytesRead = stream.read(bytes, offset, EXPECTED_SIZE - offset);
            if (bytesRead == -1) {
                throw new IOException("Unable to read message");
            }
            offset += bytesRead;
        }

        final byte[] expectedBytes = BANNER_TEXT.getBytes();
        final byte[] bannerTextBytes = new byte[expectedBytes.length];
        System.arraycopy(bytes, 0, bannerTextBytes, 0, expectedBytes.length);

        for (int i = 0; i < expectedBytes.length; i++) {
            if (expectedBytes[i] != bannerTextBytes[i]) {
                throw new IOException("Unknown prefix");
            }
        }

        revision1Supported = (bytes[10] & 0x01) > 0;
        compressionSupported = (bytes[10] & 0x02) > 0;
        revision1Required = (bytes[18] & 0x01) > 0;
        compressionRequired = (bytes[18] & 0x02) > 0;
    }

    public boolean isRevision1Supported() {
        return revision1Supported;
    }

    public void setRevision1Supported(boolean revision1Supported) {
        this.revision1Supported = revision1Supported;
    }

    public boolean isCompressionSupported() {
        return compressionSupported;
    }

    public void setCompressionSupported(boolean compressionSupported) {
        this.compressionSupported = compressionSupported;
    }

    public boolean isRevision1Required() {
        return revision1Required;
    }

    public void setRevision1Required(boolean revision1Required) {
        this.revision1Required = revision1Required;
    }

    public boolean isCompressionRequired() {
        return compressionRequired;
    }

    public void setCompressionRequired(boolean compressionRequired) {
        this.compressionRequired = compressionRequired;
    }
}
