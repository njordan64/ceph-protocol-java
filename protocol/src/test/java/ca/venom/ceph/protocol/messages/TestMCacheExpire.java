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
import ca.venom.ceph.protocol.types.mds.Dirfrag;
import ca.venom.ceph.protocol.types.mds.Vinodeno;
import ca.venom.ceph.types.MessageType;
import ca.venom.ceph.utils.HexFunctions;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestMCacheExpire {
    private static final String MESSAGE_PATH = "sample_messages/messages/MCacheExpire";
    private static final int NUM_MESSAGES = 20;

    private List<MCacheExpire> expectedMessages;
    private List<byte[]> binaryMessages;
    private CephMsgHeader2 header;

    @BeforeEach
    public void setup() throws Exception {
        expectedMessages = new ArrayList<>();
        binaryMessages = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();

        for (int i = 1; i <= NUM_MESSAGES; i++) {
            String jsonPath = String.format("%s/message_%02d.json", MESSAGE_PATH, i);

            try (InputStream jsonInputStream = TestMCacheExpire.class.getClassLoader().getResourceAsStream(jsonPath)) {
                JsonNode rootNode = objectMapper.readTree(jsonInputStream);

                MCacheExpire message = new MCacheExpire();
                message.setFrom((short) rootNode.get("from").asInt());

                Map<Dirfrag, MCacheExpire.Realm> realms = new LinkedHashMap<>();

                JsonNode realmsNode = rootNode.get("realms");
                if (realmsNode != null && realmsNode.isArray()) {
                    for (JsonNode realmEntry : realmsNode) {
                        Dirfrag dirfragKey = parseDirfrag(realmEntry.get("key"));
                        MCacheExpire.Realm realmValue = parseRealm(realmEntry.get("value"));
                        realms.put(dirfragKey, realmValue);
                    }
                }

                message.setRealms(realms);
                expectedMessages.add(message);
            }
        }

        for (int i = 1; i <= NUM_MESSAGES; i++) {
            String binPath = String.format("%s/message_%02d.bin", MESSAGE_PATH, i);

            try (InputStream binInputStream = TestMCacheExpire.class.getClassLoader().getResourceAsStream(binPath)) {
                byte[] bytes = binInputStream.readAllBytes();
                binaryMessages.add(bytes);
            }
        }

        header = new CephMsgHeader2();
        header.setType((short) MessageType.MSG_MDS_CACHEEXPIRE.getValueInt());
    }

    private Dirfrag parseDirfrag(JsonNode node) {
        Dirfrag d = new Dirfrag();
        d.setIno(node.get("ino").asLong());
        d.setFrag(node.get("frag").asInt());
        return d;
    }

    private Vinodeno parseVinodeno(JsonNode node) {
        Vinodeno v = new Vinodeno();
        v.setIno(node.get("ino").asLong());
        v.setSnapid(node.get("snapid").asLong());
        return v;
    }

    private MCacheExpire.DentryKey parseDentryKey(JsonNode node) {
        MCacheExpire.DentryKey d = new MCacheExpire.DentryKey();
        d.setName(node.get("name").asText());
        d.setSnapId(node.get("snapid").asLong());
        return d;
    }

    private MCacheExpire.Realm parseRealm(JsonNode node) {
        MCacheExpire.Realm realm = new MCacheExpire.Realm();

        if (node.has("inodes")) {
            Map<Vinodeno, Integer> inodes = new LinkedHashMap<>();
            for (JsonNode entry : node.get("inodes")) {
                Vinodeno key = parseVinodeno(entry.get("key"));
                inodes.put(key, entry.get("value").asInt());
            }
            realm.setInodes(inodes);
        }

        if (node.has("dirs")) {
            Map<Dirfrag, Integer> dirs = new LinkedHashMap<>();
            for (JsonNode entry : node.get("dirs")) {
                Dirfrag key = parseDirfrag(entry.get("key"));
                dirs.put(key, entry.get("value").asInt());
            }
            realm.setDirs(dirs);
        }

        if (node.has("dentries")) {
            Map<Dirfrag, Map<MCacheExpire.DentryKey, Integer>> dentriesMap = new LinkedHashMap<>();
            JsonNode dentriesNode = node.get("dentries");
            if (dentriesNode != null && dentriesNode.isArray()) {
                for (JsonNode entry : dentriesNode) {
                    Dirfrag outerKey = parseDirfrag(entry.get("key"));
                    Map<MCacheExpire.DentryKey, Integer> innerMap = new LinkedHashMap<>();
                    JsonNode innerList = entry.get("value");
                    if (innerList != null && innerList.isArray()) {
                        for (JsonNode innerEntry : innerList) {
                            MCacheExpire.DentryKey innerKey = parseDentryKey(innerEntry.get("key"));
                            innerMap.put(innerKey, innerEntry.get("value").asInt());
                        }
                    }
                    dentriesMap.put(outerKey, innerMap);
                }
            }
            realm.setDentries(dentriesMap);
        }

        return realm;
    }

//    @Test
//    public void testEncodeMessage1() throws Exception {
//        testEncode(0);
//    }
//
//    @Test
//    public void testEncodeMessage2() throws Exception {
//        testEncode(1);
//    }
//
//    @Test
//    public void testEncodeMessage3() throws Exception {
//        testEncode(2);
//    }
//
//    @Test
//    public void testEncodeMessage4() throws Exception {
//        testEncode(3);
//    }
//
//    @Test
//    public void testEncodeMessage5() throws Exception {
//        testEncode(4);
//    }
//
//    @Test
//    public void testEncodeMessage6() throws Exception {
//        testEncode(5);
//    }
//
//    @Test
//    public void testEncodeMessage7() throws Exception {
//        testEncode(6);
//    }
//
//    @Test
//    public void testEncodeMessage8() throws Exception {
//        testEncode(7);
//    }
//
//    @Test
//    public void testEncodeMessage9() throws Exception {
//        testEncode(8);
//    }
//
//    @Test
//    public void testEncodeMessage10() throws Exception {
//        testEncode(9);
//    }
//
//    @Test
//    public void testEncodeMessage11() throws Exception {
//        testEncode(10);
//    }
//
//    @Test
//    public void testEncodeMessage12() throws Exception {
//        testEncode(11);
//    }
//
//    @Test
//    public void testEncodeMessage13() throws Exception {
//        testEncode(12);
//    }
//
//    @Test
//    public void testEncodeMessage14() throws Exception {
//        testEncode(13);
//    }
//
//    @Test
//    public void testEncodeMessage15() throws Exception {
//        testEncode(14);
//    }
//
//    @Test
//    public void testEncodeMessage16() throws Exception {
//        testEncode(15);
//    }
//
//    @Test
//    public void testEncodeMessage17() throws Exception {
//        testEncode(16);
//    }
//
//    @Test
//    public void testEncodeMessage18() throws Exception {
//        testEncode(17);
//    }
//
//    @Test
//    public void testEncodeMessage19() throws Exception {
//        testEncode(18);
//    }
//
//    @Test
//    public void testEncodeMessage20() throws Exception {
//        testEncode(19);
//    }

    @Test
    public void testDecodeMessage1() throws Exception {
        testDecode(0);
    }

//    @Test
//    public void testDecodeMessage2() throws Exception {
//        testDecode(1);
//    }
//
//    @Test
//    public void testDecodeMessage3() throws Exception {
//        testDecode(2);
//    }
//
//    @Test
//    public void testDecodeMessage4() throws Exception {
//        testDecode(3);
//    }
//
//    @Test
//    public void testDecodeMessage5() throws Exception {
//        testDecode(4);
//    }
//
//    @Test
//    public void testDecodeMessage6() throws Exception {
//        testDecode(5);
//    }
//
//    @Test
//    public void testDecodeMessage7() throws Exception {
//        testDecode(6);
//    }
//
//    @Test
//    public void testDecodeMessage8() throws Exception {
//        testDecode(7);
//    }
//
//    @Test
//    public void testDecodeMessage9() throws Exception {
//        testDecode(8);
//    }
//
//    @Test
//    public void testDecodeMessage10() throws Exception {
//        testDecode(9);
//    }
//
//    @Test
//    public void testDecodeMessage11() throws Exception {
//        testDecode(10);
//    }
//
//    @Test
//    public void testDecodeMessage12() throws Exception {
//        testDecode(11);
//    }
//
//    @Test
//    public void testDecodeMessage13() throws Exception {
//        testDecode(12);
//    }
//
//    @Test
//    public void testDecodeMessage14() throws Exception {
//        testDecode(13);
//    }
//
//    @Test
//    public void testDecodeMessage15() throws Exception {
//        testDecode(14);
//    }
//
//    @Test
//    public void testDecodeMessage16() throws Exception {
//        testDecode(15);
//    }
//
//    @Test
//    public void testDecodeMessage17() throws Exception {
//        testDecode(16);
//    }
//
//    @Test
//    public void testDecodeMessage18() throws Exception {
//        testDecode(17);
//    }
//
//    @Test
//    public void testDecodeMessage19() throws Exception {
//        testDecode(18);
//    }
//
//    @Test
//    public void testDecodeMessage20() throws Exception {
//        testDecode(19);
//    }

    private void testEncode(int index) throws Exception {
        MCacheExpire message = expectedMessages.get(index);
        byte[] expectedBytes = binaryMessages.get(index);

        ByteBuf byteBuf = Unpooled.buffer();
        CephEncoder.encode(message, byteBuf, true, new BitSet(64));

        byte[] encodedBytes = new byte[byteBuf.writerIndex()];
        byteBuf.getBytes(0, encodedBytes);

        assertEquals(expectedBytes.length, encodedBytes.length,
                "Encoded length mismatch for message " + (index + 1));
    }

    private void testDecode(int index) throws Exception {
        byte[] encodedBytes = binaryMessages.get(index);

        ByteBuf byteBuf = Unpooled.wrappedBuffer(encodedBytes);
        MCacheExpire decoded = (MCacheExpire) CephDecoder.decodeMessagePayload(byteBuf, true, new BitSet(64), header);

        verifyMessage(decoded, expectedMessages.get(index));
    }

    private void verifyMessage(MCacheExpire actual, MCacheExpire expected) {
        assertEquals(expected.getFrom(), actual.getFrom());
        assertEquals(expected.getRealms().keySet(), actual.getRealms().keySet());

        for (Map.Entry<Dirfrag, MCacheExpire.Realm> entry : expected.getRealms().entrySet()) {
            Dirfrag key = entry.getKey();
            MCacheExpire.Realm expectedRealm = entry.getValue();
            MCacheExpire.Realm actualRealm = actual.getRealms().get(key);

            assertEquals(expectedRealm.getInodes().keySet(), actualRealm.getInodes().keySet(),
                    "Inode keys mismatch for realm " + key);
            for (Map.Entry<Vinodeno, Integer> inodeEntry : expectedRealm.getInodes().entrySet()) {
                assertEquals(inodeEntry.getValue(), actualRealm.getInodes().get(inodeEntry.getKey()),
                        "Inode value mismatch for vinodeno " + inodeEntry.getKey());
            }

            assertEquals(expectedRealm.getDirs().keySet(), actualRealm.getDirs().keySet(),
                    "Dir keys mismatch for realm " + key);
            for (Map.Entry<Dirfrag, Integer> dirEntry : expectedRealm.getDirs().entrySet()) {
                assertEquals(dirEntry.getValue(), actualRealm.getDirs().get(dirEntry.getKey()),
                        "Dir value mismatch for dirfrag " + dirEntry.getKey());
            }

            assertEquals(expectedRealm.getDentries().keySet(), actualRealm.getDentries().keySet(),
                    "Dentry outer keys mismatch for realm " + key);
            for (Map.Entry<Dirfrag, Map<MCacheExpire.DentryKey, Integer>> dentryOuterEntry : expectedRealm.getDentries().entrySet()) {
                Dirfrag outerKey = dentryOuterEntry.getKey();
                Map<MCacheExpire.DentryKey, Integer> expectedInner = dentryOuterEntry.getValue();
                Map<MCacheExpire.DentryKey, Integer> actualInner = actualRealm.getDentries().get(outerKey);

                assertEquals(expectedInner.keySet(), actualInner.keySet(),
                        "Dentry inner keys mismatch for dirfrag " + outerKey + " in realm " + key);
                for (Map.Entry<MCacheExpire.DentryKey, Integer> innerEntry : expectedInner.entrySet()) {
                    assertEquals(innerEntry.getValue(), actualInner.get(innerEntry.getKey()),
                            "Dentry value mismatch for dentryKey " + innerEntry.getKey());
                }
            }
        }
    }
}
