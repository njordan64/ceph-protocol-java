package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.CephList;
import ca.venom.ceph.protocol.types.Int32;
import ca.venom.ceph.protocol.types.UInt32;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class AuthBadMethodFrame extends ControlFrame {
    private UInt32 method;
    private Int32 result;
    private CephList<UInt32> allowedMethods;
    private CephList<UInt32> allowedModes;

    public UInt32 getMethod() {
        return method;
    }

    public void setMethod(UInt32 method) {
        this.method = method;
    }

    public int getResult() {
        return result.getValue();
    }

    public void setResult(int result) {
        this.result = new Int32(result);
    }

    public List<UInt32> getAllowedMethods() {
        return allowedMethods.getValues();
    }

    public void setAllowedMethods(List<UInt32> allowedMethods) {
        this.allowedMethods = new CephList<>(allowedMethods);
    }

    public List<UInt32> getAllowedModes() {
        return allowedModes.getValues();
    }

    public void setAllowedModes(List<UInt32> allowedModes) {
        this.allowedModes = new CephList<>(allowedModes);
    }

    @Override
    protected int encodeSegmentBody(int segmentIndex, ByteArrayOutputStream outputStream) {
        if (segmentIndex == 0) {
            method.encode(outputStream);
            result.encode(outputStream);
            allowedMethods.encode(outputStream);
            allowedModes.encode(outputStream);

            return 8;
        } else {
            return 0;
        }
    }

    @Override
    protected void decodeSegmentBody(int segmentIndex, ByteBuffer byteBuffer, int alignment) {
        if (segmentIndex == 0) {
            method = UInt32.read(byteBuffer);
            result = Int32.read(byteBuffer);
            allowedMethods = CephList.read(byteBuffer, UInt32.class);
            allowedModes = CephList.read(byteBuffer, UInt32.class);
        }
    }

    @Override
    public MessageType getTag() {
        return MessageType.AUTH_BAD_METHOD;
    }
}
