package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.UInt32;
import ca.venom.ceph.protocol.types.UInt64;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class AuthDone extends ControlFrame {
    private UInt64 globalId;
    private UInt32 connectionMode;
    private byte[] authMethodPayload;

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
        return authMethodPayload;
    }

    public void setAuthMethodPayload(byte[] authMethodPayload) {
        this.authMethodPayload = authMethodPayload;
    }

    @Override
    protected int encodeSegmentBody(int segmentIndex, ByteArrayOutputStream outputStream) {
        if (segmentIndex == 0) {
            write(globalId, outputStream);
            write(connectionMode, outputStream);
            write(authMethodPayload, outputStream);

            return 8;
        } else {
            return 0;
        }
    }

    @Override
    protected void decodeSegmentBody(int segmentIndex, ByteBuffer byteBuffer, int alignment) {
        if (segmentIndex == 0) {
            globalId = readUInt64(byteBuffer);
            connectionMode = readUInt32(byteBuffer);
            authMethodPayload = readByteArray(byteBuffer);
        }
    }

    @Override
    public MessageType getTag() {
        return MessageType.AUTH_DONE;
    }
}
