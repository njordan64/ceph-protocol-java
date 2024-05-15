/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.protocol.types.auth;

import ca.venom.ceph.encoding.annotations.CephField;
import ca.venom.ceph.encoding.annotations.CephType;
import ca.venom.ceph.encoding.annotations.CephTypeVersion;
import lombok.Getter;
import lombok.Setter;

@CephType
@CephTypeVersion(version = 1)
public class CephXAuthorizeReplyV1 extends CephXAuthorizeReply {
    @CephField
    @Getter
    @Setter
    private long noncePlusOne;
}
