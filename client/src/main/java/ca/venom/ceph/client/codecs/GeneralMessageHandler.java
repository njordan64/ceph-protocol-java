package ca.venom.ceph.client.codecs;

import ca.venom.ceph.protocol.frames.ControlFrame;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GeneralMessageHandler extends MessageToMessageCodec<ControlFrame, RequestWithFuture> {
    private CompletableFuture<ControlFrame> responseFuture;

    @Override
    protected void encode(ChannelHandlerContext ctx,
                          RequestWithFuture requestWithFuture,
                          List<Object> output) throws Exception {
        if (responseFuture != null) {
            throw new IllegalStateException("Request already pending");
        }

        responseFuture = requestWithFuture.getResponseFuture();
        output.add(requestWithFuture.getRequest());
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ControlFrame response, List<Object> output) throws Exception {
        if (responseFuture != null) {
            responseFuture.complete(response);
        } else {
            throw new IllegalStateException("No pending request");
        }
    }
}
