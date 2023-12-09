package ca.venom.ceph.client.codecs;

import ca.venom.ceph.client.AttributeKeys;
import ca.venom.ceph.protocol.CephFeatures;
import ca.venom.ceph.protocol.frames.ClientIdentFrame;
import ca.venom.ceph.protocol.frames.ServerIdentFrame;
import ca.venom.ceph.protocol.types.AddrIPv4;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.security.SecureRandom;
import java.util.BitSet;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class ServerIdentHandler extends SimpleChannelInboundHandler<ServerIdentFrame> {
    private static final Logger LOG = LoggerFactory.getLogger(ServerIdentHandler.class);

    private CompletableFuture<Boolean> future;

    public CompletableFuture<Boolean> start(Channel channel) {
        ClientIdentFrame clientIdentFrame = new ClientIdentFrame();
        clientIdentFrame.setSegment1(new ClientIdentFrame.Segment1());
        clientIdentFrame.getSegment1().setGlobalId(-1);
        clientIdentFrame.getSegment1().setGlobalSeq(1);

        SecureRandom random = new SecureRandom();
        clientIdentFrame.getSegment1().setClientCookie(random.nextLong());

        clientIdentFrame.getSegment1().setSupportedFeatures(CephFeatures.ALL);
        BitSet requiredFeatures = new BitSet();
        CephFeatures.MSG_ADDR2.enable(requiredFeatures);
        clientIdentFrame.getSegment1().setRequiredFeatures(requiredFeatures);
        clientIdentFrame.getSegment1().setFlags(new BitSet(64));

        int addrNonce = random.nextInt();
        channel.attr(AttributeKeys.ADDR_NONCE).set(addrNonce);

        AddrIPv4 myAddr = new AddrIPv4();
        myAddr.setNonce(addrNonce);
        InetSocketAddress inetSocketAddress = (InetSocketAddress) channel.localAddress();
        myAddr.setPort((short) inetSocketAddress.getPort());
        myAddr.setAddrBytes(inetSocketAddress.getAddress().getAddress());
        clientIdentFrame.getSegment1().setMyAddresses(Collections.singletonList(myAddr));

        AddrIPv4 targetAddr = new AddrIPv4();
        clientIdentFrame.getSegment1().setTargetAddress(targetAddr);
        targetAddr.setNonce(0);

        inetSocketAddress = (InetSocketAddress) channel.remoteAddress();
        String overrideServerPort = System.getenv("OVERRIDE_SERVER_PORT");
        if (overrideServerPort != null) {
            targetAddr.setPort((short) Integer.parseInt(overrideServerPort));
        } else {
            targetAddr.setPort((short) inetSocketAddress.getPort());
        }
        String overrideServerHost = System.getenv("OVERRIDE_SERVER_HOST");
        if (overrideServerHost != null) {
            String[] ipParts = overrideServerHost.split("\\.");
            byte[] ip = new byte[4];
            for (int i = 0; i < 4; i++) {
                ip[i] = (byte) Integer.parseInt(ipParts[i]);
            }
            targetAddr.setAddrBytes(ip);
        } else {
            targetAddr.setAddrBytes(inetSocketAddress.getAddress().getAddress());
        }

        channel.writeAndFlush(clientIdentFrame);

        future = new CompletableFuture<>();
        return future;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ServerIdentFrame serverIdentFrame) {
        LOG.debug(">>> Received ServerIdent");
        future.complete(true);
    }
}
