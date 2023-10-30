package ca.venom.ceph.protocol;

import io.netty.util.AttributeKey;

import java.util.concurrent.CompletableFuture;

public class AttributeKeys {
    public static final AttributeKey<CompletableFuture<Void>> SECURE_MODE_FUTURE =
            AttributeKey.newInstance("Secure-Mode-Future");
}
