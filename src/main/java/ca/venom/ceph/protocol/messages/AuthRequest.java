package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.UInt32;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

public class AuthRequest extends ControlFrame {
    public class Segment1 implements Segment {
        @Override
        public int getAlignment() {
            return 8;
        }

        @Override
        public void encode(ByteArrayOutputStream stream) throws IOException {
            write(authMethod, stream);

            if (preferredModes != null) {
                write(new UInt32(preferredModes.size()), stream);
                preferredModes.forEach(pm -> write(pm, stream));
            } else {
                stream.writeBytes(new byte[4]);
            }

            write(authPayload, stream);
        }

        @Override
        public void decode(ByteBuffer byteBuffer) {
            authMethod = readUInt32(byteBuffer);
            preferredModes = readList(byteBuffer, UInt32.class);
            authPayload = readByteArray(byteBuffer);
        }
    }

    private final Segment1 segment1 = new Segment1();

    private UInt32 authMethod;
    private List<UInt32> preferredModes;
    private byte[] authPayload;

    public UInt32 getAuthMethod() {
        return authMethod;
    }

    public void setAuthMethod(UInt32 authMethod) {
        this.authMethod = authMethod;
    }

    public List<UInt32> getPreferredModes() {
        return preferredModes;
    }

    public void setPreferredModes(List<UInt32> preferredModes) {
        this.preferredModes = preferredModes;
    }

    public byte[] getAuthPayload() {
        return authPayload;
    }

    public void setAuthPayload(byte[] authPayload) {
        this.authPayload = authPayload;
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
        return MessageType.AUTH_REQUEST;
    }
}
