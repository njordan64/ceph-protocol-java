/*
 * Copyright (C) 2023 Norman Jordan <norman.jordan@gmail.com>
 *
 * This is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software
 * Foundation.  See file COPYING.
 *
 */
package ca.venom.ceph.annotation.processor.parser;

import java.util.Arrays;

public class VersionGroup {
    public static final VersionGroup DEFAULT = new VersionGroup((byte) -1, (byte) -1);

    private final byte minVersion;

    private final byte maxVersion;

    public VersionGroup(byte minVersion, byte maxVersion) {
        this.minVersion = minVersion;
        this.maxVersion = maxVersion;
    }

    public byte getMinVersion() {
        return minVersion;
    }

    public byte getMaxVersion() {
        return maxVersion;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof VersionGroup versionGroup) {
            return minVersion == versionGroup.minVersion &&
                    maxVersion == versionGroup.maxVersion;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(new byte[] {minVersion, maxVersion});
    }
}
