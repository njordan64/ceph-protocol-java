/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.protocol.frames;

import ca.venom.ceph.protocol.CephFeatures;
import ca.venom.ceph.protocol.CephProtocolContext;
import ca.venom.ceph.protocol.NodeType;
import ca.venom.ceph.protocol.types.CephAddr;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.util.BitSet;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestHelloFrame {
    private static final String MESSAGE1_PATH = "frames/hello1.bin";
    private byte[] message1Bytes;
    private CephProtocolContext ctx;

    @BeforeEach
    public void setup() throws Exception {
        InputStream inputStream = TestHelloFrame.class.getClassLoader().getResourceAsStream(MESSAGE1_PATH);
        message1Bytes = inputStream.readAllBytes();
        inputStream.close();

        ctx = new CephProtocolContext();
        ctx.setRev1(true);
        ctx.setSecureMode(CephProtocolContext.SecureMode.CRC);
    }

    @Test
    public void testDecodeMessage1() throws Exception {
        HelloFrame parsedMessage = new HelloFrame();
        ByteBuf byteBuf = Unpooled.wrappedBuffer(message1Bytes);
        byteBuf.skipBytes(32);
        parsedMessage.decodeSegment1(byteBuf, true, new BitSet(64));

        assertEquals(NodeType.MON, parsedMessage.getSegment1().getNodeType());

        CephAddr addr = parsedMessage.getSegment1().getAddr();
        assertEquals(0, addr.getNonce());

        assertEquals(60832, addr.getPort());
        assertArrayEquals(
                new byte[] {(byte) 192, (byte) 168, (byte) 122, (byte) 227},
                addr.getSocketAddress().getAddress().getAddress()
        );
    }

    @Test
    public void testEncodeMessage1() throws Exception {
        HelloFrame helloFrame = new HelloFrame();
        helloFrame.setSegment1(new HelloFrame.Segment1());

        helloFrame.getSegment1().setNodeType(NodeType.MON);

        CephAddr addr = new CephAddr();
        addr.setNonce(0);
        Inet4Address inet4Address = (Inet4Address) Inet4Address.getByAddress(
                new byte[] {
                        (byte) 192,
                        (byte) 168,
                        (byte) 122,
                        (byte) 227
                }
        );
        addr.setSocketAddress(new InetSocketAddress(inet4Address, 60832));

        helloFrame.getSegment1().setAddr(addr);

        byte[] expectedSegment = new byte[message1Bytes.length - 36];
        System.arraycopy(message1Bytes, 32, expectedSegment, 0, message1Bytes.length - 36);
        ByteBuf byteBuf = Unpooled.buffer();
        BitSet features = new BitSet(64);
        CephFeatures.MSG_ADDR2.enable(features);
        helloFrame.encodeSegment1(byteBuf, true, features);

        byte[] actualSegment = new byte[byteBuf.writerIndex()];
        System.arraycopy(byteBuf.array(), 0, actualSegment, 0, byteBuf.writerIndex());
        assertArrayEquals(expectedSegment, actualSegment);
    }
}
