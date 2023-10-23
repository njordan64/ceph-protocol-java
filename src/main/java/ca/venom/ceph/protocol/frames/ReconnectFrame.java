package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.MessageType;
import ca.venom.ceph.protocol.types.Addr;
import ca.venom.ceph.protocol.types.CephList;
import ca.venom.ceph.protocol.types.Int64;
import io.netty.buffer.ByteBuf;

import java.util.List;

public class ReconnectFrame extends ControlFrame {
    private CephList<Addr> myAddresses;
    private Int64 clientCookie;
    private Int64 serverCookie;
    private Int64 globalSeq;
    private Int64 connectSeq;
    private Int64 messageSeq;

    public List<Addr> getMyAddresses() {
        return myAddresses.getValues();
    }

    public void setMyAddresses(List<Addr> myAddresses) {
        this.myAddresses = new CephList<>(myAddresses, Addr.class);
    }

    public Int64 getClientCookie() {
        return clientCookie;
    }

    public void setClientCookie(Int64 clientCookie) {
        this.clientCookie = clientCookie;
    }

    public Int64 getServerCookie() {
        return serverCookie;
    }

    public void setServerCookie(Int64 serverCookie) {
        this.serverCookie = serverCookie;
    }

    public Int64 getGlobalSeq() {
        return globalSeq;
    }

    public void setGlobalSeq(Int64 globalSeq) {
        this.globalSeq = globalSeq;
    }

    public Int64 getConnectSeq() {
        return connectSeq;
    }

    public void setConnectSeq(Int64 connectSeq) {
        this.connectSeq = connectSeq;
    }

    public Int64 getMessageSeq() {
        return messageSeq;
    }

    public void setMessageSeq(Int64 messageSeq) {
        this.messageSeq = messageSeq;
    }

    @Override
    public void encodeSegment1(ByteBuf byteBuf, boolean le) {
        myAddresses.encode(byteBuf, le);
        clientCookie.encode(byteBuf, le);
        serverCookie.encode(byteBuf, le);
        globalSeq.encode(byteBuf, le);
        connectSeq.encode(byteBuf, le);
        messageSeq.encode(byteBuf, le);
    }

    @Override
    public void decodeSegment1(ByteBuf byteBuf, boolean le) {
        myAddresses = new CephList<>(Addr.class);
        myAddresses.decode(byteBuf, le);

        clientCookie = new Int64();
        clientCookie.decode(byteBuf, le);

        serverCookie = new Int64();
        serverCookie.decode(byteBuf, le);

        globalSeq = new Int64();
        globalSeq.decode(byteBuf, le);

        connectSeq = new Int64();
        connectSeq.decode(byteBuf, le);

        messageSeq = new Int64();
        messageSeq.decode(byteBuf, le);
    }

    @Override
    public MessageType getTag() {
        return MessageType.SESSION_RECONNECT;
    }
}
