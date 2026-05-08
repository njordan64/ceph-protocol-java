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
import ca.venom.ceph.protocol.types.mds.CapItem;
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

public class TestMClientCapRelease {
    private static final String MESSAGE_PATH = "sample_messages/messages/MClientCapRelease/";
    
    private List<MClientCapRelease> expectedMessages;
    private List<byte[]> binaryMessages;
    private CephMsgHeader2 header;

    @BeforeEach
    public void setup() throws Exception {
        expectedMessages = new ArrayList<>();
        binaryMessages = new ArrayList<>();
        
        for (int i = 1; i <= 10; i++) {
            String jsonPath = MESSAGE_PATH + "MClientCapRelease_" + (i < 10 ? "0" + i : "" + i) + ".json";
            String binPath = MESSAGE_PATH + "MClientCapRelease_" + (i < 10 ? "0" + i : "" + i) + ".bin";
            
            InputStream jsonInputStream = TestMClientCapRelease.class.getClassLoader().getResourceAsStream(jsonPath);
            InputStream binInputStream = TestMClientCapRelease.class.getClassLoader().getResourceAsStream(binPath);
            
            ObjectMapper objectMapper = new ObjectMapper();
            MClientCapRelease message = objectMapper.readValue(jsonInputStream, MClientCapRelease.class);
            expectedMessages.add(message);
            
            byte[] binaryData = binInputStream.readAllBytes();
            binaryMessages.add(binaryData);
            
            jsonInputStream.close();
            binInputStream.close();
        }

        header = new CephMsgHeader2();
        header.setType((short) MessageType.MSG_CLIENT_CAPRELEASE.getValueInt());
    }
    
    @Test
    public void testDecodeMessage1() throws Exception {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessages.get(0));
        MClientCapRelease decoded = (MClientCapRelease) CephDecoder.decodeMessagePayload(byteBuf, true, new BitSet(64), header);

        verifyMessage(decoded, expectedMessages.get(0));
    }
    
    @Test
    public void testDecodeMessage2() throws Exception {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessages.get(1));
        MClientCapRelease decoded = (MClientCapRelease) CephDecoder.decodeMessagePayload(byteBuf, true, new BitSet(64), header);
        
        verifyMessage(decoded, expectedMessages.get(1));
    }
    
    @Test
    public void testDecodeMessage3() throws Exception {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessages.get(2));
        MClientCapRelease decoded = (MClientCapRelease) CephDecoder.decodeMessagePayload(byteBuf, true, new BitSet(64), header);
        
        verifyMessage(decoded, expectedMessages.get(2));
    }
    
    @Test
    public void testDecodeMessage4() throws Exception {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessages.get(3));
        MClientCapRelease decoded = (MClientCapRelease) CephDecoder.decodeMessagePayload(byteBuf, true, new BitSet(64), header);
        
        verifyMessage(decoded, expectedMessages.get(3));
    }
    
    @Test
    public void testDecodeMessage5() throws Exception {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessages.get(4));
        MClientCapRelease decoded = (MClientCapRelease) CephDecoder.decodeMessagePayload(byteBuf, true, new BitSet(64), header);
        
        verifyMessage(decoded, expectedMessages.get(4));
    }
    
    @Test
    public void testDecodeMessage6() throws Exception {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessages.get(5));
        MClientCapRelease decoded = (MClientCapRelease) CephDecoder.decodeMessagePayload(byteBuf, true, new BitSet(64), header);
        
        verifyMessage(decoded, expectedMessages.get(5));
    }
    
    @Test
    public void testDecodeMessage7() throws Exception {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessages.get(6));
        MClientCapRelease decoded = (MClientCapRelease) CephDecoder.decodeMessagePayload(byteBuf, true, new BitSet(64), header);
        
        verifyMessage(decoded, expectedMessages.get(6));
    }
    
    @Test
    public void testDecodeMessage8() throws Exception {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessages.get(7));
        MClientCapRelease decoded = (MClientCapRelease) CephDecoder.decodeMessagePayload(byteBuf, true, new BitSet(64), header);
        
        verifyMessage(decoded, expectedMessages.get(7));
    }
    
    @Test
    public void testDecodeMessage9() throws Exception {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessages.get(8));
        MClientCapRelease decoded = (MClientCapRelease) CephDecoder.decodeMessagePayload(byteBuf, true, new BitSet(64), header);
        
        verifyMessage(decoded, expectedMessages.get(8));
    }
    
    @Test
    public void testDecodeMessage10() throws Exception {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessages.get(9));
        MClientCapRelease decoded = (MClientCapRelease) CephDecoder.decodeMessagePayload(byteBuf, true, new BitSet(64), header);
        
        verifyMessage(decoded, expectedMessages.get(9));
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
    
    @Test
    public void testEncodeMessage6() throws Exception {
        testEncode(5);
    }
    
    @Test
    public void testEncodeMessage7() throws Exception {
        testEncode(6);
    }
    
    @Test
    public void testEncodeMessage8() throws Exception {
        testEncode(7);
    }
    
    @Test
    public void testEncodeMessage9() throws Exception {
        testEncode(8);
    }
    
    @Test
    public void testEncodeMessage10() throws Exception {
        testEncode(9);
    }
    
    private void testEncode(int index) throws Exception {
        MClientCapRelease message = expectedMessages.get(index);
        
        ByteBuf byteBuf = Unpooled.buffer();
        CephEncoder.encode(message, byteBuf, true, new BitSet(64));
        
        byte[] encodedBytes = new byte[byteBuf.writerIndex()];
        byteBuf.getBytes(0, encodedBytes);

        assertArrayEquals(binaryMessages.get(index), encodedBytes);
    }
    
    private void verifyMessage(MClientCapRelease actual, MClientCapRelease expected) {
        assertEquals(expected.getCaps(), actual.getCaps());
        assertEquals(expected.getCaps().size(), actual.getCaps().size());
        for (int i = 0; i < expected.getCaps().size(); i++) {
            CapItem expectedCap = expected.getCaps().get(i);
            CapItem actualCap = actual.getCaps().get(i);
            assertEquals(expectedCap.getIno(), actualCap.getIno());
            assertEquals(expectedCap.getCapId(), actualCap.getCapId());
            assertEquals(expectedCap.getMigrateSeq(), actualCap.getMigrateSeq());
            assertEquals(expectedCap.getIssueSeq(), actualCap.getIssueSeq());
        }
        assertEquals(expected.getOsdEpochBarrier(), actual.getOsdEpochBarrier());
    }
}
