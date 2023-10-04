package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.CephBytes;
import ca.venom.ceph.protocol.types.UInt32;
import ca.venom.ceph.protocol.types.UInt64;
import ca.venom.ceph.protocol.types.auth.AuthDonePayload;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class AuthDoneFrame extends ControlFrame {
    private UInt64 globalId;
    private UInt32 connectionMode;
    private AuthDonePayload payload;

    public UInt64 getGlobalId() {
        return globalId;
    }

    public void setGlobalId(UInt64 globalId) {
        this.globalId = globalId;
    }

    public UInt32 getConnectionMode() {
        return connectionMode;
    }

    public void setConnectionMode(UInt32 connectionMode) {
        this.connectionMode = connectionMode;
    }

    public AuthDonePayload getPayload() {
        return payload;
    }

    public void setPayload(AuthDonePayload payload) {
        this.payload = payload;
    }

    @Override
    protected int encodeSegmentBody(int segmentIndex, ByteArrayOutputStream outputStream) {
        if (segmentIndex == 0) {
            globalId.encode(outputStream);
            connectionMode.encode(outputStream);
            payload.encode(outputStream);

            return 8;
        } else {
            return 0;
        }
    }

    @Override
    protected void decodeSegmentBody(int segmentIndex, ByteBuffer byteBuffer, int alignment) {
        if (segmentIndex == 0) {
            globalId = UInt64.read(byteBuffer);
            connectionMode = UInt32.read(byteBuffer);
            payload = new AuthDonePayload(byteBuffer);
        }
    }

    @Override
    public MessageType getTag() {
        return MessageType.AUTH_DONE;
    }
}
