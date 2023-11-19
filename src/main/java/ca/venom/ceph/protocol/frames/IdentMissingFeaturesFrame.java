package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.ControlFrameType;
import ca.venom.ceph.protocol.types.Int64;
import io.netty.buffer.ByteBuf;

public class IdentMissingFeaturesFrame extends ControlFrame {
    private Int64 missingFeaturesMask;

    public Int64 getMissingFeaturesMask() {
        return missingFeaturesMask;
    }

    public void setMissingFeaturesMask(Int64 missingFeaturesMask) {
        this.missingFeaturesMask = missingFeaturesMask;
    }

    @Override
    public void encodeSegment1(ByteBuf byteBuf, boolean le) {
        missingFeaturesMask.encode(byteBuf, le);
    }

    @Override
    public void decodeSegment1(ByteBuf byteBuf, boolean le) {
        missingFeaturesMask = new Int64();
        missingFeaturesMask.decode(byteBuf, le);
    }

    @Override
    public ControlFrameType getTag() {
        return ControlFrameType.IDENT_MISSING_FEATURES;
    }
}
