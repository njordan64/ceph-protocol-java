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

import ca.venom.ceph.encoding.annotations.*;
import ca.venom.ceph.protocol.CephDecoder;
import ca.venom.ceph.protocol.CephEncoder;
import ca.venom.ceph.protocol.CephFeatures;
import ca.venom.ceph.protocol.DecodingException;
import ca.venom.ceph.protocol.types.CephFileLayout;
import ca.venom.ceph.protocol.types.FileLayout;
import ca.venom.ceph.protocol.types.UTime;
import ca.venom.ceph.protocol.types.mds.CapPeer;
import ca.venom.ceph.protocol.types.mds.CapsBody;
import ca.venom.ceph.protocol.types.mds.CapsHead;
import ca.venom.ceph.protocol.types.mds.CapsNonExportBody;
import ca.venom.ceph.types.MessageType;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

import java.util.BitSet;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/v17.2.6/src/messages/MClientCaps.h#L22
 */
@CephType
@CephMessagePayload(MessageType.MSG_CLIENT_CAPS)
public class MClientCaps extends MessagePayload {
    @Getter
    @Setter
    @CephField
    private CapsHead head;

    @Getter
    @Setter
    private long size;

    @Getter
    @Setter
    private long maxSize;

    @Getter
    @Setter
    private long truncateSize;

    @Getter
    @Setter
    private int truncateSeq;

    @Getter
    @Setter
    private UTime mtime;

    @Getter
    @Setter
    private UTime atime;

    @Getter
    @Setter
    private UTime ctime;

    @Getter
    @Setter
    private FileLayout layout;

    @Getter
    @Setter
    private int timeWarpSeq;

    @Getter
    @Setter
    @CephField(order = 3, sizeProperty = "getHead().getSnapTraceLen()")
    private byte[] snap;

    @Getter
    @Setter
    private byte[] xattr;

    @Getter
    @Setter
    private byte[] flock;

    @Getter
    @Setter
    private CapPeer peer;

    @Getter
    @Setter
    private long inlineVersion = 0;

    @Getter
    @Setter
    private byte[] inlineData;

    @Getter
    @Setter
    private int osdEpochBarrier;

    @Getter
    @Setter
    private long oldestFlushTid;

    @Getter
    @Setter
    private int callerUid;

    @Getter
    @Setter
    private int callerGid;

    @Getter
    @Setter
    private UTime bTime;

    @Getter
    @Setter
    private long changeAttr = 0;

    @Getter
    @Setter
    private int flags = 0;

    @Getter
    @Setter
    @CephField(order = 14)
    private long nFiles = -1;

    @Getter
    @Setter
    private long nSubDirs = -1;

    @Getter
    @Setter
    private byte[] fscryptAuth;

    @Getter
    @Setter
    private byte[] fscryptFile;

    @Getter
    @Setter
    private long subVolumeId;

    private short headerVersion;

    @Override
    public short getHeadVersion(BitSet features) {
        if (!CephFeatures.FLOCK.isEnabled(features)) {
            return 1;
        } else if (!CephFeatures.EXPORT_PEER.isEnabled(features)) {
            return 2;
        }

        return  13;
    }

    @Override
    public short getHeadCompatVersion(BitSet features) {
        return 1;
    }

    @Override
    public void prepareForEncode() {
        head.setSnapTraceLen(snap.length);
        head.setXattrLen(xattr.length);
    }

    @Override
    public void prepareForDecode(CephMsgHeader2 header) {
        headerVersion = header.getVersion();
    }

    @Override
    public void encodeMiddle(ByteBuf byteBuf, boolean le, BitSet features) {
        byteBuf.writeBytes(xattr);
    }

    @Override
    public void decodeMiddle(ByteBuf byteBuf, boolean le, BitSet features) {
        if (head.getXattrLen() > 0) {
            xattr = new byte[head.getXattrLen()];
            byteBuf.readBytes(xattr);
        }
    }

    @CephFieldEncode(order = 2)
    public void encodeBody(ByteBuf byteBuf, boolean le, BitSet features) {
        if (head.getOp() == CapsHead.OpValue.EXPORT) {
            CephEncoder.encode(peer, byteBuf, le, features);
            byteBuf.writeZero(63);
            return;
        }

        CephEncoder.encode(size, byteBuf, le);
        CephEncoder.encode(maxSize, byteBuf, le);
        CephEncoder.encode(truncateSize, byteBuf, le);
        CephEncoder.encode(truncateSeq, byteBuf, le);
        CephEncoder.encode(mtime, byteBuf, le, features);
        CephEncoder.encode(atime, byteBuf, le, features);
        CephEncoder.encode(ctime, byteBuf, le, features);
        CephFileLayout cephFileLayout = new CephFileLayout();
        layout.toLegacy(cephFileLayout);
        CephEncoder.encode(cephFileLayout, byteBuf, le, features);
        CephEncoder.encode(timeWarpSeq, byteBuf, le);
    }

    @CephFieldDecode(order = 2)
    public void decodeBody(ByteBuf byteBuf, boolean le, BitSet features) {
        if (head.getOp() == CapsHead.OpValue.EXPORT) {
            try {
                peer = CephDecoder.decode(byteBuf, le, features, CapPeer.class);
                byteBuf.readerIndex(byteBuf.readerIndex() + 63);
            } catch (DecodingException de) {
                throw new RuntimeException(de);
            }
            return;
        }

        try {
            size = CephDecoder.decodeLong(byteBuf, le);
            maxSize = CephDecoder.decodeLong(byteBuf, le);
            truncateSize = CephDecoder.decodeLong(byteBuf, le);
            truncateSeq = CephDecoder.decodeInt(byteBuf, le);
            mtime = CephDecoder.decode(byteBuf, le, features, UTime.class);
            atime = CephDecoder.decode(byteBuf, le, features, UTime.class);
            ctime = CephDecoder.decode(byteBuf, le, features, UTime.class);
            CephFileLayout cephFileLayout = CephDecoder.decode(byteBuf, le, features, CephFileLayout.class);
            layout = new FileLayout();
            layout.fromLegacy(cephFileLayout);
            timeWarpSeq = CephDecoder.decodeInt(byteBuf, le);
        } catch (DecodingException de) {
            throw new RuntimeException(de);
        }
    }

    @CephFieldEncode(order = 3)
    public void encodeFlock(ByteBuf byteBuf, boolean le, BitSet features) {
        if (CephFeatures.FLOCK.isEnabled(features)) {
            CephEncoder.encode(flock.length, byteBuf, le);
            byteBuf.writeBytes(flock);
        }
    }

    @CephFieldDecode(order = 3)
    public void decodeFlock(ByteBuf byteBuf, boolean le, BitSet features) {
        if (headerVersion >= 2) {
            int length = CephDecoder.decodeInt(byteBuf, le);
            flock = new byte[length];
            byteBuf.readBytes(flock);
        }
    }

    @CephFieldEncode(order = 4)
    public void encodePeer(ByteBuf byteBuf, boolean le, BitSet features) {
        if (CephFeatures.EXPORT_PEER.isEnabled(features) &&
                CephFeatures.FLOCK.isEnabled(features) &&
                head.getOp() == CapsHead.OpValue.IMPORT) {
            CephEncoder.encode(peer, byteBuf, le, features);
        }
    }

    @CephFieldDecode(order = 4)
    public void decodePeer(ByteBuf byteBuf, boolean le, BitSet features) {
        if (headerVersion >= 3 && head.getOp() == CapsHead.OpValue.IMPORT) {
            try {
                peer = CephDecoder.decode(byteBuf, le, features, CapPeer.class);
            } catch (DecodingException de) {
                throw new RuntimeException(de);
            }
        }
    }

    @CephFieldDecode(order = 5)
    public void decodeInlineVersion(ByteBuf byteBuf, boolean le, BitSet features) {
        if (headerVersion >= 4) {
            inlineVersion = CephDecoder.decodeLong(byteBuf, le);
        } else {
            inlineVersion = -1;
        }
    }

    @CephFieldEncode(order = 6)
    public void encodeInlineData(ByteBuf byteBuf, boolean le, BitSet features) {
        if (CephFeatures.MDS_INLINE_DATA.isEnabled(features) &&
                CephFeatures.FLOCK.isEnabled(features) &&
                CephFeatures.EXPORT_PEER.isEnabled(features) &&
                inlineData != null) {
            CephEncoder.encode(inlineData.length, byteBuf, le);
            byteBuf.writeBytes(inlineData);
        } else {
            byteBuf.writeZero(4);
        }
    }

    @CephFieldDecode(order = 6)
    public void decodeInlineData(ByteBuf byteBuf, boolean le, BitSet features) {
        if (headerVersion >= 4) {
            int length = CephDecoder.decodeInt(byteBuf, le);
            inlineData = new byte[length];
            byteBuf.readBytes(inlineData);
        }
    }

    @CephFieldEncode(order = 7)
    public void encodeOsdEpochBarrier(ByteBuf byteBuf, boolean le, BitSet features) {
        if (CephFeatures.FLOCK.isEnabled(features) &&
                CephFeatures.EXPORT_PEER.isEnabled(features)) {
            CephEncoder.encode(osdEpochBarrier, byteBuf, le);
        }
    }

    @CephFieldDecode(order = 7)
    public void decodeOsdEpochBarrier(ByteBuf byteBuf, boolean le, BitSet features) {
        if (headerVersion >= 5) {
            osdEpochBarrier = CephDecoder.decodeInt(byteBuf, le);
        }
    }

    @CephFieldEncode(order = 8)
    public void encodeOldestFlushTid(ByteBuf byteBuf, boolean le, BitSet features) {
        if (CephFeatures.FLOCK.isEnabled(features) &&
                CephFeatures.EXPORT_PEER.isEnabled(features)) {
            CephEncoder.encode(oldestFlushTid, byteBuf, le);
        }
    }

    @CephFieldDecode(order = 8)
    public void decodeOldestFlushTid(ByteBuf byteBuf, boolean le, BitSet features) {
        if (headerVersion >= 6) {
            oldestFlushTid = CephDecoder.decodeInt(byteBuf, le);
        }
    }

    @CephFieldEncode(order = 9)
    public void encodeCallerUid(ByteBuf byteBuf, boolean le, BitSet features) {
        if (CephFeatures.FLOCK.isEnabled(features) &&
                CephFeatures.EXPORT_PEER.isEnabled(features)) {
            CephEncoder.encode(callerUid, byteBuf, le);
        }
    }

    @CephFieldDecode(order = 9)
    public void decodeCallerUid(ByteBuf byteBuf, boolean le, BitSet features) {
        if (headerVersion >= 7) {
            callerUid = CephDecoder.decodeInt(byteBuf, le);
        }
    }

    @CephFieldEncode(order = 10)
    public void encodeCallerGid(ByteBuf byteBuf, boolean le, BitSet features) {
        if (CephFeatures.FLOCK.isEnabled(features) &&
                CephFeatures.EXPORT_PEER.isEnabled(features)) {
            CephEncoder.encode(callerGid, byteBuf, le);
        }
    }

    @CephFieldDecode(order = 10)
    public void decodeCallerGid(ByteBuf byteBuf, boolean le, BitSet features) {
        if (headerVersion >= 7) {
            callerGid = CephDecoder.decodeInt(byteBuf, le);
        }
    }

    @CephFieldEncode(order = 11)
    public void encodePoolNs(ByteBuf byteBuf, boolean le, BitSet features) {
        if (layout != null && CephFeatures.FLOCK.isEnabled(features) &&
                CephFeatures.EXPORT_PEER.isEnabled(features)) {
            CephEncoder.encode(layout.getPoolNs(), byteBuf, le);
        }
    }

    @CephFieldDecode(order = 11)
    public void decodePoolNs(ByteBuf byteBuf, boolean le, BitSet features) {
        if (headerVersion >= 8) {
            layout.setPoolNs(CephDecoder.decodeString(byteBuf, le));
        }
    }

    @CephFieldEncode(order = 12)
    public void encodeBTime(ByteBuf byteBuf, boolean le, BitSet features) {
        if (CephFeatures.FLOCK.isEnabled(features) &&
                CephFeatures.EXPORT_PEER.isEnabled(features)) {
            CephEncoder.encode(bTime, byteBuf, le, features);
        }
    }

    @CephFieldDecode(order = 12)
    public void decodeBTime(ByteBuf byteBuf, boolean le, BitSet features) {
        if (headerVersion >= 9) {
            try {
                bTime = CephDecoder.decode(byteBuf, le, features, UTime.class);
            } catch (DecodingException de) {
                throw new RuntimeException(de);
            }
        }
    }

    @CephFieldEncode(order = 13)
    public void encodeChangeAttr(ByteBuf byteBuf, boolean le, BitSet features) {
        if (CephFeatures.FLOCK.isEnabled(features) &&
                CephFeatures.EXPORT_PEER.isEnabled(features)) {
            CephEncoder.encode(changeAttr, byteBuf, le);
        }
    }

    @CephFieldDecode(order = 13)
    public void decodeChangeAttr(ByteBuf byteBuf, boolean le, BitSet features) {
        if (headerVersion >= 9) {
            changeAttr = CephDecoder.decodeLong(byteBuf, le);
        }
    }

    @CephFieldEncode(order = 14)
    public void encodeFlags(ByteBuf byteBuf, boolean le, BitSet features) {
        if (CephFeatures.FLOCK.isEnabled(features) &&
                CephFeatures.EXPORT_PEER.isEnabled(features)) {
            CephEncoder.encode(flags, byteBuf, le);
        }
    }

    @CephFieldDecode(order = 14)
    public void decodeFlags(ByteBuf byteBuf, boolean le, BitSet features) {
        if (headerVersion >= 10) {
            flags = CephDecoder.decodeInt(byteBuf, le);
        }
    }

    @CephFieldEncode(order = 15)
    public void encodeNFiles(ByteBuf byteBuf, boolean le, BitSet features) {
        if (CephFeatures.FLOCK.isEnabled(features) &&
                CephFeatures.EXPORT_PEER.isEnabled(features)) {
            CephEncoder.encode(nFiles, byteBuf, le);
        }
    }

    @CephFieldDecode(order = 15)
    public void decodeNFiles(ByteBuf byteBuf, boolean le, BitSet features) {
        if (headerVersion >= 11) {
            nFiles = CephDecoder.decodeLong(byteBuf, le);
        }
    }

    @CephFieldEncode(order = 16)
    public void encodeNSubDirs(ByteBuf byteBuf, boolean le, BitSet features) {
        if (CephFeatures.FLOCK.isEnabled(features) &&
                CephFeatures.EXPORT_PEER.isEnabled(features)) {
            CephEncoder.encode(nSubDirs, byteBuf, le);
        }
    }

    @CephFieldDecode(order = 16)
    public void decodeNSubDirs(ByteBuf byteBuf, boolean le, BitSet features) {
        if (headerVersion >= 11) {
            nSubDirs = CephDecoder.decodeLong(byteBuf, le);
        }
    }

    @CephFieldEncode(order = 17)
    public void encodeFscryptAuth(ByteBuf byteBuf, boolean le, BitSet features) {
        if (CephFeatures.FLOCK.isEnabled(features) &&
                CephFeatures.EXPORT_PEER.isEnabled(features)) {
            CephEncoder.encode(fscryptAuth, byteBuf, le);
        }
    }

    @CephFieldDecode(order = 17)
    public void decodeFscryptAuth(ByteBuf byteBuf, boolean le, BitSet features) {
        if (headerVersion >= 12) {
            fscryptAuth = CephDecoder.decodeBytes(byteBuf, le);
        }
    }

    @CephFieldEncode(order = 18)
    public void encodeFscryptFile(ByteBuf byteBuf, boolean le, BitSet features) {
        if (CephFeatures.FLOCK.isEnabled(features) &&
                CephFeatures.EXPORT_PEER.isEnabled(features)) {
            CephEncoder.encode(fscryptFile, byteBuf, le);
        }
    }

    @CephFieldDecode(order = 18)
    public void decodeFscryptFile(ByteBuf byteBuf, boolean le, BitSet features) {
        if (headerVersion >= 12) {
            fscryptFile = CephDecoder.decodeBytes(byteBuf, le);
        }
    }

    @CephFieldEncode(order = 19)
    public void encodeSubVolumeId(ByteBuf byteBuf, boolean le, BitSet features) {
        if (CephFeatures.FLOCK.isEnabled(features) &&
                CephFeatures.EXPORT_PEER.isEnabled(features)) {
            CephEncoder.encode(subVolumeId, byteBuf, le);
        }
    }

    @CephFieldDecode(order = 19)
    public void decodeSubVolumeId(ByteBuf byteBuf, boolean le, BitSet features) {
        if (headerVersion >= 13) {
            subVolumeId = CephDecoder.decodeLong(byteBuf, le);
        }
    }
}
