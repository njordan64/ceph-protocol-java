/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.client;

import io.netty.util.AttributeKey;

import java.util.BitSet;

public class AttributeKeys {
    public static final AttributeKey<Integer> ADDR_NONCE =
            AttributeKey.newInstance("Addr-Nonce");

    public static final AttributeKey<BitSet> CLIENT_FEATURES =
            AttributeKey.newInstance("Client-Features");

    public static final AttributeKey<BitSet> SERVER_FEATURES =
            AttributeKey.newInstance("Server-Features");
}