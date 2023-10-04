package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.Addr;
import ca.venom.ceph.protocol.types.CephList;
import ca.venom.ceph.protocol.types.UInt64;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class ReconnectFrame extends ControlFrame {
    private CephList<Addr> myAddresses;
    private UInt64 clientCookie;
    private UInt64 serverCookie;
    private UInt64 globalSeq;
    private UInt64 connectSeq;
    private UInt64 messageSeq;

    public List<Addr> getMyAddresses() {
        return myAddresses.getValues();
    }

    public void setMyAddresses(List<Addr> myAddresses) {
        this.myAddresses = new CephList<>(myAddresses);
    }

    public UInt64 getClientCookie() {
        return clientCookie;
    }

    public void setClientCookie(UInt64 clientCookie) {
        this.clientCookie = clientCookie;
    }

    public UInt64 getServerCookie() {
        return serverCookie;
    }

    public void setServerCookie(UInt64 serverCookie) {
        this.serverCookie = serverCookie;
    }

    public UInt64 getGlobalSeq() {
        return globalSeq;
    }

    public void setGlobalSeq(UInt64 globalSeq) {
        this.globalSeq = globalSeq;
    }

    public UInt64 getConnectSeq() {
        return connectSeq;
    }

    public void setConnectSeq(UInt64 connectSeq) {
        this.connectSeq = connectSeq;
    }

    public UInt64 getMessageSeq() {
        return messageSeq;
    }

    public void setMessageSeq(UInt64 messageSeq) {
        this.messageSeq = messageSeq;
    }

    @Override
    public MessageType getTag() {
        return MessageType.SESSION_RECONNECT;
    }

    @Override
    protected int encodeSegmentBody(int index, ByteArrayOutputStream outputStream) {
        if (index == 0) {
            myAddresses.encode(outputStream);
            clientCookie.encode(outputStream);
            serverCookie.encode(outputStream);
            globalSeq.encode(outputStream);
            connectSeq.encode(outputStream);
            messageSeq.encode(outputStream);

            return 8;
        } else {
            return 0;
        }
    }

    @Override
    protected void decodeSegmentBody(int index, ByteBuffer byteBuffer, int alignment) {
        if (index == 0) {
            myAddresses = CephList.read(byteBuffer, Addr.class);
            clientCookie = UInt64.read(byteBuffer);
            serverCookie = UInt64.read(byteBuffer);
            globalSeq = UInt64.read(byteBuffer);
            connectSeq = UInt64.read(byteBuffer);
            messageSeq = UInt64.read(byteBuffer);
        }
    }
}
