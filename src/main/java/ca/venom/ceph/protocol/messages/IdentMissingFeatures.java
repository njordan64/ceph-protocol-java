package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.UInt64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class IdentMissingFeatures extends ControlFrame {
    public class Segment1 implements Segment {
        @Override
        public int getAlignment() {
            return 8;
        }

        @Override
        public void encode(ByteArrayOutputStream stream) throws IOException {
            write(missingFeaturesMask, stream);
        }

        @Override
        public void decode(ByteBuffer byteBuffer) {
            missingFeaturesMask = readUInt64(byteBuffer);
        }
    }

    private UInt64 missingFeaturesMask;
    private Segment1 segment1;

    public UInt64 getMissingFeaturesMask() {
        return missingFeaturesMask;
    }

    public void setMissingFeaturesMask(UInt64 missingFeaturesMask) {
        this.missingFeaturesMask = missingFeaturesMask;
    }

    @Override
    protected Segment getSegment(int index) {
        if (index == 0) {
            return segment1;
        } else {
            return null;
        }
    }

    @Override
    public MessageType getTag() {
        return MessageType.IDENT_MISSING_FEATURES;
    }
}
