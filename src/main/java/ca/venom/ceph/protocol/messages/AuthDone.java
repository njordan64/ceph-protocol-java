package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.UInt32;
import ca.venom.ceph.protocol.types.UInt64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class AuthDone extends ControlFrame {
    public class Segment1 implements Segment {
        @Override
        public int getAlignment() {
            return 8;
        }

        @Override
        public void encode(ByteArrayOutputStream stream) throws IOException {
            write(globalId, stream);
            write(connectionMode, stream);
            write(authMethodPayload, stream);
        }

        @Override
        public void decode(ByteBuffer byteBuffer) {
            globalId = readUInt64(byteBuffer);
            connectionMode = readUInt32(byteBuffer);
            authMethodPayload = readByteArray(byteBuffer);
        }
    }

    private UInt64 globalId;
    private UInt32 connectionMode;
    private byte[] authMethodPayload;
    private Segment1 segment1 = new Segment1();

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
    protected Segment getSegment(int index) {
        return segment1;
    }

    @Override
    public MessageType getTag() {
        return MessageType.AUTH_DONE;
    }
}
