/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.protocol;

import ca.venom.ceph.protocol.types.mon.MonMap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.FileInputStream;

public class DecodeTest {
    public static void main(String[] args) throws Exception {
        FileInputStream fis = new FileInputStream("monmap.bin");
        byte[] bytes = fis.readAllBytes();
        fis.close();

        ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes, 4, bytes.length - 4);
        MonMap monMap = CephDecoder.decode(byteBuf, true, MonMap.class);
        System.out.println(">>> Done");
    }
}
