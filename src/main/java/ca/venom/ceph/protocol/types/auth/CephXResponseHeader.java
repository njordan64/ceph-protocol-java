package ca.venom.ceph.protocol.types.auth;

import ca.venom.ceph.protocol.types.CephDataType;
import ca.venom.ceph.protocol.types.Int32;
import ca.venom.ceph.protocol.types.UInt16;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class CephXResponseHeader implements CephDataType {
    private UInt16 responseType;
    private Int32 status;

    public CephXResponseHeader(UInt16 responseType, Int32 status) {
        this.responseType = responseType;
        this.status = status;
    }

    public static CephXResponseHeader read(ByteBuffer byteBuffer) {
        UInt16 responseType = UInt16.read(byteBuffer);
        Int32 status = Int32.read(byteBuffer);
        return new CephXResponseHeader(responseType, status);
    }

    public UInt16 getResponseType() {
        return responseType;
    }

    public void setResponseType(UInt16 responseType) {
        this.responseType = responseType;
    }

    public Int32 getStatus() {
        return status;
    }

    public void setStatus(Int32 status) {
        this.status = status;
    }

    @Override
    public int getSize() {
        return 6;
    }

    @Override
    public void encode(ByteArrayOutputStream outputStream) {
        responseType.encode(outputStream);
        status.encode(outputStream);
    }

    @Override
    public void encode(ByteBuffer byteBuffer) {
        responseType.encode(byteBuffer);
        status.encode(byteBuffer);
    }
}
