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

import ca.venom.ceph.encoding.annotations.CephChildType;
import ca.venom.ceph.encoding.annotations.CephMarker;
import ca.venom.ceph.encoding.annotations.CephParentType;
import ca.venom.ceph.encoding.annotations.CephType;
import ca.venom.ceph.encoding.annotations.CephTypeSize;
import ca.venom.ceph.encoding.annotations.CephTypeVersionConstant;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

@CephType
@CephTypeSize
@CephParentType(typeSize = 1)
@CephChildType(typeValue = 0, typeClass = AddrLegacy.class)
@CephChildType(typeValue = 1, typeClass = Addr.class)
public abstract class AddrBase {
    public abstract short getPort();

    public abstract InetAddress getAddress();

    public abstract int getIPv6FlowInfo();

    public abstract void setIPv4AddrWithPort(Inet4Address addr, short port);

    public abstract void setIPv6AddrWithPort(Inet6Address addr, short port, int flowInfo);
}
