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

import ca.venom.ceph.encoding.annotations.CephChildType;
import ca.venom.ceph.encoding.annotations.CephParentType;
import ca.venom.ceph.encoding.annotations.CephType;

@CephType
@CephParentType(typeSize = 1, typeOffset = 0)
@CephChildType(typeValue = 1, typeClass = CephXAuthorizeReplyV1.class)
@CephChildType(typeValue = 2, typeClass = CephXAuthorizeReplyV2.class)
public class CephXAuthorizeReply {
}
