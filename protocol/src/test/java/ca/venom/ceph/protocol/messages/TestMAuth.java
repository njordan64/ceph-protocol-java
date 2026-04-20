/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.protocol.CephDecoder;
import ca.venom.ceph.protocol.CephEncoder;
import ca.venom.ceph.types.MessageType;
import ca.venom.ceph.utils.HexFunctions;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestMAuth {
    private static final String MESSAGE_PATH = "sample_messages/messages/MAuth/";
    
    private List<MAuth> expectedMessages;
    private List<byte[]> binaryMessages;
    private CephMsgHeader2 header;

    @BeforeEach
    public void setup() throws Exception {
        expectedMessages = new ArrayList<>();
        binaryMessages = new ArrayList<>();
        
        for (int i = 1; i <= 5; i++) {
            String jsonPath = MESSAGE_PATH + "message_" + i + ".json";
            String binPath = MESSAGE_PATH + "message_" + i + ".bin";
            
            InputStream jsonInputStream = TestMAuth.class.getClassLoader().getResourceAsStream(jsonPath);
            InputStream binInputStream = TestMAuth.class.getClassLoader().getResourceAsStream(binPath);
            
            ObjectMapper objectMapper = new ObjectMapper();
            MAuth message = objectMapper.readValue(jsonInputStream, MAuth.class);
            expectedMessages.add(message);
            
            byte[] binaryData = binInputStream.readAllBytes();
            binaryMessages.add(binaryData);
            
            jsonInputStream.close();
            binInputStream.close();
        }

        header = new CephMsgHeader2();
        header.setType((short) MessageType.MSG_AUTH.getValueInt());
    }
    
    @Test
    public void testDecodeMessage1() throws Exception {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessages.get(0));
        MAuth decoded = (MAuth) CephDecoder.decodeMessagePayload(byteBuf, true, new BitSet(64), header);

        verifyMessage(decoded, expectedMessages.get(0));
    }
    
    @Test
    public void testDecodeMessage2() throws Exception {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessages.get(1));
        MAuth decoded = (MAuth) CephDecoder.decodeMessagePayload(byteBuf, true, new BitSet(64), header);
        
        verifyMessage(decoded, expectedMessages.get(1));
    }
    
    @Test
    public void testDecodeMessage3() throws Exception {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessages.get(2));
        MAuth decoded = (MAuth) CephDecoder.decodeMessagePayload(byteBuf, true, new BitSet(64), header);
        
        verifyMessage(decoded, expectedMessages.get(2));
    }
    
    @Test
    public void testDecodeMessage4() throws Exception {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessages.get(3));
        MAuth decoded = (MAuth) CephDecoder.decodeMessagePayload(byteBuf, true, new BitSet(64), header);
        
        verifyMessage(decoded, expectedMessages.get(3));
    }
    
    @Test
    public void testDecodeMessage5() throws Exception {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessages.get(4));
        MAuth decoded = (MAuth) CephDecoder.decodeMessagePayload(byteBuf, true, new BitSet(64), header);
        
        verifyMessage(decoded, expectedMessages.get(4));
    }
    
    @Test
    public void testEncodeMessage1() throws Exception {
        testEncode(0);
    }
    
    @Test
    public void testEncodeMessage2() throws Exception {
        testEncode(1);
    }
    
    @Test
    public void testEncodeMessage3() throws Exception {
        testEncode(2);
    }
    
    @Test
    public void testEncodeMessage4() throws Exception {
        testEncode(3);
    }
    
    @Test
    public void testEncodeMessage5() throws Exception {
        testEncode(4);
    }
    
    private void testEncode(int index) throws Exception {
        MAuth message = expectedMessages.get(index);
        
        ByteBuf byteBuf = Unpooled.buffer();
        CephEncoder.encode(message, byteBuf, true, new BitSet(64));
        
        byte[] encodedBytes = new byte[byteBuf.writerIndex()];
        byteBuf.getBytes(0, encodedBytes);

        assertArrayEquals(binaryMessages.get(index), encodedBytes);
    }
    
    private void verifyMessage(MAuth actual, MAuth expected) {
        assertEquals(expected.getProtocol(), actual.getProtocol());
        assertArrayEquals(expected.getAuthPayload(), actual.getAuthPayload());
        assertEquals(expected.getMonMapEpoch(), actual.getMonMapEpoch());
    }
}
