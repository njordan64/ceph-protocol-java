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

import ca.venom.ceph.protocol.CephDecoder;
import ca.venom.ceph.protocol.CephEncoder;
import ca.venom.ceph.protocol.CephProtocolContext;
import ca.venom.ceph.protocol.ControlFrameType;
import ca.venom.ceph.protocol.types.auth.AuthDoneMonPayload;
import ca.venom.ceph.protocol.types.auth.CephXResponseHeader;
import ca.venom.ceph.protocol.types.auth.CephXTicketInfo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class TestAuthDoneFrame {
    private static final String MESSAGE1_PATH = "frames/authdone1.bin";
    private static final byte[] SERVICE_TICKET = Base64.getDecoder().decode("91f6n78UK8Lv9tZ0tY6EdZ/YlITihBSgI6SnVOhGvwUNDTWKiSF4AlNv88v7sy93");
    private static final byte[] TICKET = Base64.getDecoder().decode("AQYAAAAAAAAAYAAAAL4rUVLyUZ6U8uLKn2q2KX1BA2ClpaT4dU57vFvBokLJw8tEHY3RpNAN989DhBRrJsctXotAfx9FjlNRddQcpg+B/FOtWmMzPPzJ2ucidvQKtirpr2ovH/IMcLRHnMUEdQ==");
    private static final byte[] ENCRYPTED_SECRET = Base64.getDecoder().decode("UAAAAIsAjSn+TV0HSZPTB58SJ09igR/NHzbUmrNPYuzOshS/FbTTnjaPEOv+L+5kqYn1OEp8DvxFt3St0kq7leB0X1w2jpyupNNY2CnOnTXinu9Y");

    private byte[] message1Bytes;
    private CephProtocolContext ctx;

    @BeforeEach
    public void setup() throws Exception {
        InputStream inputStream = TestAuthDoneFrame.class.getClassLoader().getResourceAsStream(MESSAGE1_PATH);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] buffer = new byte[4096];
        int bytesRead = inputStream.read(buffer);
        while (bytesRead > -1) {
            outputStream.write(buffer, 0, bytesRead);
            bytesRead = inputStream.read(buffer);
        }

        message1Bytes = outputStream.toByteArray();
        outputStream.close();
        inputStream.close();

        ctx = new CephProtocolContext();
        ctx.setRev1(true);
        ctx.setSecureMode(CephProtocolContext.SecureMode.CRC);
    }

    @Test
    public void testDecodeMessage1() throws Exception {
        AuthDoneFrame parsedMessage = new AuthDoneFrame();
        ByteBuf byteBuf = Unpooled.wrappedBuffer(message1Bytes);
        byteBuf.skipBytes(32);
        parsedMessage.decodeSegment1(byteBuf, true);

        assertEquals(ControlFrameType.AUTH_DONE, parsedMessage.getTag());
        assertEquals(154220L, parsedMessage.getSegment1().getGlobalId());
        assertEquals(2, parsedMessage.getSegment1().getConnectionMode());

        AuthDoneMonPayload payload = CephDecoder.decode(
                Unpooled.wrappedBuffer(parsedMessage.getSegment1().getPayload()),
                true,
                AuthDoneMonPayload.class
        );

        assertEquals(0x100, payload.getResponseHeader().getResponseType());
        assertEquals(0, payload.getResponseHeader().getStatus());
        assertEquals(1, payload.getTicketInfos().size());
        assertEquals(32L, payload.getTicketInfos().get(0).getServiceId());
        assertArrayEquals(SERVICE_TICKET, payload.getTicketInfos().get(0).getServiceTicket());
        assertFalse(payload.getTicketInfos().get(0).isEncrypted());
        assertArrayEquals(TICKET, payload.getTicketInfos().get(0).getTicket());
        assertArrayEquals(ENCRYPTED_SECRET, payload.getEncryptedSecret());
        assertArrayEquals(new byte[0], payload.getExtra());
    }

    @Test
    public void testEncodeMessage1() throws Exception {
        AuthDoneFrame authDoneFrame = new AuthDoneFrame();
        authDoneFrame.setSegment1(new AuthDoneFrame.Segment1());
        authDoneFrame.getSegment1().setGlobalId(154220L);
        authDoneFrame.getSegment1().setConnectionMode(2);

        AuthDoneMonPayload payload = new AuthDoneMonPayload();
        CephXResponseHeader responseHeader = new CephXResponseHeader();
        responseHeader.setResponseType((short) 0x100);
        responseHeader.setStatus(0);
        payload.setResponseHeader(responseHeader);

        CephXTicketInfo ticketInfo = new CephXTicketInfo();
        ticketInfo.setServiceId(32);
        ticketInfo.setServiceTicket(SERVICE_TICKET);
        ticketInfo.setEncrypted(false);
        ticketInfo.setTicket(TICKET);
        List<CephXTicketInfo> ticketInfoList = new ArrayList<>();
        ticketInfoList.add(ticketInfo);
        payload.setTicketInfos(ticketInfoList);

        payload.setEncryptedSecret(ENCRYPTED_SECRET);
        payload.setExtra(new byte[0]);
        ByteBuf payloadByteBuf = Unpooled.buffer();
        CephEncoder.encode(payload, payloadByteBuf, true);

        byte[] payloadBytes = new byte[payloadByteBuf.writerIndex()];
        payloadByteBuf.readBytes(payloadBytes);
        authDoneFrame.getSegment1().setPayload(payloadBytes);

        byte[] expectedPayload = new byte[message1Bytes.length - 36];
        System.arraycopy(message1Bytes, 32, expectedPayload, 0, message1Bytes.length - 36);

        ByteBuf byteBuf = Unpooled.buffer();
        authDoneFrame.encodeSegment1(byteBuf, true);
        byte[] actualPayload = new byte[byteBuf.writerIndex()];
        byteBuf.readBytes(actualPayload);
        assertArrayEquals(expectedPayload, actualPayload);
    }
}
