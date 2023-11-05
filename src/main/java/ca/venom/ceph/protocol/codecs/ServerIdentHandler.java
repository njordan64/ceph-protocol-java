package ca.venom.ceph.protocol.codecs;

import ca.venom.ceph.protocol.AttributeKeys;
import ca.venom.ceph.protocol.CephFeatures;
import ca.venom.ceph.protocol.frames.ClientIdentFrame;
import ca.venom.ceph.protocol.frames.ServerIdentFrame;
import ca.venom.ceph.protocol.types.Addr;
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
        clientIdentFrame.setGlobalId(-1);
        clientIdentFrame.setGlobalSeq(1);

        SecureRandom random = new SecureRandom();
        clientIdentFrame.setClientCookie(random.nextLong());

        clientIdentFrame.setSupportedFeatures(CephFeatures.ALL);
        BitSet requiredFeatures = new BitSet();
        CephFeatures.MSG_ADDR2.enable(requiredFeatures);
        clientIdentFrame.setRequiredFeatures(requiredFeatures);
        clientIdentFrame.setFlags(new BitSet(64));

        byte[] addrNonce = new byte[4];
        random.nextBytes(addrNonce);
        channel.attr(AttributeKeys.ADDR_NONCE).set(addrNonce);

        Addr myAddr = new Addr();
        myAddr.setNonce(addrNonce);
        myAddr.setType(2);
        Addr.Ipv4Details details = new Addr.Ipv4Details();
        myAddr.setAddrDetails(details);
        InetSocketAddress inetSocketAddress = (InetSocketAddress) channel.localAddress();
        details.setPort(inetSocketAddress.getPort());
        details.setAddrBytes(inetSocketAddress.getAddress().getAddress());
        clientIdentFrame.setMyAddresses(Collections.singletonList(myAddr));

        Addr targetAddr = new Addr();
        clientIdentFrame.setTargetAddress(targetAddr);
        targetAddr.setNonce(new byte[4]);
        targetAddr.setType(2);
        details = new Addr.Ipv4Details();
        targetAddr.setAddrDetails(details);

        inetSocketAddress = (InetSocketAddress) channel.remoteAddress();
        String overrideServerPort = System.getenv("OVERRIDE_SERVER_PORT");
        if (overrideServerPort != null) {
            details.setPort(Integer.parseInt(overrideServerPort));
        } else {
            details.setPort(inetSocketAddress.getPort());
        }
        String overrideServerHost = System.getenv("OVERRIDE_SERVER_HOST");
        if (overrideServerHost != null) {
            String[] ipParts = overrideServerHost.split("\\.");
            byte[] ip = new byte[4];
            for (int i = 0; i < 4; i++) {
                ip[i] = (byte) Integer.parseInt(ipParts[i]);
            }
            details.setAddrBytes(ip);
        } else {
            details.setAddrBytes(inetSocketAddress.getAddress().getAddress());
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
