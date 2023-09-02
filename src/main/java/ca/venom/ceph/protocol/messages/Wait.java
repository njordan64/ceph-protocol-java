package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.MessageType;

public class Wait extends ControlFrame {
    @Override
    protected Segment getSegment(int index) {
        return null;
    }

    @Override
    public MessageType getTag() {
        return MessageType.WAIT;
    }
}
