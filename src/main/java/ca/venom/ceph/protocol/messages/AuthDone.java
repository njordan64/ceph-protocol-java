package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.CephBytes;
import ca.venom.ceph.protocol.types.UInt32;
import ca.venom.ceph.protocol.types.UInt64;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class AuthDone extends ControlFrame {
    private UInt64 globalId;
    private UInt32 connectionMode;
    private CephBytes authMethodPayload;

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

    public byte[] getAuthMethodPayload() {
        return authMethodPayload.getValue();
    }

    public void setAuthMethodPayload(byte[] authMethodPayload) {
        this.authMethodPayload = new CephBytes(authMethodPayload);
    }

    @Override
    protected int encodeSegmentBody(int segmentIndex, ByteArrayOutputStream outputStream) {
        if (segmentIndex == 0) {
            globalId.encode(outputStream);
            connectionMode.encode(outputStream);
            authMethodPayload.encode(outputStream);

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
            authMethodPayload = CephBytes.read(byteBuffer);
        }
    }

    @Override
    public MessageType getTag() {
        return MessageType.AUTH_DONE;
    }
}
