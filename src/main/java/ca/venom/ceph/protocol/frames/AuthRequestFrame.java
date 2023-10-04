package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.CephList;
import ca.venom.ceph.protocol.types.UInt32;
import ca.venom.ceph.protocol.types.auth.AuthRequestPayload;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class AuthRequestFrame extends ControlFrame {
    private UInt32 authMethod;
    private CephList<UInt32> preferredModes = new CephList<>(new ArrayList<>());
    private AuthRequestPayload payload;

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

    public AuthRequestPayload getPayload() {
        return payload;
    }

    public void setPayload(AuthRequestPayload payload) {
        this.payload = payload;
    }

    @Override
    protected int encodeSegmentBody(int segmentIndex, ByteArrayOutputStream outputStream) {
        if (segmentIndex == 0) {
            authMethod.encode(outputStream);
            preferredModes.encode(outputStream);
            payload.encode(outputStream);

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
            payload = new AuthRequestPayload(byteBuffer);
        }
    }

    @Override
    public MessageType getTag() {
        return MessageType.AUTH_REQUEST;
    }
}
