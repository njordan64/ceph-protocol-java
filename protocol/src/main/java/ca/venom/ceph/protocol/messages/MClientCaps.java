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

import ca.venom.ceph.encoding.annotations.CephCondition;
import ca.venom.ceph.encoding.annotations.CephField;
import ca.venom.ceph.encoding.annotations.CephMessagePayload;
import ca.venom.ceph.encoding.annotations.CephParentType;
import ca.venom.ceph.encoding.annotations.CephParentTypeValue;
import ca.venom.ceph.encoding.annotations.CephType;
import ca.venom.ceph.encoding.annotations.ConditonOperator;
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
    @CephField(order = 2)
    @CephParentTypeValue("getHead().getOp().getValueInt()")
    private CapsBody body;

    @Getter
    @Setter
    @CephField(order = 3, sizeProperty = "getHead().getSnapTraceLen()")
    private byte[] snap;

    @Getter
    @Setter
    private byte[] xattr;

    @Getter
    @Setter
    @CephField(order = 4, includeSize = true)
    private byte[] flock;

    @Getter
    @Setter
    @CephField(order = 5)
    @CephCondition(operator = ConditonOperator.EQUAL, property = "getHead().getOp()", values = {"ca.venom.ceph.protocol.types.mds.CapsHead.OpValue.IMPORT"})
    private CapPeer peer;

    @Getter
    @Setter
    @CephField(order = 6)
    private long inline_version = 0;

    @Getter
    @Setter
    @CephField(order = 7, includeSize = true)
    private byte[] inlineData;

    @Getter
    @Setter
    @CephField(order = 8)
    private int osdEpochBarrier = 0;

    @Getter
    @Setter
    @CephField(order = 9)
    private int callerUid = 0;

    @Getter
    @Setter
    @CephField(order = 10)
    private int callerGid = 0;

    @Getter
    @Setter
    @CephField(order = 11)
    private String poolNs;

    @Getter
    @Setter
    @CephField(order = 12)
    private UTime bTime;

    @Getter
    @Setter
    @CephField(order = 13)
    private long changeAttr = 0;

    @Getter
    @Setter
    @CephField(order = 14)
    private int flags = 0;

    @Getter
    @Setter
    @CephField(order = 15)
    private long nFiles = -1;

    @Getter
    @Setter
    @CephField(order = 16)
    private long nSubdirs = -1;

    private FileLayout layout = new FileLayout();

    @Override
    public short getHeadVersion() {
        return 11;
    }

    @Override
    public short getHeadCompatVersion() {
        return 1;
    }

    @Override
    public void prepareForEncode() {
        head.setSnapTraceLen(snap.length);
        head.setXattrLen(xattr.length);

        if (head.getOp() != CapsHead.OpValue.EXPORT) {
            layout.toLegacy(((CapsNonExportBody) body).getLayout());
        }

        poolNs = layout.getPoolNs();
    }

    @Override
    public void finishDecode() {
        if (body instanceof CapsNonExportBody capsBody) {
            layout.fromLegacy(capsBody.getLayout());
        }

        layout.setPoolNs(poolNs);
    }

    @Override
    public void encodeMiddle(ByteBuf byteBuf, boolean le) {
        byteBuf.writeBytes(xattr);
    }

    @Override
    public void decodeMiddle(ByteBuf byteBuf, boolean le) {
        if (head.getXattrLen() > 0) {
            xattr = new byte[head.getXattrLen()];
            byteBuf.readBytes(xattr);
        }
    }
}
