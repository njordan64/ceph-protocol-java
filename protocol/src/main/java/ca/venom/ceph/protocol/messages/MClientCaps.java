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
import ca.venom.ceph.protocol.types.TimeSpec;
import ca.venom.ceph.protocol.types.UTime;
import ca.venom.ceph.protocol.types.mds.CapPeer;
import ca.venom.ceph.protocol.types.mds.CapsBody;
import ca.venom.ceph.protocol.types.mds.CapsExportBody;
import ca.venom.ceph.protocol.types.mds.CapsHead;
import ca.venom.ceph.protocol.types.mds.CapsNonExportBody;
import ca.venom.ceph.types.MessageType;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

import java.util.BitSet;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/1d146b4afffae5eb9031693f85cd9eabfc308679/src/messages/MClientCaps.h#L23
 */
@CephType
@CephMessagePayload(MessageType.MSG_CLIENT_CAPS)
public class MClientCaps extends MessagePayload {
    @Getter
    @Setter
    @CephField(minVersion = (byte) 1, maxVersion = (byte) 1)
    @CephField(minVersion = (byte) 2, maxVersion = (byte) 2)
    @CephField(minVersion = (byte) 3, maxVersion = (byte) 3)
    @CephField(minVersion = (byte) 4, maxVersion = (byte) 4)
    @CephField(minVersion = (byte) 5, maxVersion = (byte) 5)
    @CephField(minVersion = (byte) 6, maxVersion = (byte) 6)
    @CephField(minVersion = (byte) 7, maxVersion = (byte) 7)
    @CephField(minVersion = (byte) 8, maxVersion = (byte) 8)
    @CephField(minVersion = (byte) 9, maxVersion = (byte) 9)
    @CephField(minVersion = (byte) 10, maxVersion = (byte) 10)
    @CephField(minVersion = (byte) 11, maxVersion = (byte) 11)
    @CephField(minVersion = (byte) 12, maxVersion = (byte) 12)
    @CephField(minVersion = (byte) 13, maxVersion = (byte) 13)
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
    @CephField(order = 3, minVersion = 1, maxVersion = 1, sizeProperty = "getHead().getSnapTraceLen()")
    @CephField(order = 3, minVersion = 2, maxVersion = 2, sizeProperty = "getHead().getSnapTraceLen()")
    @CephField(order = 3, minVersion = 3, maxVersion = 3, sizeProperty = "getHead().getSnapTraceLen()")
    @CephField(order = 3, minVersion = 4, maxVersion = 4, sizeProperty = "getHead().getSnapTraceLen()")
    @CephField(order = 3, minVersion = 5, maxVersion = 5, sizeProperty = "getHead().getSnapTraceLen()")
    @CephField(order = 3, minVersion = 6, maxVersion = 6, sizeProperty = "getHead().getSnapTraceLen()")
    @CephField(order = 3, minVersion = 7, maxVersion = 7, sizeProperty = "getHead().getSnapTraceLen()")
    @CephField(order = 3, minVersion = 8, maxVersion = 8, sizeProperty = "getHead().getSnapTraceLen()")
    @CephField(order = 3, minVersion = 9, maxVersion = 9, sizeProperty = "getHead().getSnapTraceLen()")
    @CephField(order = 3, minVersion = 10, maxVersion = 10, sizeProperty = "getHead().getSnapTraceLen()")
    @CephField(order = 3, minVersion = 11, maxVersion = 11, sizeProperty = "getHead().getSnapTraceLen()")
    @CephField(order = 3, minVersion = 12, maxVersion = 12, sizeProperty = "getHead().getSnapTraceLen()")
    @CephField(order = 3, minVersion = 13, maxVersion = 13, sizeProperty = "getHead().getSnapTraceLen()")
    private byte[] snap;

    @Getter
    @Setter
    private byte[] xattr;

    @Getter
    @Setter
    @CephField(order = 4, includeSize = true, minVersion = 2, maxVersion = 2)
    @CephField(order = 4, includeSize = true, minVersion = 3, maxVersion = 3)
    @CephField(order = 4, includeSize = true, minVersion = 4, maxVersion = 4)
    @CephField(order = 4, includeSize = true, minVersion = 5, maxVersion = 5)
    @CephField(order = 4, includeSize = true, minVersion = 6, maxVersion = 6)
    @CephField(order = 4, includeSize = true, minVersion = 7, maxVersion = 7)
    @CephField(order = 4, includeSize = true, minVersion = 8, maxVersion = 8)
    @CephField(order = 4, includeSize = true, minVersion = 9, maxVersion = 9)
    @CephField(order = 4, includeSize = true, minVersion = 10, maxVersion = 10)
    @CephField(order = 4, includeSize = true, minVersion = 11, maxVersion = 11)
    @CephField(order = 4, includeSize = true, minVersion = 12, maxVersion = 12)
    @CephField(order = 4, includeSize = true, minVersion = 13, maxVersion = 13)
    private byte[] flock;

    @Getter
    @Setter
    @CephField(order = 5, minVersion = 3, maxVersion = 3)
    @CephField(order = 5, minVersion = 4, maxVersion = 4)
    @CephField(order = 5, minVersion = 5, maxVersion = 5)
    @CephField(order = 5, minVersion = 6, maxVersion = 6)
    @CephField(order = 5, minVersion = 7, maxVersion = 7)
    @CephField(order = 5, minVersion = 8, maxVersion = 8)
    @CephField(order = 5, minVersion = 9, maxVersion = 9)
    @CephField(order = 5, minVersion = 10, maxVersion = 10)
    @CephField(order = 5, minVersion = 11, maxVersion = 11)
    @CephField(order = 5, minVersion = 12, maxVersion = 12)
    @CephField(order = 5, minVersion = 13, maxVersion = 13)
    private CapPeer peer;

    @Getter
    @Setter
    @CephField(order = 6, minVersion = 4, maxVersion = 4)
    @CephField(order = 6, minVersion = 5, maxVersion = 5)
    @CephField(order = 6, minVersion = 6, maxVersion = 6)
    @CephField(order = 6, minVersion = 7, maxVersion = 7)
    @CephField(order = 6, minVersion = 8, maxVersion = 8)
    @CephField(order = 6, minVersion = 9, maxVersion = 9)
    @CephField(order = 6, minVersion = 10, maxVersion = 10)
    @CephField(order = 6, minVersion = 11, maxVersion = 11)
    @CephField(order = 6, minVersion = 12, maxVersion = 12)
    @CephField(order = 6, minVersion = 13, maxVersion = 13)
    private long inlineVersion = -1L;

    @Getter
    @Setter
    @CephField(order = 7, includeSize = true, minVersion = 4, maxVersion = 4)
    @CephField(order = 7, includeSize = true, minVersion = 5, maxVersion = 5)
    @CephField(order = 7, includeSize = true, minVersion = 6, maxVersion = 6)
    @CephField(order = 7, includeSize = true, minVersion = 7, maxVersion = 7)
    @CephField(order = 7, includeSize = true, minVersion = 8, maxVersion = 8)
    @CephField(order = 7, includeSize = true, minVersion = 9, maxVersion = 9)
    @CephField(order = 7, includeSize = true, minVersion = 10, maxVersion = 10)
    @CephField(order = 7, includeSize = true, minVersion = 11, maxVersion = 11)
    @CephField(order = 7, includeSize = true, minVersion = 12, maxVersion = 12)
    @CephField(order = 7, includeSize = true, minVersion = 13, maxVersion = 13)
    private byte[] inlineData;

    @Getter
    @Setter
    @CephField(order = 8, minVersion = 5, maxVersion = 5)
    @CephField(order = 8, minVersion = 6, maxVersion = 6)
    @CephField(order = 8, minVersion = 7, maxVersion = 7)
    @CephField(order = 8, minVersion = 8, maxVersion = 8)
    @CephField(order = 8, minVersion = 9, maxVersion = 9)
    @CephField(order = 8, minVersion = 10, maxVersion = 10)
    @CephField(order = 8, minVersion = 11, maxVersion = 11)
    @CephField(order = 8, minVersion = 12, maxVersion = 12)
    @CephField(order = 8, minVersion = 13, maxVersion = 13)
    private int osdEpochBarrier;

    @Getter
    @Setter
    @CephField(order = 9, minVersion = 6, maxVersion = 6)
    @CephField(order = 9, minVersion = 7, maxVersion = 7)
    @CephField(order = 9, minVersion = 8, maxVersion = 8)
    @CephField(order = 9, minVersion = 9, maxVersion = 9)
    @CephField(order = 9, minVersion = 10, maxVersion = 10)
    @CephField(order = 9, minVersion = 11, maxVersion = 11)
    @CephField(order = 9, minVersion = 12, maxVersion = 12)
    @CephField(order = 9, minVersion = 13, maxVersion = 13)
    private long oldestFlushTid;

    @Getter
    @Setter
    @CephField(order = 10, minVersion = 7, maxVersion = 7)
    @CephField(order = 10, minVersion = 8, maxVersion = 8)
    @CephField(order = 10, minVersion = 9, maxVersion = 9)
    @CephField(order = 10, minVersion = 10, maxVersion = 10)
    @CephField(order = 10, minVersion = 11, maxVersion = 11)
    @CephField(order = 10, minVersion = 12, maxVersion = 12)
    @CephField(order = 10, minVersion = 13, maxVersion = 13)
    private int callerUid;

    @Getter
    @Setter
    @CephField(order = 11, minVersion = 7, maxVersion = 7)
    @CephField(order = 11, minVersion = 8, maxVersion = 8)
    @CephField(order = 11, minVersion = 9, maxVersion = 9)
    @CephField(order = 11, minVersion = 10, maxVersion = 10)
    @CephField(order = 11, minVersion = 11, maxVersion = 11)
    @CephField(order = 11, minVersion = 12, maxVersion = 12)
    @CephField(order = 11, minVersion = 13, maxVersion = 13)
    private int callerGid;

    @Getter
    @Setter
    @CephField(order = 13, minVersion = 9, maxVersion = 9)
    @CephField(order = 13, minVersion = 10, maxVersion = 10)
    @CephField(order = 13, minVersion = 11, maxVersion = 11)
    @CephField(order = 13, minVersion = 12, maxVersion = 12)
    @CephField(order = 13, minVersion = 13, maxVersion = 13)
    private UTime bTime;

    @Getter
    @Setter
    @CephField(order = 14, minVersion = 9, maxVersion = 9)
    @CephField(order = 14, minVersion = 10, maxVersion = 10)
    @CephField(order = 14, minVersion = 11, maxVersion = 11)
    @CephField(order = 14, minVersion = 12, maxVersion = 12)
    @CephField(order = 14, minVersion = 13, maxVersion = 13)
    private long changeAttr = 0;

    @Getter
    @Setter
    @CephField(order = 15, minVersion = 10, maxVersion = 10)
    @CephField(order = 15, minVersion = 11, maxVersion = 11)
    @CephField(order = 15, minVersion = 12, maxVersion = 12)
    @CephField(order = 15, minVersion = 13, maxVersion = 13)
    private int flags = 0;

    @Getter
    @Setter
    @CephField(order = 16, minVersion = 11, maxVersion = 11)
    @CephField(order = 16, minVersion = 12, maxVersion = 12)
    @CephField(order = 16, minVersion = 13, maxVersion = 13)
    private long nFiles = -1;

    @Getter
    @Setter
    @CephField(order = 17, minVersion = 11, maxVersion = 11)
    @CephField(order = 17, minVersion = 12, maxVersion = 12)
    @CephField(order = 17, minVersion = 13, maxVersion = 13)
    private long nSubDirs = -1;

    @Getter
    @Setter
    @CephField(order = 18, includeSize = true, minVersion = 12, maxVersion = 12)
    @CephField(order = 18, includeSize = true, minVersion = 13, maxVersion = 13)
    private byte[] fscryptAuth;

    @Getter
    @Setter
    @CephField(order = 19, includeSize = true, minVersion = 12, maxVersion = 12)
    @CephField(order = 19, includeSize = true, minVersion = 13, maxVersion = 13)
    private byte[] fscryptFile;

    @Getter
    @Setter
    @CephField(order = 20, minVersion = 13, maxVersion = 13)
    private long subVolumeId;

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
    public void prepareForEncode(ByteBuf byteBuf, boolean le, BitSet features) {
        head.setSnapTraceLen(snap.length);
        head.setXattrLen(xattr.length);
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

    @CephFieldEncode(order = 2, minVersion = (byte) 1, maxVersion = (byte) 1)
    @CephFieldEncode(order = 2, minVersion = (byte) 2, maxVersion = (byte) 2)
    @CephFieldEncode(order = 2, minVersion = (byte) 3, maxVersion = (byte) 3)
    @CephFieldEncode(order = 2, minVersion = (byte) 4, maxVersion = (byte) 4)
    @CephFieldEncode(order = 2, minVersion = (byte) 5, maxVersion = (byte) 5)
    @CephFieldEncode(order = 2, minVersion = (byte) 6, maxVersion = (byte) 6)
    @CephFieldEncode(order = 2, minVersion = (byte) 7, maxVersion = (byte) 7)
    @CephFieldEncode(order = 2, minVersion = (byte) 8, maxVersion = (byte) 8)
    @CephFieldEncode(order = 2, minVersion = (byte) 9, maxVersion = (byte) 9)
    @CephFieldEncode(order = 2, minVersion = (byte) 10, maxVersion = (byte) 10)
    @CephFieldEncode(order = 2, minVersion = (byte) 11, maxVersion = (byte) 11)
    @CephFieldEncode(order = 2, minVersion = (byte) 12, maxVersion = (byte) 12)
    @CephFieldEncode(order = 2, minVersion = (byte) 13, maxVersion = (byte) 13)
    public void encodeBody(ByteBuf byteBuf, boolean le, BitSet features) {
        if (head.getOp() == CapsHead.OpValue.EXPORT) {
            final CapsExportBody exportBody = new CapsExportBody();
            exportBody.setPeer(peer);
            CephEncoder.encode(exportBody, byteBuf, le, features);
            byteBuf.writeZero(63);
            return;
        }

        final CapsNonExportBody nonExportBody = new CapsNonExportBody();
        nonExportBody.setSize(size);
        nonExportBody.setMaxSize(maxSize);
        nonExportBody.setTruncateSize(truncateSize);
        nonExportBody.setTruncateSeq(truncateSeq);

        TimeSpec timeSpec = new TimeSpec();
        timeSpec.setTvSec(mtime.getTvSec());
        timeSpec.setTvNSec(mtime.getTvNSec());
        nonExportBody.setMTime(timeSpec);

        timeSpec = new TimeSpec();
        timeSpec.setTvSec(atime.getTvSec());
        timeSpec.setTvNSec(atime.getTvNSec());
        nonExportBody.setATime(timeSpec);

        timeSpec = new TimeSpec();
        timeSpec.setTvSec(ctime.getTvSec());
        timeSpec.setTvNSec(ctime.getTvNSec());
        nonExportBody.setCTime(timeSpec);

        final CephFileLayout cephFileLayout = new CephFileLayout();
        layout.toLegacy(cephFileLayout);
        nonExportBody.setLayout(cephFileLayout);
        nonExportBody.setTimeWarpSeq(timeWarpSeq);
        CephEncoder.encode(nonExportBody, byteBuf, le, features);
    }

    @CephFieldDecode(order = 2, minVersion = 1, maxVersion = 1)
    @CephFieldDecode(order = 2, minVersion = 2, maxVersion = 2)
    @CephFieldDecode(order = 2, minVersion = 3, maxVersion = 3)
    @CephFieldDecode(order = 2, minVersion = 4, maxVersion = 4)
    @CephFieldDecode(order = 2, minVersion = 5, maxVersion = 5)
    @CephFieldDecode(order = 2, minVersion = 6, maxVersion = 6)
    @CephFieldDecode(order = 2, minVersion = 7, maxVersion = 7)
    @CephFieldDecode(order = 2, minVersion = 8, maxVersion = 8)
    @CephFieldDecode(order = 2, minVersion = 9, maxVersion = 9)
    @CephFieldDecode(order = 2, minVersion = 10, maxVersion = 10)
    @CephFieldDecode(order = 2, minVersion = 11, maxVersion = 11)
    @CephFieldDecode(order = 2, minVersion = 12, maxVersion = 12)
    @CephFieldDecode(order = 2, minVersion = 13, maxVersion = 13)
    public void decodeBody(ByteBuf byteBuf, boolean le, BitSet features) {
        if (head.getOp() == CapsHead.OpValue.EXPORT) {
            try {
                final CapsExportBody exportBody = CephDecoder.decode(byteBuf, le, features, CapsExportBody.class);
                peer = exportBody.getPeer();
                byteBuf.readerIndex(byteBuf.readerIndex() + 63);
            } catch (DecodingException de) {
                throw new RuntimeException(de);
            }
            return;
        }

        try {
            final CapsNonExportBody nonExportBody = CephDecoder.decode(byteBuf, le, features, CapsNonExportBody.class);
            size = nonExportBody.getSize();
            maxSize = nonExportBody.getMaxSize();
            truncateSize = nonExportBody.getTruncateSize();
            truncateSeq = nonExportBody.getTruncateSeq();

            mtime = new UTime();
            mtime.setTvSec(nonExportBody.getMTime().getTvSec());
            mtime.setTvNSec(nonExportBody.getMTime().getTvNSec());

            atime = new UTime();
            atime.setTvSec(nonExportBody.getATime().getTvSec());
            atime.setTvNSec(nonExportBody.getATime().getTvNSec());

            ctime = new UTime();
            ctime.setTvSec(nonExportBody.getCTime().getTvSec());
            ctime.setTvNSec(nonExportBody.getCTime().getTvNSec());

            layout = new FileLayout();
            layout.fromLegacy(nonExportBody.getLayout());

            timeWarpSeq = nonExportBody.getTimeWarpSeq();
        } catch (DecodingException de) {
            throw new RuntimeException(de);
        }
    }

    @CephFieldEncode(order = 7, minVersion = 4, maxVersion = 4)
    @CephFieldEncode(order = 7, minVersion = 5, maxVersion = 5)
    @CephFieldEncode(order = 7, minVersion = 6, maxVersion = 6)
    @CephFieldEncode(order = 7, minVersion = 7, maxVersion = 7)
    @CephFieldEncode(order = 7, minVersion = 8, maxVersion = 8)
    @CephFieldEncode(order = 7, minVersion = 9, maxVersion = 9)
    @CephFieldEncode(order = 7, minVersion = 10, maxVersion = 10)
    @CephFieldEncode(order = 7, minVersion = 11, maxVersion = 11)
    @CephFieldEncode(order = 7, minVersion = 12, maxVersion = 12)
    @CephFieldEncode(order = 7, minVersion = 13, maxVersion = 13)
    public void encodeInlineData(ByteBuf byteBuf, boolean le, BitSet features) {
        if (CephFeatures.MDS_INLINE_DATA.isEnabled(features)) {
            CephEncoder.encode(inlineData, byteBuf, le, features);
        } else {
            byteBuf.writeZero(4);
        }
    }

    @CephFieldEncode(order = 12, minVersion = 8, maxVersion = 8)
    @CephFieldEncode(order = 12, minVersion = 9, maxVersion = 9)
    @CephFieldEncode(order = 12, minVersion = 10, maxVersion = 10)
    @CephFieldEncode(order = 12, minVersion = 11, maxVersion = 11)
    @CephFieldEncode(order = 12, minVersion = 12, maxVersion = 12)
    @CephFieldEncode(order = 12, minVersion = 13, maxVersion = 13)
    public void encodePoolNs(ByteBuf byteBuf, boolean le, BitSet features) {
        CephEncoder.encodeString(layout.getPoolNs(), byteBuf, le);
    }

    @CephFieldDecode(order = 12, minVersion = 8, maxVersion = 8)
    @CephFieldDecode(order = 12, minVersion = 9, maxVersion = 9)
    @CephFieldDecode(order = 12, minVersion = 10, maxVersion = 10)
    @CephFieldDecode(order = 12, minVersion = 11, maxVersion = 11)
    @CephFieldDecode(order = 12, minVersion = 12, maxVersion = 12)
    @CephFieldDecode(order = 12, minVersion = 13, maxVersion = 13)
    public void decodePoolNs(ByteBuf byteBuf, boolean le, BitSet features) {
        layout.setPoolNs(CephDecoder.decodeString(byteBuf, le));
    }

    @CephPostDecode
    public void postDecode(short version) {
        if (version < 4) {
            inlineVersion = -1L;
        }
    }
}
