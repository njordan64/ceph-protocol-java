package ca.venom.ceph.encoding.annotations;

public enum StartDecodeFormat {
    NONE,
    VERSION_ONLY,
    STANDARD,
    UNCHECKED,
    UNKNOWN, // Unsupported
    LEGACY_COMPAT_LEN
}
