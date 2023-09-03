package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.UInt32;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class AuthRequest extends ControlFrame {
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
    protected int encodeSegmentBody(int segmentIndex, ByteArrayOutputStream outputStream) {
        if (segmentIndex == 0) {
            write(authMethod, outputStream);

            if (preferredModes != null) {
                write(new UInt32(preferredModes.size()), outputStream);
                preferredModes.forEach(pm -> write(pm, outputStream));
            } else {
                outputStream.writeBytes(new byte[4]);
            }

            write(authPayload, outputStream);

            return 8;
        } else {
            return 0;
        }
    }

    @Override
    protected void decodeSegmentBody(int segmentIndex, ByteBuffer byteBuffer, int alignment) {
        if (segmentIndex == 0) {
            authMethod = readUInt32(byteBuffer);
            preferredModes = readList(byteBuffer, UInt32.class);
            authPayload = readByteArray(byteBuffer);
        }
    }

    @Override
    public MessageType getTag() {
        return MessageType.AUTH_REQUEST;
    }
}
