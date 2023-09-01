package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.UInt32;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class AuthBadMethod extends ControlFrame {
    private class Segment1 implements Segment {
        @Override
        public int getAlignment() {
            return 8;
        }

        @Override
        public void encode(ByteArrayOutputStream stream) throws IOException {
            write(method, stream);
            write(result, stream);
            write(allowedMethods, stream, UInt32.class);
            write(allowedModes, stream, UInt32.class);
        }

        @Override
        public void decode(ByteBuffer byteBuffer) {
            method = readUInt32(byteBuffer);
            result = readInt(byteBuffer);
            allowedMethods = readList(byteBuffer, UInt32.class);
            allowedModes = readList(byteBuffer, UInt32.class);
        }
    }

    private UInt32 method;
    private int result;
    private List<UInt32> allowedMethods;
    private List<UInt32> allowedModes;
    private Segment1 segment1 = new Segment1();

    public UInt32 getMethod() {
        return method;
    }

    public void setMethod(UInt32 method) {
        this.method = method;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public List<UInt32> getAllowedMethods() {
        return allowedMethods;
    }

    public void setAllowedMethods(List<UInt32> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    public List<UInt32> getAllowedModes() {
        return allowedModes;
    }

    public void setAllowedModes(List<UInt32> allowedModes) {
        this.allowedModes = allowedModes;
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
        return MessageType.AUTH_BAD_METHOD;
    }
}
