package ca.venom.ceph.protocol.types.auth;

import ca.venom.ceph.protocol.types.CephDataType;
import ca.venom.ceph.protocol.types.UInt16;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class CephXRequestHeader implements CephDataType {
    private UInt16 requestType;

    public CephXRequestHeader(UInt16 requestType) {
        this.requestType = requestType;
    }

    public static CephXRequestHeader read(ByteBuffer byteBuffer) {
        return new CephXRequestHeader(UInt16.read(byteBuffer));
    }

    public UInt16 getRequestType() {
        return requestType;
    }

    public void setRequestType(UInt16 requestType) {
        this.requestType = requestType;
    }

    @Override
    public int getSize() {
        return 2;
    }

    @Override
    public void encode(ByteArrayOutputStream outputStream) {
        requestType.encode(outputStream);
    }

    @Override
    public void encode(ByteBuffer byteBuffer) {
        requestType.encode(byteBuffer);
    }
}
