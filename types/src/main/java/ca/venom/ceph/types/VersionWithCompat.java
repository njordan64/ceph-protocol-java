package ca.venom.ceph.types;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class VersionWithCompat {
    @Getter
    private byte version;

    @Getter
    private Byte compat;
}
