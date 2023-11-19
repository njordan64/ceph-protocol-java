package ca.venom.ceph.protocol.types.mon;

import ca.venom.ceph.EnumWithIntValue;
import ca.venom.ceph.protocol.CephRelease;
import ca.venom.ceph.protocol.types.CephBoolean;
import ca.venom.ceph.protocol.types.CephDataType;
import ca.venom.ceph.protocol.types.CephEnum;
import ca.venom.ceph.protocol.types.CephList;
import ca.venom.ceph.protocol.types.CephMap;
import ca.venom.ceph.protocol.types.CephSet;
import ca.venom.ceph.protocol.types.CephString;
import ca.venom.ceph.protocol.types.CephUUID;
import ca.venom.ceph.protocol.types.Int32;
import ca.venom.ceph.protocol.types.UTime;
import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class MonMap implements CephDataType {
    private static final byte VERSION = 9;
    private static final byte COMPAT_VERSION = 6;

    private Int32 epoch;
    private CephUUID fsid;
    private UTime lastChanged;
    private UTime created;
    private MonFeature persistentFeatures;
    private MonFeature optionalFeatures;
    private CephMap<CephString, MonInfo> monInfo;
    private CephMap<CephString, CephString> addrMons;
    private CephList<CephString> ranks;
    private CephEnum<CephRelease> minMonRelease;
    private CephSet<Int32> removedRanks;
    private CephEnum<ElectionStrategy> strategy;
    private CephSet<CephString> disallowedLeaders;
    private CephBoolean stretchModeEnabled;
    private CephString tieBreakerMon;
    private CephSet<CephString> stretchMarkedDownMons;

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

    public int getEpoch() {
        return epoch.getValue();
    }

    public void setEpoch(int epoch) {
        this.epoch = new Int32(epoch);
    }

    public UUID getFsid() {
        return fsid.getValue();
    }

    public void setFsid(UUID fsid) {
        this.fsid = new CephUUID(fsid);
    }

    public UTime getLastChanged() {
        return lastChanged;
    }

    public void setLastChanged(UTime lastChanged) {
        this.lastChanged = lastChanged;
    }

    public UTime getCreated() {
        return created;
    }

    public void setCreated(UTime created) {
        this.created = created;
    }

    public MonFeature getPersistentFeatures() {
        return persistentFeatures;
    }

    public void setPersistentFeatures(MonFeature persistentFeatures) {
        this.persistentFeatures = persistentFeatures;
    }

    public MonFeature getOptionalFeatures() {
        return optionalFeatures;
    }

    public void setOptionalFeatures(MonFeature optionalFeatures) {
        this.optionalFeatures = optionalFeatures;
    }

    public Map<String, MonInfo> getMonInfo() {
        Map<String, MonInfo> simplified = new HashMap<>();
        for (Map.Entry<CephString, MonInfo> entry : monInfo.getMap().entrySet()) {
            simplified.put(entry.getKey().getValue(), entry.getValue());
        }

        return simplified;
    }

    public void setMonInfo(Map<String, MonInfo> monInfo) {
        this.monInfo = new CephMap<>(CephString.class, MonInfo.class);
        Map<CephString, MonInfo> values = new HashMap<>();
        this.monInfo.setMap(values);

        for (Map.Entry<String, MonInfo> entry : monInfo.entrySet()) {
            values.put(new CephString(entry.getKey()), entry.getValue());
        }
    }

    public Map<String, String> getAddrMons() {
        Map<String, String> simplified = new HashMap<>();
        for (Map.Entry<CephString, CephString> entry : addrMons.getMap().entrySet()) {
            simplified.put(entry.getKey().getValue(), entry.getValue().getValue());
        }

        return simplified;
    }

    public void setAddrMons(Map<String, String> addrMons) {
        this.addrMons = new CephMap<>(CephString.class, CephString.class);
        Map<CephString, CephString> values = new HashMap<>();
        this.addrMons.setMap(values);

        for (Map.Entry<String, String> entry : addrMons.entrySet()) {
            values.put(new CephString(entry.getKey()), new CephString(entry.getValue()));
        }
    }

    public List<String> getRanks() {
        return ranks.getValues()
                .stream()
                .map(CephString::getValue)
                .collect(Collectors.toList());
    }

    public void setRanks(List<String> ranks) {
        this.ranks = new CephList<>(CephString.class);
        this.ranks.setValues(
                ranks
                        .stream()
                        .map(CephString::new)
                        .collect(Collectors.toList())
        );
    }

    public CephRelease getMinMonRelease() {
        return minMonRelease.getValue();
    }

    public void setMinMonRelease(CephRelease minMonRelease) {
        this.minMonRelease = new CephEnum<>(minMonRelease);
    }

    public Set<Integer> getRemovedRanks() {
        Set<Integer> simplified = new HashSet<>();
        for (Int32 value : removedRanks.getValues()) {
            simplified.add(value.getValue());
        }

        return simplified;
    }

    public void setRemovedRanks(Set<Integer> removedRanks) {
        this.removedRanks = new CephSet<>(Int32.class);
        Set<Int32> values = new HashSet<>();
        this.removedRanks.setValues(values);

        for (Integer value : removedRanks) {
            values.add(new Int32(value));
        }
    }

    public ElectionStrategy getStrategy() {
        return strategy.getValue();
    }

    public void setStrategy(ElectionStrategy strategy) {
        this.strategy = new CephEnum<>(strategy);
    }

    public Set<String> getDisallowedLeaders() {
        Set<String> simplified = new HashSet<>();
        for (CephString disallowedLeader : disallowedLeaders.getValues()) {
            simplified.add(disallowedLeader.getValue());
        }

        return simplified;
    }

    public void setDisallowedLeaders(Set<String> disallowedLeaders) {
        this.disallowedLeaders = new CephSet<>(CephString.class);
        Set<CephString> values = new HashSet<>();
        this.disallowedLeaders.setValues(values);

        for (String disallowedLeader : disallowedLeaders) {
            values.add(new CephString(disallowedLeader));
        }
    }

    public boolean getStretchModeEnabled() {
        return stretchModeEnabled.getValue();
    }

    public void setStretchModeEnabled(boolean stretchModeEnabled) {
        this.stretchModeEnabled = new CephBoolean(stretchModeEnabled);
    }

    public String getTieBreakerMon() {
        return tieBreakerMon.getValue();
    }

    public void setTieBreakerMon(String tieBreakerMon) {
        this.tieBreakerMon = new CephString(tieBreakerMon);
    }

    public Set<String> getStretchMarkedDownMons() {
        Set<String> simplified = new HashSet<>();
        for (CephString value : stretchMarkedDownMons.getValues()) {
            simplified.add(value.getValue());
        }

        return simplified;
    }

    public void setStretchMarkedDownMons(Set<String> stretchMarkedDownMons) {
        this.stretchMarkedDownMons = new CephSet<>(CephString.class);
        Set<CephString> values = new HashSet<>();
        this.stretchMarkedDownMons.setValues(values);

        for (String value : stretchMarkedDownMons) {
            values.add(new CephString(value));
        }
    }

    @Override
    public int getSize() {
        return 6 +
                epoch.getSize() +
                lastChanged.getSize() +
                created.getSize() +
                persistentFeatures.getSize() +
                optionalFeatures.getSize() +
                monInfo.getSize() +
                ranks.getSize() +
                minMonRelease.getSize() +
                removedRanks.getSize() +
                strategy.getSize() +
                disallowedLeaders.getSize() +
                stretchModeEnabled.getSize() +
                tieBreakerMon.getSize() +
                stretchMarkedDownMons.getSize();
    }

    @Override
    public void encode(ByteBuf byteBuf, boolean le) {
        byteBuf.writeByte(VERSION);
        byteBuf.writeByte(COMPAT_VERSION);
        if (le) {
            byteBuf.writeIntLE(getSize() - 6);
        } else {
            byteBuf.writeInt(getSize() - 6);
        }

        fsid.encode(byteBuf, le);
        epoch.encode(byteBuf, le);
        lastChanged.encode(byteBuf, le);
        created.encode(byteBuf, le);
        persistentFeatures.encode(byteBuf, le);
        optionalFeatures.encode(byteBuf, le);
        monInfo.encode(byteBuf, le);
        ranks.encode(byteBuf, le);
        minMonRelease.encode(byteBuf, le);
        removedRanks.encode(byteBuf, le);
        strategy.encode(byteBuf, le);
        disallowedLeaders.encode(byteBuf, le);
        stretchModeEnabled.encode(byteBuf, le);
        tieBreakerMon.encode(byteBuf, le);
        stretchMarkedDownMons.encode(byteBuf, le);
    }

    @Override
    public void decode(ByteBuf byteBuf, boolean le) {
        byte version = byteBuf.readByte();
        byte compat = byteBuf.readByte();
        int size = le ? byteBuf.readIntLE() : byteBuf.readInt();

        fsid = new CephUUID();
        fsid.decode(byteBuf, le);
        epoch = new Int32();
        epoch.decode(byteBuf, le);
        lastChanged = new UTime();
        lastChanged.decode(byteBuf, le);
        created = new UTime();
        created.decode(byteBuf, le);
        persistentFeatures = new MonFeature();
        persistentFeatures.decode(byteBuf, le);
        optionalFeatures = new MonFeature();
        optionalFeatures.decode(byteBuf, le);
        monInfo = new CephMap<>(CephString.class, MonInfo.class);
        monInfo.decode(byteBuf, le);
        ranks = new CephList<>(CephString.class);
        ranks.decode(byteBuf, le);
        minMonRelease = new CephEnum<>(CephRelease.class);
        minMonRelease.decode(byteBuf, le);
        removedRanks = new CephSet<>(Int32.class);
        removedRanks.decode(byteBuf, le);
        strategy = new CephEnum<>(ElectionStrategy.class);
        strategy.decode(byteBuf, le);
        disallowedLeaders = new CephSet<>(CephString.class);
        disallowedLeaders.decode(byteBuf, le);
        stretchModeEnabled = new CephBoolean();
        stretchModeEnabled.decode(byteBuf, le);
        tieBreakerMon = new CephString();
        tieBreakerMon.decode(byteBuf, le);
        stretchMarkedDownMons = new CephSet<>(CephString.class);
        stretchMarkedDownMons.decode(byteBuf, le);
    }
}
