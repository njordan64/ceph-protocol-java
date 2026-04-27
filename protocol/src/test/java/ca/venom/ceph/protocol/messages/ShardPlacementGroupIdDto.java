package ca.venom.ceph.protocol.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShardPlacementGroupIdDto {
    @JsonProperty("pgid")
    private PlacementGroupIdDto pgid;

    @JsonProperty("shard")
    private byte shard;
}
