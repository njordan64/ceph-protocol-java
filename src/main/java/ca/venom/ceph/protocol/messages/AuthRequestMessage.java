package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.UInt32;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class AuthRequestMessage extends MessageBase {
    public class Segment1 implements Segment {
        @Override
        public int getAlignment() {
            return 8;
        }

        @Override
        public void encode(ByteArrayOutputStream stream) throws IOException {
            authMethod.encode(stream);

            if (preferredModes != null) {
                new UInt32(preferredModes.size()).encode(stream);
                preferredModes.forEach(pm -> pm.encode(stream));
            } else {
                stream.writeBytes(new byte[4]);
            }

            new UInt32(authPayload.length).encode(stream);
            stream.write(authPayload);
        }

        @Override
        public void decode(ByteBuffer byteBuffer) {
            authMethod = UInt32.read(byteBuffer);

            int preferredModesCount = (int) UInt32.read(byteBuffer).getValue();
            preferredModes = new ArrayList<>(preferredModesCount);
            for (int i = 0; i < preferredModesCount; i++) {
                preferredModes.add(UInt32.read(byteBuffer));
            }

            int authPayloadSize = (int) UInt32.read(byteBuffer).getValue();
            authPayload = new byte[authPayloadSize];
            byteBuffer.get(authPayload);
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
    protected Segment getSegment1() {
        return segment1;
    }

    @Override
    public MessageType getTag() {
        return MessageType.AUTH_REQUEST;
    }
}
