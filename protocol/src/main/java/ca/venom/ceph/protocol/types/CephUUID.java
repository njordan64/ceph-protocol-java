/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.protocol.types;

import ca.venom.ceph.encoding.annotations.ByteOrderPreference;
import ca.venom.ceph.encoding.annotations.CephEncodingSize;
import ca.venom.ceph.encoding.annotations.CephField;
import ca.venom.ceph.encoding.annotations.CephType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@CephType()
public class CephUUID {
    @Getter
    @Setter
    @CephField(byteOrderPreference = ByteOrderPreference.BE)
    @CephEncodingSize(16)
    private byte[] bytes;

    public UUID getUUID() {
        ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);
        long high = byteBuf.getLongLE(0);
        long low = byteBuf.getLongLE(8);

        return new UUID(high, low);
    }
}
