/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
module ca.venom.ceph.protocol.analysis {
    requires ca.venom.ceph.protocol.protocol;
    requires ca.venom.ceph.protocol.utils;
    requires com.fasterxml.jackson.databind;
    requires io.netty.buffer;
}