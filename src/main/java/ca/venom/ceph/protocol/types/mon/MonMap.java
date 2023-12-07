package ca.venom.ceph.protocol.types.mon;

import ca.venom.ceph.EnumWithIntValue;
import ca.venom.ceph.protocol.CephRelease;
import ca.venom.ceph.protocol.types.annotations.CephField;
import ca.venom.ceph.protocol.types.annotations.CephType;
import ca.venom.ceph.protocol.types.annotations.CephTypeSize;
import ca.venom.ceph.protocol.types.annotations.CephTypeVersion;
import ca.venom.ceph.protocol.types.CephUUID;
import ca.venom.ceph.protocol.types.UTime;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CephType
@CephTypeVersion(version = 9, compatVersion = 6)
@CephTypeSize
public class MonMap {
    public enum ElectionStrategy implements EnumWithIntValue {
        CLASSIC(1),
        DISALLOW(2),
        CONNECTIVITY(3);

        private int value;

        ElectionStrategy(int value) {
            this.value = value;
        }

        public static ElectionStrategy getFromValueInt(int value) {
            for (ElectionStrategy electionStrategy : values()) {
                if (electionStrategy.value == value) {
                    return electionStrategy;
                }
            }

            return null;
        }


        @Override
        public int getValueInt() {
            return value;
        }
    }

    @Getter
    @Setter
    @CephField
    private int epoch;

    @Getter
    @Setter
    @CephField(order = 2)
    private CephUUID fsid;

    @Getter
    @Setter
    @CephField(order = 3)
    private UTime lastChanged;

    @Getter
    @Setter
    @CephField(order = 4)
    private UTime created;

    @Getter
    @Setter
    @CephField(order = 5)
    private MonFeature persistentFeatures;

    @Getter
    @Setter
    @CephField(order = 6)
    private MonFeature optionalFeatures;

    @Getter
    @Setter
    @CephField(order = 7)
    private Map<String, MonInfo> monInfo;

    @Getter
    @Setter
    @CephField(order = 8)
    private Map<String, String> addrMons;

    @Getter
    @Setter
    @CephField(order = 9)
    private List<String> ranks;

    @Getter
    @Setter
    @CephField(order = 10)
    private CephRelease minMonRelease;

    @Getter
    @Setter
    @CephField(order = 11)
    private Set<Integer> removedRanks;

    @Getter
    @Setter
    @CephField(order = 12)
    private ElectionStrategy strategy;

    @Getter
    @Setter
    @CephField(order = 13)
    private String disallowedLeaders;

    @Getter
    @Setter
    @CephField(order = 14)
    private boolean stretchModeEnabled;

    @Getter
    @Setter
    @CephField(order = 15)
    private String tieBreakerMon;

    @Getter
    @Setter
    @CephField(order = 16)
    private Set<String> stretchMarkedDownMons;
}
