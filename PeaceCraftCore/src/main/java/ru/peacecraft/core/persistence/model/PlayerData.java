package ru.peacecraft.core.persistence.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class PlayerData {

    private int dataVersion;
    private UUID uuid;

    private String currentIslandId;
    private Set<String> discoveredIslands;
    private Set<String> unlockedIslands;

    private String activeLogPoseTarget;
    private String devilFruitId;

    private int fisherLevel;
    private int farmerLevel;
    private String crewId;

    private long lastSaveTime;

    private transient boolean dirty;

    public PlayerData(UUID uuid) {
        this.dataVersion = 1;
        this.uuid = uuid;
        this.currentIslandId = null;
        this.discoveredIslands = new LinkedHashSet<>();
        this.unlockedIslands = new LinkedHashSet<>();
        this.activeLogPoseTarget = null;
        this.devilFruitId = null;
        this.fisherLevel = 0;
        this.farmerLevel = 0;
        this.crewId = null;
        this.lastSaveTime = System.currentTimeMillis();
        this.dirty = false;
    }

    public void normalizeAfterLoad(UUID expectedUuid) {
        if (this.dataVersion <= 0) {
            this.dataVersion = 1;
        }

        if (this.uuid == null) {
            this.uuid = expectedUuid;
        }

        if (this.discoveredIslands == null) {
            this.discoveredIslands = new LinkedHashSet<>();
        }

        if (this.unlockedIslands == null) {
            this.unlockedIslands = new LinkedHashSet<>();
        }

        this.dirty = false;
    }

    public int getDataVersion() {
        return dataVersion;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getCurrentIslandId() {
        return currentIslandId;
    }

    public Set<String> getDiscoveredIslands() {
        return Collections.unmodifiableSet(discoveredIslands);
    }

    public Set<String> getUnlockedIslands() {
        return Collections.unmodifiableSet(unlockedIslands);
    }

    public String getActiveLogPoseTarget() {
        return activeLogPoseTarget;
    }

    public String getDevilFruitId() {
        return devilFruitId;
    }

    public int getFisherLevel() {
        return fisherLevel;
    }

    public int getFarmerLevel() {
        return farmerLevel;
    }

    public String getCrewId() {
        return crewId;
    }

    public long getLastSaveTime() {
        return lastSaveTime;
    }

    public boolean isDirty() {
        return dirty;
    }

    public boolean hasAnyDiscoveredIsland() {
        return !discoveredIslands.isEmpty();
    }

    public void markDirty() {
        this.dirty = true;
    }

    public void markClean() {
        this.dirty = false;
    }

    public boolean setCurrentIslandId(String islandId) {
        if (Objects.equals(this.currentIslandId, islandId)) {
            return false;
        }

        this.currentIslandId = islandId;
        this.dirty = true;
        return true;
    }

    public boolean addDiscoveredIsland(String islandId) {
        boolean changed = this.discoveredIslands.add(islandId);
        if (changed) {
            this.dirty = true;
        }
        return changed;
    }

    public boolean addUnlockedIsland(String islandId) {
        boolean changed = this.unlockedIslands.add(islandId);
        if (changed) {
            this.dirty = true;
        }
        return changed;
    }

    public boolean setActiveLogPoseTarget(String targetId) {
        if (Objects.equals(this.activeLogPoseTarget, targetId)) {
            return false;
        }

        this.activeLogPoseTarget = targetId;
        this.dirty = true;
        return true;
    }

    public boolean setDevilFruitId(String fruitId) {
        if (Objects.equals(this.devilFruitId, fruitId)) {
            return false;
        }

        this.devilFruitId = fruitId;
        this.dirty = true;
        return true;
    }

    public boolean setFisherLevel(int level) {
        if (this.fisherLevel == level) {
            return false;
        }

        this.fisherLevel = level;
        this.dirty = true;
        return true;
    }

    public boolean setFarmerLevel(int level) {
        if (this.farmerLevel == level) {
            return false;
        }

        this.farmerLevel = level;
        this.dirty = true;
        return true;
    }

    public boolean setCrewId(String crewId) {
        if (Objects.equals(this.crewId, crewId)) {
            return false;
        }

        this.crewId = crewId;
        this.dirty = true;
        return true;
    }

    public void setLastSaveTime(long time) {
        this.lastSaveTime = time;
    }

    public boolean isIslandDiscovered(String islandId) {
        return discoveredIslands.contains(islandId);
    }

    public boolean isIslandUnlocked(String islandId) {
        return unlockedIslands.contains(islandId);
    }
}
