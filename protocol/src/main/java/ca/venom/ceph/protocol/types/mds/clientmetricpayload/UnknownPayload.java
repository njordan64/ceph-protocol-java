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

import ca.venom.ceph.encoding.annotations.CephFieldEncode;
import ca.venom.ceph.protocol.CephDecoder;
import io.netty.buffer.ByteBuf;

import java.util.BitSet;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/1d146b4afffae5eb9031693f85cd9eabfc308679/src/include/cephfs/metrics/Types.h#L537
 */
public class UnknownPayload extends ClientMetricPayload {
    public UnknownPayload() {
        super(ClientMetricType.UNKNOWN);
    }

    @CephFieldEncode
    public void encodeData(ByteBuf byteBuf, boolean le, BitSet features) {
        // Skip version and compat version
        byteBuf.readerIndex(byteBuf.readerIndex() + 2);

        final int length = CephDecoder.decodeInt(byteBuf, le);
        byteBuf.readerIndex(byteBuf.readerIndex() + length);
    }
}
