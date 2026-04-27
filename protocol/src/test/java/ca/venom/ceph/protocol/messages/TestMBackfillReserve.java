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
import ca.venom.ceph.protocol.CephFeatures;
import ca.venom.ceph.protocol.types.osd.PlacementGroupId;
import ca.venom.ceph.protocol.types.osd.ShardPlacementGroupId;
import ca.venom.ceph.types.MessageType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class TestMBackfillReserve {
    @Getter
    @Setter
    public class MessageDto {
        @JsonProperty("pgid")
        private ShardPlacementGroupIdDto pgid;

        @JsonProperty("queryEpoch")
        private int queryEpoch;

        @JsonProperty("type")
        private int type;

        @JsonProperty("priority")
        private int priority;

        @JsonProperty("primaryNumBytes")
        private long primaryNumBytes;

        @JsonProperty("shardNumBytes")
        private long shardNumBytes;
    }

    private static final String MESSAGE_PATH = "sample_messages/messages/MBackfillReserve/";
    private static final int NUM_MESSAGES = 5;

    private List<MBackfillReserve> expectedMessagesVersion3;
    private List<byte[]> binaryMessagesVersion3;
    private List<MBackfillReserve> expectedMessagesVersion4;
    private List<byte[]> binaryMessagesVersion4;
    private List<MBackfillReserve> expectedMessagesVersion5;
    private List<byte[]> binaryMessagesVersion5;

    @BeforeEach
    public void setup() throws Exception {
        expectedMessagesVersion3 = new ArrayList<>();
        binaryMessagesVersion3 = new ArrayList<>();
        for (int i = 1; i <= NUM_MESSAGES; i++) {
            loadTestData(i, 3, expectedMessagesVersion3, binaryMessagesVersion3);
        }

        expectedMessagesVersion4 = new ArrayList<>();
        binaryMessagesVersion4 = new ArrayList<>();
        for (int i = 1; i <= NUM_MESSAGES; i++) {
            loadTestData(i, 4, expectedMessagesVersion4, binaryMessagesVersion4);
        }

        expectedMessagesVersion5 = new ArrayList<>();
        binaryMessagesVersion5 = new ArrayList<>();
        for (int i = 1; i <= NUM_MESSAGES; i++) {
            loadTestData(i, 5, expectedMessagesVersion5, binaryMessagesVersion5);
        }
    }

    private void loadTestData(int index, int version, List<MBackfillReserve> expectedMessages, List<byte[]> binaryMessages) throws Exception {
        String jsonPath = MESSAGE_PATH + "version_" + version + "/message_" + index + ".json";
        String binPath = MESSAGE_PATH + "version_" + version + "/message_" + index + ".bin";

        ObjectMapper objectMapper = new ObjectMapper();

        InputStream jsonInputStream = TestMBackfillReserve.class.getClassLoader().getResourceAsStream(jsonPath);
        InputStream binInputStream = TestMBackfillReserve.class.getClassLoader().getResourceAsStream(binPath);

        JsonNode jsonNode = objectMapper.readTree(jsonInputStream);
        MessageDto messageDto = objectMapper.treeToValue(jsonNode, MessageDto.class);

        ShardPlacementGroupId shardPgid = new ShardPlacementGroupId();
        PlacementGroupId pgid = new PlacementGroupId();
        pgid.setMPool(messageDto.getPgid().getPgid().getMPool());
        pgid.setMSeed(messageDto.getPgid().getPgid().getMSeed());
        shardPgid.setPgid(pgid);
        shardPgid.setShard(messageDto.getPgid().getShard());

        MBackfillReserve message = new MBackfillReserve();
        message.setPgid(shardPgid);
        message.setQueryEpoch(messageDto.getQueryEpoch());
        message.setType(messageDto.getType());
        message.setPriority(messageDto.getPriority());
        message.setPrimaryNumBytes(messageDto.getPrimaryNumBytes());
        message.setShardNumBytes(messageDto.getShardNumBytes());

        expectedMessages.add(message);

        byte[] binaryData = binInputStream.readAllBytes();
        binaryMessages.add(binaryData);

        jsonInputStream.close();
        binInputStream.close();
    }

    @Test
    public void testDecodeVersion3Message1() throws Exception {
        testDecodeVersion3(0);
    }

    @Test
    public void testDecodeVersion3Message2() throws Exception {
        testDecodeVersion3(1);
    }

    @Test
    public void testDecodeVersion3Message3() throws Exception {
        testDecodeVersion3(2);
    }

    @Test
    public void testDecodeVersion3Message4() throws Exception {
        testDecodeVersion3(3);
    }

    @Test
    public void testDecodeVersion3Message5() throws Exception {
        testDecodeVersion3(4);
    }

    @Test
    public void testDecodeVersion4Message1() throws Exception {
        testDecodeVersion4(0);
    }

    @Test
    public void testDecodeVersion4Message2() throws Exception {
        testDecodeVersion4(1);
    }

    @Test
    public void testDecodeVersion4Message3() throws Exception {
        testDecodeVersion4(2);
    }

    @Test
    public void testDecodeVersion4Message4() throws Exception {
        testDecodeVersion4(3);
    }

    @Test
    public void testDecodeVersion4Message5() throws Exception {
        testDecodeVersion4(4);
    }

    @Test
    public void testDecodeVersion5Message1() throws Exception {
        testDecodeVersion5(0);
    }

    @Test
    public void testDecodeVersion5Message2() throws Exception {
        testDecodeVersion5(1);
    }

    @Test
    public void testDecodeVersion5Message3() throws Exception {
        testDecodeVersion5(2);
    }

    @Test
    public void testDecodeVersion5Message4() throws Exception {
        testDecodeVersion5(3);
    }

    @Test
    public void testDecodeVersion5Message5() throws Exception {
        testDecodeVersion5(4);
    }

    @Test
    public void testEncodeVersion3Message1() throws Exception {
        testEncodeVersion3(0);
    }

    @Test
    public void testEncodeVersion3Message2() throws Exception {
        testEncodeVersion3(1);
    }

    @Test
    public void testEncodeVersion3Message3() throws Exception {
        testEncodeVersion3(2);
    }

    @Test
    public void testEncodeVersion3Message4() throws Exception {
        testEncodeVersion3(3);
    }

    @Test
    public void testEncodeVersion3Message5() throws Exception {
        testEncodeVersion3(4);
    }

    @Test
    public void testEncodeVersion5Message1() throws Exception {
        testEncodeVersion5(0);
    }

    @Test
    public void testEncodeVersion5Message2() throws Exception {
        testEncodeVersion5(1);
    }

    @Test
    public void testEncodeVersion5Message3() throws Exception {
        testEncodeVersion5(2);
    }

    @Test
    public void testEncodeVersion5Message4() throws Exception {
        testEncodeVersion5(3);
    }

    @Test
    public void testEncodeVersion5Message5() throws Exception {
        testEncodeVersion5(4);
    }

    private void testDecodeVersion3(int index) throws Exception {
        final BitSet features = new BitSet(64);
        final CephMsgHeader2 header = new CephMsgHeader2();
        header.setVersion((short) 3);
        header.setCompatVersion((short) 3);
        header.setType((short) MessageType.MSG_OSD_BACKFILL_RESERVE.getValueInt());

        final ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessagesVersion3.get(index));
        final MBackfillReserve decoded = (MBackfillReserve) CephDecoder.decodeMessagePayload(
                byteBuf, true, features, header);

        verifyMessage(decoded, expectedMessagesVersion3.get(index));
    }

    private void testDecodeVersion4(int index) throws Exception {
        final BitSet features = new BitSet(64);
        final CephMsgHeader2 header = new CephMsgHeader2();
        header.setVersion((short) 4);
        header.setCompatVersion((short) 3);
        header.setType((short) MessageType.MSG_OSD_BACKFILL_RESERVE.getValueInt());

        final ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessagesVersion4.get(index));
        final MBackfillReserve decoded = (MBackfillReserve) CephDecoder.decodeMessagePayload(
                byteBuf, true, features, header);

        verifyMessage(decoded, expectedMessagesVersion4.get(index));
    }

    private void testDecodeVersion5(int index) throws Exception {
        final BitSet features = new BitSet(64);
        final CephMsgHeader2 header = new CephMsgHeader2();
        header.setVersion((short) 5);
        header.setCompatVersion((short) 3);
        header.setType((short) MessageType.MSG_OSD_BACKFILL_RESERVE.getValueInt());

        final ByteBuf byteBuf = Unpooled.wrappedBuffer(binaryMessagesVersion5.get(index));
        final MBackfillReserve decoded = (MBackfillReserve) CephDecoder.decodeMessagePayload(
                byteBuf, true, features, header);

        verifyMessage(decoded, expectedMessagesVersion5.get(index));
    }

    private void testEncodeVersion3(int index) throws Exception {
        final MBackfillReserve message = expectedMessagesVersion3.get(index);
        BitSet features = new BitSet(64);

        CephMsgHeader2 header = new CephMsgHeader2();
        header.setType((short) MessageType.MSG_OSD_BACKFILL_RESERVE.getValueInt());

        ByteBuf byteBuf = Unpooled.buffer();
        message.prepareForEncode(null, true, features);

        CephEncoder.encode(message, byteBuf, true, features);

        byte[] encodedBytes = new byte[byteBuf.writerIndex()];
        byteBuf.getBytes(0, encodedBytes);

        assertArrayEquals(binaryMessagesVersion3.get(index), encodedBytes);
    }

    private void testEncodeVersion5(int index) throws Exception {
        final MBackfillReserve message = expectedMessagesVersion5.get(index);
        BitSet features = new BitSet(64);
        CephFeatures.RECOVERY_RESERVATION_2.enable(features);

        CephMsgHeader2 header = new CephMsgHeader2();
        header.setType((short) MessageType.MSG_OSD_BACKFILL_RESERVE.getValueInt());

        ByteBuf byteBuf = Unpooled.buffer();
        message.prepareForEncode(null, true, features);

        CephEncoder.encode(message, byteBuf, true, features);

        byte[] encodedBytes = new byte[byteBuf.writerIndex()];
        byteBuf.getBytes(0, encodedBytes);

        assertArrayEquals(binaryMessagesVersion5.get(index), encodedBytes);
    }

    private void verifyMessage(MBackfillReserve actual, MBackfillReserve expected) {
        assertEquals(expected.getPgid().getPgid().getMPool(), actual.getPgid().getPgid().getMPool());
        assertEquals(expected.getPgid().getPgid().getMSeed(), actual.getPgid().getPgid().getMSeed());
        assertEquals(expected.getPgid().getShard(), actual.getPgid().getShard());
        assertEquals(expected.getQueryEpoch(), actual.getQueryEpoch());
        assertEquals(expected.getType(), actual.getType());
        assertEquals(expected.getPriority(), actual.getPriority());
        assertEquals(expected.getPrimaryNumBytes(), actual.getPrimaryNumBytes());
        assertEquals(expected.getShardNumBytes(), actual.getShardNumBytes());
    }
}
