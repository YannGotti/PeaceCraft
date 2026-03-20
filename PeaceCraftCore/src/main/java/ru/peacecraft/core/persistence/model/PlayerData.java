package ru.peacecraft.core.persistence.model;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class PlayerData {

    private final UUID uuid;
    private String currentIslandId;
    private final Set<String> discoveredIslands;
    private final Set<String> unlockedIslands;
    private String activeLogPoseTarget;
    private String devilFruitId;
    private int fisherLevel;
    private int farmerLevel;
    private String crewId;
    private long lastSaveTime;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.currentIslandId = "starter_island";
        this.discoveredIslands = new HashSet<>();
        this.unlockedIslands = new HashSet<>();
        this.activeLogPoseTarget = null;
        this.devilFruitId = null;
        this.fisherLevel = 0;
        this.farmerLevel = 0;
        this.crewId = null;
        this.lastSaveTime = System.currentTimeMillis();
    }

    // Getters
    public UUID getUuid() {
        return uuid;
    }

    public String getCurrentIslandId() {
        return currentIslandId;
    }

    public Set<String> getDiscoveredIslands() {
        return new HashSet<>(discoveredIslands);
    }

    public Set<String> getUnlockedIslands() {
        return new HashSet<>(unlockedIslands);
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

    // Setters
    public void setCurrentIslandId(String islandId) {
        this.currentIslandId = islandId;
    }

    public void addDiscoveredIsland(String islandId) {
        this.discoveredIslands.add(islandId);
    }

    public void addUnlockedIsland(String islandId) {
        this.unlockedIslands.add(islandId);
    }

    public void setActiveLogPoseTarget(String targetId) {
        this.activeLogPoseTarget = targetId;
    }

    public void setDevilFruitId(String fruitId) {
        this.devilFruitId = fruitId;
    }

    public void setFisherLevel(int level) {
        this.fisherLevel = level;
    }

    public void setFarmerLevel(int level) {
        this.farmerLevel = level;
    }

    public void setCrewId(String crewId) {
        this.crewId = crewId;
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
