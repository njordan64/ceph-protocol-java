package ca.venom.ceph.protocol;

import io.netty.util.AttributeKey;

public class AttributeKeys {
    public static final AttributeKey<byte[]> ADDR_NONCE =
            AttributeKey.newInstance("Addr-Nonce");
}