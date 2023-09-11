package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.Addr;
import ca.venom.ceph.protocol.types.UInt64;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class Reconnect extends ControlFrame {
    private List<Addr> myAddresses;
    private UInt64 clientCookie;
    private UInt64 serverCookie;
    private UInt64 globalSeq;
    private UInt64 connectSeq;
    private UInt64 messageSeq;

    public List<Addr> getMyAddresses() {
        return myAddresses;
    }

    public void setMyAddresses(List<Addr> myAddresses) {
        this.myAddresses = myAddresses;
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
            write(myAddresses, outputStream, Addr.class);
            write(clientCookie, outputStream);
            write(serverCookie, outputStream);
            write(globalSeq, outputStream);
            write(connectSeq, outputStream);
            write(messageSeq, outputStream);

            return 8;
        } else {
            return 0;
        }
    }

    @Override
    protected void decodeSegmentBody(int index, ByteBuffer byteBuffer, int alignment) {
        if (index == 0) {
            myAddresses = readList(byteBuffer, Addr.class);
            clientCookie = readUInt64(byteBuffer);
            serverCookie = readUInt64(byteBuffer);
            globalSeq = readUInt64(byteBuffer);
            connectSeq = readUInt64(byteBuffer);
            messageSeq = readUInt64(byteBuffer);
        }
    }
}
