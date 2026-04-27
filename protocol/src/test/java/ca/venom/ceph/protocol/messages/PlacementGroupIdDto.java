package ca.venom.ceph.protocol.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlacementGroupIdDto {
    @JsonProperty("mPool")
    private long mPool;

    @JsonProperty("mSeed")
    private int mSeed;
}
