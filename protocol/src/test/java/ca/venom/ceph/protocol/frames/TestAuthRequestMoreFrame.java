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

import ca.venom.ceph.protocol.CephProtocolContext;
import ca.venom.ceph.protocol.types.auth.AuthRequestMoreMonPayload;
import ca.venom.ceph.protocol.types.auth.CephXAuthenticate;
import ca.venom.ceph.protocol.types.auth.CephXRequestHeader;
import ca.venom.ceph.protocol.types.auth.CephXTicketBlob;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestAuthRequestMoreFrame {
    private static final String MESSAGE1_PATH = "frames/authrequestmore1.bin";
    private byte[] message1Bytes;
    private CephProtocolContext ctx;

    @BeforeEach
    public void setup() throws Exception {
        InputStream inputStream = TestAuthRequestMoreFrame.class.getClassLoader().getResourceAsStream(MESSAGE1_PATH);
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
        AuthRequestMoreFrame parsedMessage = new AuthRequestMoreFrame();
        ByteBuf byteBuf = Unpooled.wrappedBuffer(message1Bytes);
        byteBuf.skipBytes(32);
        parsedMessage.decodeSegment1(byteBuf, true);

        AuthRequestMoreMonPayload payload = (AuthRequestMoreMonPayload) parsedMessage.getPayload();
        assertEquals(0x100, payload.getRequestHeader().getRequestType());
        byte[] clientChallenge = new byte[] {
                (byte) 0x27, (byte) 0x44, (byte) 0x58, (byte) 0xbc,
                (byte) 0xa0, (byte) 0x12, (byte) 0x94, (byte) 0xae
        };
        assertArrayEquals(clientChallenge, payload.getAuthenticate().getClientChallenge());
        byte[] keyBytes = new byte[] {
                (byte) 0x03, (byte) 0x23, (byte) 0xeb, (byte) 0xc7,
                (byte) 0x3a, (byte) 0xca, (byte) 0xe9, (byte) 0x17
        };
        assertArrayEquals(keyBytes, payload.getAuthenticate().getKey());
        assertEquals(0L, payload.getAuthenticate().getOldTicket().getSecretId());
        assertArrayEquals(new byte[0], payload.getAuthenticate().getOldTicket().getBlob());
        assertEquals(32L, payload.getAuthenticate().getOtherKeys());
    }

    @Test
    public void testEncodeMessage1() throws Exception {
        AuthRequestMoreFrame authRequest = new AuthRequestMoreFrame();
        AuthRequestMoreMonPayload payload = new AuthRequestMoreMonPayload();
        authRequest.setPayload(payload);
        CephXRequestHeader header = new CephXRequestHeader();
        header.setRequestType((short) 0x100);
        payload.setRequestHeader(header);

        byte[] clientChallenge = new byte[] {
                (byte) 0x27, (byte) 0x44, (byte) 0x58, (byte) 0xbc,
                (byte) 0xa0, (byte) 0x12, (byte) 0x94, (byte) 0xae
        };
        byte[] keyBytes = new byte[] {
                (byte) 0x03, (byte) 0x23, (byte) 0xeb, (byte) 0xc7,
                (byte) 0x3a, (byte) 0xca, (byte) 0xe9, (byte) 0x17
        };
        CephXTicketBlob ticketBlob = new CephXTicketBlob();
        ticketBlob.setSecretId(0L);
        ticketBlob.setBlob(new byte[0]);
        CephXAuthenticate authenticate = new CephXAuthenticate();
        payload.setAuthenticate(authenticate);
        authenticate.setClientChallenge(clientChallenge);
        authenticate.setKey(keyBytes);
        authenticate.setOldTicket(ticketBlob);
        authenticate.setOtherKeys(32);


        byte[] expectedSegment = new byte[message1Bytes.length - 36];
        System.arraycopy(message1Bytes, 32, expectedSegment, 0, message1Bytes.length - 36);

        ByteBuf byteBuf = Unpooled.buffer();
        authRequest.encodeSegment1(byteBuf, true);
        byte[] actualBytes = new byte[byteBuf.writerIndex()];
        byteBuf.readBytes(actualBytes);

        assertArrayEquals(expectedSegment, actualBytes);
    }
}
