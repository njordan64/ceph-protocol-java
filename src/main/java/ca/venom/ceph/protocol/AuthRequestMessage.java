package ca.venom.ceph.protocol;

import ca.venom.ceph.protocol.types.UInt8;
import ca.venom.ceph.protocol.types.UInt32;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class AuthRequestMessage extends MessageBase {
    private UInt32 authMethod;
    private List<UInt32> preferredModes;
    private byte[] authPayload;

    @Override
    protected UInt8 getTag() {
        return MessageType.AUTH_REQUEST.getTagNum();
    }

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
    protected SectionMetadata[] getSectionMetadatas() {
        return new SectionMetadata[] {
            new SectionMetadata(12 + 4 * preferredModes.size() + authPayload.length, 8),
            new SectionMetadata(0, 0),
            new SectionMetadata(0, 0),
            new SectionMetadata(0, 0)
        };
    }

    @Override
    protected void encodeSection(int section, ByteBuffer byteBuffer) throws IOException {
        if (section > 0) {
            return;
        }

        authMethod.encode(byteBuffer);
        UInt32.fromValue(preferredModes.size()).encode(byteBuffer);
        for (UInt32 preferredMode : preferredModes) {
            preferredMode.encode(byteBuffer);
        }

        UInt32.fromValue(authPayload.length).encode(byteBuffer);
        byteBuffer.put(authPayload);
    }

    protected void decodeSection(int section, ByteBuffer byteBuffer) throws IOException {
        if (section > 0) {
            return;
        }

        authMethod = new UInt32(byteBuffer);
        int numValues = (int) new UInt32(byteBuffer).getValue();

        preferredModes = new ArrayList<>();
        for (int i = 0; i < numValues; i++) {
            preferredModes.add(new UInt32(byteBuffer));
        }

        int authPayloadSize = (int) new UInt32(byteBuffer).getValue();
        authPayload = new byte[authPayloadSize];
        byteBuffer.get(authPayload);
    }
}