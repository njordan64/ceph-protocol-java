package ca.venom.ceph.encoding.annotations;

import ca.venom.ceph.types.MessageType;

public @interface CephMessagePayload {
    MessageType value();
}
