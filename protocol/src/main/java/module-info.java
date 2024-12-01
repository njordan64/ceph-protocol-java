/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
module ca.venom.ceph.protocol.protocol {
    requires static lombok;
    requires ca.venom.ceph.protocol.annotation.processor;
    requires ca.venom.ceph.protocol.annotations;
    requires ca.venom.ceph.protocol.types;
    requires ca.venom.ceph.protocol.utils;
    requires org.slf4j;
    requires io.netty.buffer;

    exports ca.venom.ceph.protocol;
    exports ca.venom.ceph.protocol.decode;
    exports ca.venom.ceph.protocol.frames;
    exports ca.venom.ceph.protocol.messages;
    exports ca.venom.ceph.protocol.types;
    exports ca.venom.ceph.protocol.types.auth;
    exports ca.venom.ceph.protocol.types.mon;
}