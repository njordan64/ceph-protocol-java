package ca.venom.ceph.client.codecs;

import ca.venom.ceph.protocol.frames.ControlFrame;
import lombok.Getter;

import java.util.concurrent.CompletableFuture;

public class RequestWithFuture {
    @Getter
    private final ControlFrame request;
    @Getter
    private final CompletableFuture<ControlFrame> responseFuture;

    public RequestWithFuture(ControlFrame request, CompletableFuture<ControlFrame> responseFuture) {
        this.request = request;
        this.responseFuture = responseFuture;
    }
}
