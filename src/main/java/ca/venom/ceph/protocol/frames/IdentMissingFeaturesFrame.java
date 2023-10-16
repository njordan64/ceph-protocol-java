package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.UInt64;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class IdentMissingFeaturesFrame extends ControlFrame {
    private UInt64 missingFeaturesMask;

    public UInt64 getMissingFeaturesMask() {
        return missingFeaturesMask;
    }

    public void setMissingFeaturesMask(UInt64 missingFeaturesMask) {
        this.missingFeaturesMask = missingFeaturesMask;
    }

    @Override
    protected int encodeSegmentBody(int segmentIndex, ByteArrayOutputStream outputStream) {
        if (segmentIndex == 0) {
            missingFeaturesMask.encode(outputStream);
            return 8;
        } else {
            return 0;
        }
    }

    @Override
    protected void decodeSegmentBody(int segmentIndex, ByteBuffer byteBuffer, int alignment) {
        if (segmentIndex == 0) {
            missingFeaturesMask = UInt64.read(byteBuffer);
        }
    }

    @Override
    public MessageType getTag() {
        return MessageType.IDENT_MISSING_FEATURES;
    }
}