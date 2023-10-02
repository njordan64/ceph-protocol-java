package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.CephBytes;
import ca.venom.ceph.protocol.types.CephList;
import ca.venom.ceph.protocol.types.UInt32;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class AuthRequest extends ControlFrame {
    private UInt32 authMethod;
    private CephList<UInt32> preferredModes;
    private CephBytes authPayload;

    public UInt32 getAuthMethod() {
        return authMethod;
    }

    public void setAuthMethod(UInt32 authMethod) {
        this.authMethod = authMethod;
    }

    public List<UInt32> getPreferredModes() {
        return preferredModes.getValues();
    }

    public void setPreferredModes(List<UInt32> preferredModes) {
        this.preferredModes = new CephList<>(preferredModes);
    }

    public byte[] getAuthPayload() {
        return authPayload.getValue();
    }

    public void setAuthPayload(byte[] authPayload) {
        this.authPayload = new CephBytes(authPayload);
    }

    @Override
    protected int encodeSegmentBody(int segmentIndex, ByteArrayOutputStream outputStream) {
        if (segmentIndex == 0) {
            authMethod.encode(outputStream);

            if (preferredModes != null) {
                preferredModes.encode(outputStream);
            } else {
                outputStream.writeBytes(new byte[4]);
            }

            authPayload.encode(outputStream);

            return 8;
        } else {
            return 0;
        }
    }

    @Override
    protected void decodeSegmentBody(int segmentIndex, ByteBuffer byteBuffer, int alignment) {
        if (segmentIndex == 0) {
            authMethod = UInt32.read(byteBuffer);
            preferredModes = CephList.read(byteBuffer, UInt32.class);
            authPayload = CephBytes.read(byteBuffer);
        }
    }

    @Override
    public MessageType getTag() {
        return MessageType.AUTH_REQUEST;
    }
}
