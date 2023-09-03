package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.UInt32;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class AuthBadMethod extends ControlFrame {
    private UInt32 method;
    private int result;
    private List<UInt32> allowedMethods;
    private List<UInt32> allowedModes;

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
    protected int encodeSegmentBody(int segmentIndex, ByteArrayOutputStream outputStream) {
        if (segmentIndex == 0) {
            write(method, outputStream);
            write(result, outputStream);
            write(allowedMethods, outputStream, UInt32.class);
            write(allowedModes, outputStream, UInt32.class);

            return 8;
        } else {
            return 0;
        }
    }

    @Override
    protected void decodeSegmentBody(int segmentIndex, ByteBuffer byteBuffer, int alignment) {
        if (segmentIndex == 0) {
            method = readUInt32(byteBuffer);
            result = readInt(byteBuffer);
            allowedMethods = readList(byteBuffer, UInt32.class);
            allowedModes = readList(byteBuffer, UInt32.class);
        }
    }

    @Override
    public MessageType getTag() {
        return MessageType.AUTH_BAD_METHOD;
    }
}
