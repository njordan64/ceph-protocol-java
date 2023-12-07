package ca.venom.ceph.protocol.types;

import ca.venom.ceph.protocol.types.annotations.CephType;
import lombok.Getter;
import lombok.Setter;

@CephType
public class CephBytes {
    @Getter
    @Setter
    private byte[] bytes;

    public int getSize() {
        if (bytes == null) {
            return 0;
        } else {
            return bytes.length;
        }
    }
}
