/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.protocol.messages;

import ca.venom.ceph.encoding.annotations.CephField;
import ca.venom.ceph.encoding.annotations.CephMessagePayload;
import ca.venom.ceph.encoding.annotations.CephType;
import ca.venom.ceph.protocol.types.UTime;
import ca.venom.ceph.protocol.types.mds.NestInfo;
import ca.venom.ceph.protocol.types.mds.QuotaInfo;
import ca.venom.ceph.types.MessageType;
import lombok.Getter;
import lombok.Setter;

/**
 * [Ceph URL] https://github.com/ceph/ceph/blob/v17.2.6/src/messages/MClientQuota.h#L6
 */
@CephType
@CephMessagePayload(MessageType.MSG_CLIENT_QUOTA)
public class MClientQuota extends MessagePayload {
    @Getter
    @Setter
    @CephField
    private long ino;

    @Getter
    @Setter
    private NestInfo rstat;

    @Getter
    @Setter
    @CephField(order = 2)
    private UTime rcTime;

    @Getter
    @Setter
    @CephField(order = 3)
    private long rBytes;

    @Getter
    @Setter
    @CephField(order = 4)
    private long rFiles;

    @Getter
    @Setter
    @CephField(order = 5)
    private long rSubdirs;

    @Getter
    @Setter
    @CephField(order = 6)
    private QuotaInfo quota;

    @Override
    public void prepareForEncode() {
        rcTime = rstat.getRcTime();
        rBytes = rstat.getRBytes();
        rFiles = rstat.getRFiles();
        rSubdirs = rstat.getRSubdirs();
    }

    @Override
    public void finishDecode() {
        rstat = new NestInfo();
        rstat.setRcTime(rcTime);
        rstat.setRBytes(rBytes);
        rstat.setRFiles(rFiles);
        rstat.setRSubdirs(rSubdirs);
    }
}
