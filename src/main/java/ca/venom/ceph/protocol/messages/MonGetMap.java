package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.frames.MessageFrame;
import ca.venom.ceph.protocol.types.CephMap;
import ca.venom.ceph.protocol.types.CephString;
import ca.venom.ceph.protocol.types.MonSubscribeItem;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.HashMap;
import java.util.Map;

public class MonGetMap extends MessageBase {
    private CephMap<CephString, MonSubscribeItem> what;
    private CephString hostname;

    @Override
    protected MessageType getType() {
        return MessageType.CEPH_MSG_MON_GET_MAP;
    }

    public Map<String, MonSubscribeItem> getWhat() {
        Map<String, MonSubscribeItem> simplified = new HashMap<>();
        for (Map.Entry<CephString, MonSubscribeItem> entry : what.getMap().entrySet()) {
            simplified.put(entry.getKey().getValue(), entry.getValue());
        }

        return simplified;
    }

    public void setWhat(Map<String, MonSubscribeItem> what) {
        this.what = new CephMap<>(CephString.class, MonSubscribeItem.class);
        Map<CephString, MonSubscribeItem> stored = new HashMap<>();
        this.what.setMap(stored);

        for (Map.Entry<String, MonSubscribeItem> entry : what.entrySet()) {
            stored.put(new CephString(entry.getKey()), entry.getValue());
        }
    }

    public String getHostname() {
        return hostname.getValue();
    }

    public void setHostname(String hostname) {
        this.hostname = new CephString(hostname);
    }

    @Override
    protected void encodePayload(MessageFrame messageFrame) {
        ByteBuf byteBuf = Unpooled.buffer();
        what.encode(byteBuf, true);
        hostname.encode(byteBuf, true);

        messageFrame.setFront(byteBuf);
        messageFrame.setFrontLE(true);
    }

    @Override
    protected void decodePayload(MessageFrame messageFrame) {
        ByteBuf byteBuf = messageFrame.getFront();
        boolean le = messageFrame.isFrontLE();

        what = new CephMap<>(CephString.class, MonSubscribeItem.class);
        what.decode(byteBuf, le);
        hostname = new CephString();
        hostname.decode(byteBuf, le);
    }
}
