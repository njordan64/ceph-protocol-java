/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.protocol.types.mds.clientmetricpayload;

import ca.venom.ceph.encoding.annotations.CephFieldDecode;
import ca.venom.ceph.encoding.annotations.CephFieldEncode;
import ca.venom.ceph.encoding.annotations.CephType;
import ca.venom.ceph.protocol.CephDecoder;
import ca.venom.ceph.protocol.CephEncoder;
import ca.venom.ceph.protocol.DecodingException;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;

import java.util.BitSet;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/1d146b4afffae5eb9031693f85cd9eabfc308679/src/include/cephfs/metrics/Types.h#L804
 */
@CephType
public class ClientMetricMessage {
    @Getter
    @Setter
    private ClientMetricPayload payload;

    @CephFieldEncode
    public void encodePayload(ByteBuf byteBuf, boolean le, BitSet features) {
        CephEncoder.encode(payload.getType().getValueInt(), byteBuf, le);
        CephEncoder.encode(payload, byteBuf, le, features);
    }

    @CephFieldDecode
    public void decodePayload(ByteBuf byteBuf, boolean le, BitSet features) {
        final ClientMetricType metricType = ClientMetricType.getFromValueInt(CephDecoder.decodeInt(byteBuf, le));
        if (metricType == null) {
            payload = null;
            return;
        }

        try {
            switch (metricType) {
                case CAP_INFO:
                    payload = CephDecoder.decode(byteBuf, le, features, CapInfoPayload.class);
                    break;
                case READ_LATENCY:
                    payload = CephDecoder.decode(byteBuf, le, features, ReadLatencyPayload.class);
                    break;
                case WRITE_LATENCY:
                    payload = CephDecoder.decode(byteBuf, le, features, WriteLatencyPayload.class);
                    break;
                case METADATA_LATENCY:
                    payload = CephDecoder.decode(byteBuf, le, features, MetadataLatencyPayload.class);
                    break;
                case DENTRY_LEASE:
                    payload = CephDecoder.decode(byteBuf, le, features, DentryLeasePayload.class);
                    break;
                case OPENED_FILES:
                    payload = CephDecoder.decode(byteBuf, le, features, OpenedFilesPayload.class);
                    break;
                case PINNED_ICAPS:
                    payload = CephDecoder.decode(byteBuf, le, features, PinnedIcapsPayload.class);
                    break;
                case OPENED_INODES:
                    payload = CephDecoder.decode(byteBuf, le, features, OpenedInodesPayload.class);
                    break;
                case READ_IO_SIZES:
                    payload = CephDecoder.decode(byteBuf, le, features, ReadIOSizesPayload.class);
                    break;
                case WRITE_IO_SIZES:
                    payload = CephDecoder.decode(byteBuf, le, features, WriteIOSizesPayload.class);
                    break;
                case SUBVOLUME_METRICS:
                    payload = CephDecoder.decode(byteBuf, le, features, SubvolumeMetricsPayload.class);
                    break;
                default:
                    payload = CephDecoder.decode(byteBuf, le, features, UnknownPayload.class);
                    break;
            }
        } catch (DecodingException de) {
            payload = null;
        }
    }
}
